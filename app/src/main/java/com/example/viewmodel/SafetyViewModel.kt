package com.example.viewmodel

import android.Manifest
import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.MainActivity
import com.example.data.*
import com.example.service.VoiceTriggerManager
import com.example.service.FirebaseDbService
import com.example.service.AudioRecorder
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.random.Random

// --- Guardian registry search data structure ---
data class SearchUserResult(val name: String, val email: String, val phone: String)

// --- Authentication UI State ---
sealed interface AuthState {
    object Unauthenticated : AuthState
    object Loading : AuthState
    data class Authenticated(val email: String, val name: String, val phone: String) : AuthState
}

// --- Live Sensor Simulation State ---
data class SensorState(
    val heartRate: Int = 76,
    val spo2: Int = 98,
    val temperature: Float = 36.5f,
    val accX: Float = 0.02f,
    val accY: Float = 0.98f,
    val accZ: Float = 0.01f,
    val movement: String = "Walking", // "Still", "Walking", "Running", "Violent"
    val deviceOnline: Boolean = true,
    val batteryLevel: Int = 89
)

// --- Emergency Tracking State ---
sealed interface EmergencyState {
    object Normal : EmergencyState
    data class Countdown(val secondsRemaining: Int, val source: String, val threatScore: Int) : EmergencyState
    data class ActiveAlert(val type: String, val timestamp: Long) : EmergencyState
}

class SafetyViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: SafetyRepository
    private var sensorJob: Job? = null
    private var countdownJob: Job? = null

    // Room observables
    val guardiansFlow: MutableStateFlow<List<Guardian>> = MutableStateFlow(emptyList())
    val logsFlow: MutableStateFlow<List<EmergencyLog>> = MutableStateFlow(emptyList())
    val recordingsFlow: MutableStateFlow<List<Recording>> = MutableStateFlow(emptyList())
    val sensorTelemetryFlow: MutableStateFlow<List<com.example.data.SensorTelemetry>> = MutableStateFlow(emptyList())

    // UI States
    private val _authState = MutableStateFlow<AuthState>(AuthState.Unauthenticated)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _sensorState = MutableStateFlow(SensorState())
    val sensorState: StateFlow<SensorState> = _sensorState.asStateFlow()

    private val _emergencyState = MutableStateFlow<EmergencyState>(EmergencyState.Normal)
    val emergencyState: StateFlow<EmergencyState> = _emergencyState.asStateFlow()

    // Threat Score
    private val _threatScore = MutableStateFlow(12)
    val threatScore: StateFlow<Int> = _threatScore.asStateFlow()

    // ECG wave points
    val heartRateHistory = mutableStateListOf<Float>()

    // Fake Call Simulation State
    private val _isFakeCallActive = MutableStateFlow(false)
    val isFakeCallActive: StateFlow<Boolean> = _isFakeCallActive.asStateFlow()

    // Voice Trigger System settings
    private val _isVoiceTriggerEnabled = MutableStateFlow(true)
    val isVoiceTriggerEnabled: StateFlow<Boolean> = _isVoiceTriggerEnabled.asStateFlow()

    private val _voiceTriggerPhrase = MutableStateFlow("Help me")
    val voiceTriggerPhrase: StateFlow<String> = _voiceTriggerPhrase.asStateFlow()

    // Firebase & Voice Trigger Native services
    private val firebaseDbService = FirebaseDbService(application)
    val firebaseLogsFlow: StateFlow<List<String>> = firebaseDbService.syncLogs
    val firebaseTreeFlow: StateFlow<String> = firebaseDbService.dbNodeTree
    val firebaseConnectedFlow: StateFlow<Boolean> = firebaseDbService.isConnected
    val firebaseUrlFlow = MutableStateFlow(firebaseDbService.firebaseUrl).asStateFlow()

    // Real-time lockscreen audio recorder
    val audioRecorder = AudioRecorder(application)

    // User Directory Registry for Live Guardian search
    val registryUsers = listOf(
        SearchUserResult("Dhilip Kumar", "dhilip@gmail.com", "+91 9081234567"),
        SearchUserResult("Jessica Smith", "jess.smith@outlook.com", "+1 312 555 0192"),
        SearchUserResult("Sarah Connor", "sconnor@cyberdyne.org", "+1 213 555 4811"),
        SearchUserResult("Jane Doe", "jane.doe@gmail.com", "+91 9444158421"),
        SearchUserResult("Abigail Watson", "abigail.w@gmail.com", "+1 415 889 0013"),
        SearchUserResult("Priya Sharma", "priya.sharma@yahoo.co.in", "+91 7358129410")
    )

    private val _guardianSearchQuery = MutableStateFlow("")
    val guardianSearchQuery: StateFlow<String> = _guardianSearchQuery.asStateFlow()

    private val _guardianSearchResults = MutableStateFlow<List<SearchUserResult>>(emptyList())
    val guardianSearchResults: StateFlow<List<SearchUserResult>> = _guardianSearchResults.asStateFlow()

    fun updateGuardianSearchQuery(query: String) {
        _guardianSearchQuery.value = query
        if (query.isBlank()) {
            _guardianSearchResults.value = emptyList()
        } else {
            _guardianSearchResults.value = registryUsers.filter {
                it.name.contains(query, ignoreCase = true) || it.email.contains(query, ignoreCase = true)
            }
        }
    }

    private var voiceTriggerManager: VoiceTriggerManager? = null
    val voiceStatusFlow: StateFlow<String> get() = voiceTriggerManager?.voiceStatus ?: MutableStateFlow("Tap Start to activate Voice Detection Dashboard").asStateFlow()
    val lastRecognizedTextFlow: StateFlow<String> get() = voiceTriggerManager?.lastRecognizedText ?: MutableStateFlow("").asStateFlow()
    val isVoiceActiveFlow: StateFlow<Boolean> get() = voiceTriggerManager?.isListening ?: MutableStateFlow(false).asStateFlow()

    // High Stress Simulation Trigger (for interactive testing)
    private val _isSimulatingHighStress = MutableStateFlow(false)
    val isSimulatingHighStress: StateFlow<Boolean> = _isSimulatingHighStress.asStateFlow()

    // Active screen navigation logic helper
    private val _currentRoute = MutableStateFlow("splash")
    val currentRoute: StateFlow<String> = _currentRoute.asStateFlow()

    // Location Simulation coordinates
    private val _latitude = MutableStateFlow(13.08271)
    val latitude: StateFlow<Double> = _latitude.asStateFlow()
    private val _longitude = MutableStateFlow(80.27072)
    val longitude: StateFlow<Double> = _longitude.asStateFlow()

    // Route history
    val routeHistory = mutableStateListOf<Pair<Double, Double>>()

    init {
        val database = SafetyDatabase.getDatabase(application)
        repository = SafetyRepository(database.safetyDao())

        // Restore auth session from preferences
        val authPrefs = application.getSharedPreferences("safeher_auth_prefs", Context.MODE_PRIVATE)
        val isLoggedIn = authPrefs.getBoolean("is_logged_in", false)
        if (isLoggedIn) {
            val email = authPrefs.getString("user_email", "") ?: ""
            val name = authPrefs.getString("user_name", "") ?: ""
            val phone = authPrefs.getString("user_phone", "") ?: ""
            if (email.isNotBlank()) {
                _authState.value = AuthState.Authenticated(email, name, phone)
                _currentRoute.value = "home"
            }
        }

        // Initial setup for history
        routeHistory.add(Pair(13.08271, 80.27072))

        // Pre-fill mock HR history
        for (i in 0..25) {
            heartRateHistory.add(72f + Random.nextInt(10))
        }

        // Fetch local Room DB state
        viewModelScope.launch {
            repository.allGuardians.collect { list ->
                if (list.isEmpty()) {
                    // Seed initial trusted contacts for stunning visual appearance
                    repository.insertGuardian(Guardian(name = "Mom (Primary)", phone = "+91 9876543210", status = "Active"))
                    repository.insertGuardian(Guardian(name = "Dad", phone = "+91 9765432109", status = "Active"))
                    repository.insertGuardian(Guardian(name = "Sister (Urgent)", phone = "+91 9456123780", status = "Pending"))
                } else {
                    guardiansFlow.value = list
                    val auth = _authState.value
                    val user = if (auth is AuthState.Authenticated) auth.email else "guest_user"
                    firebaseDbService.syncGuardians(user, list)
                }
            }
        }

        viewModelScope.launch {
            repository.allLogs.collect { list ->
                logsFlow.value = list
                val auth = _authState.value
                val user = if (auth is AuthState.Authenticated) auth.email else "guest_user"
                firebaseDbService.syncHistory(user, list)
            }
        }

        viewModelScope.launch {
            repository.allRecordings.collect { list ->
                if (list.isEmpty()) {
                    // Seed initial audio recording evidence as logs
                    repository.insertRecording(Recording(fileName = "Evidence_ThreatLog_216.wav", duration = "1:42", timestamp = System.currentTimeMillis() - 86400000, sizeBytes = 845000))
                    repository.insertRecording(Recording(fileName = "Evidence_PanicSOS_401.wav", duration = "0:35", timestamp = System.currentTimeMillis() - 172800000, sizeBytes = 210000))
                } else {
                    recordingsFlow.value = list
                    val auth = _authState.value
                    val user = if (auth is AuthState.Authenticated) auth.email else "guest_user"
                    firebaseDbService.syncRecordings(user, list)
                }
            }
        }

        viewModelScope.launch {
            repository.allTelemetry.collect { list ->
                sensorTelemetryFlow.value = list
            }
        }

        // Initialize Speech Trigger service on Main Thread
        voiceTriggerManager = VoiceTriggerManager(application) { triggeredKeyword ->
            triggerVoiceEmergency()
        }

        // Run perpetual sensor loop
        startSensorMonitoring()
    }

    // --- Foreground/Background Activity Lifecycle Sync ---
    fun onActivityResumed() {
        if (_isVoiceTriggerEnabled.value) {
            voiceTriggerManager?.startListening()
        }
    }

    fun onActivityPaused() {
        voiceTriggerManager?.stopListening()
    }

    // --- Active Navigation Helpers ---
    fun navigateTo(route: String) {
        _currentRoute.value = route
    }

    // --- Authentication ---
    fun register(name: String, email: String, phone: String) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            _authState.value = AuthState.Authenticated(email, name, phone)
            
            // Save to persistent preferences
            val prefs = getApplication<Application>().getSharedPreferences("safeher_auth_prefs", Context.MODE_PRIVATE)
            prefs.edit()
                .putBoolean("is_logged_in", true)
                .putString("user_email", email)
                .putString("user_name", name)
                .putString("user_phone", phone)
                .apply()

            firebaseDbService.syncUserProfile(email, name, email, phone)
            firebaseDbService.syncGuardians(email, guardiansFlow.value)
            firebaseDbService.syncHistory(email, logsFlow.value)
            firebaseDbService.syncRecordings(email, recordingsFlow.value)
            _currentRoute.value = "home"
        }
    }

    fun login(email: String, phone: String) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            // First we attempt to retrieve actual user data from Firebase Realtime Database
            firebaseDbService.fetchUserData(email) { nameRetrieved, emailRetrieved, phoneRetrieved, guardians, recordings, history ->
                viewModelScope.launch {
                    val finalName = nameRetrieved ?: email.substringBefore("@").replaceFirstChar { it.uppercase() }
                    val finalPhone = phoneRetrieved ?: phone.ifBlank { "+91 9999999999" }
                    
                    _authState.value = AuthState.Authenticated(email, finalName, finalPhone)
                    
                    // Save to persistent SharedPreferences
                    val prefs = getApplication<Application>().getSharedPreferences("safeher_auth_prefs", Context.MODE_PRIVATE)
                    prefs.edit()
                        .putBoolean("is_logged_in", true)
                        .putString("user_email", email)
                        .putString("user_name", finalName)
                        .putString("user_phone", finalPhone)
                        .apply()

                    // Clear local DB cache and populate retrieved Firebase records
                    try {
                        if (guardians != null && guardians.isNotEmpty()) {
                            repository.clearGuardians()
                            for (g in guardians) {
                                repository.insertGuardian(g)
                            }
                        }
                        if (recordings != null && recordings.isNotEmpty()) {
                            repository.clearRecordings()
                            for (r in recordings) {
                                repository.insertRecording(r)
                            }
                        }
                        if (history != null && history.isNotEmpty()) {
                            repository.clearLogs()
                            for (h in history) {
                                repository.insertLog(h)
                            }
                        }
                        
                        // Push profile sync right back so database state registers cleanly
                        firebaseDbService.syncUserProfile(email, finalName, email, finalPhone)
                    } catch (e: Exception) {
                        Log.e("SafetyViewModel", "Error restoring retrieved rows: ${e.message}")
                    }
                }
            }
            
            delay(1500)
            // Safety fallback check in case network fails or user doesn't exist yet on DB
            if (_authState.value !is AuthState.Authenticated) {
                val fallbackName = email.substringBefore("@").replaceFirstChar { it.uppercase() }
                _authState.value = AuthState.Authenticated(email, fallbackName, phone)
                
                val prefs = getApplication<Application>().getSharedPreferences("safeher_auth_prefs", Context.MODE_PRIVATE)
                prefs.edit()
                    .putBoolean("is_logged_in", true)
                    .putString("user_email", email)
                    .putString("user_name", fallbackName)
                    .putString("user_phone", phone)
                    .apply()

                firebaseDbService.syncUserProfile(email, fallbackName, email, phone)
                firebaseDbService.syncGuardians(email, guardiansFlow.value)
                firebaseDbService.syncHistory(email, logsFlow.value)
                firebaseDbService.syncRecordings(email, recordingsFlow.value)
            }
            _currentRoute.value = "home"
        }
    }

    fun logout() {
        viewModelScope.launch {
            _authState.value = AuthState.Unauthenticated
            
            // Clear persistent SharedPreferences session
            val prefs = getApplication<Application>().getSharedPreferences("safeher_auth_prefs", Context.MODE_PRIVATE)
            prefs.edit().clear().apply()

            // Delete current user's local cached data in Room for strict user privacy
            try {
                repository.clearGuardians()
                repository.clearLogs()
                repository.clearRecordings()

                // Insert standard base trusted guardians as local seed UI placeholders
                repository.insertGuardian(Guardian(name = "Mom (Primary)", phone = "+91 9876543210", status = "Active"))
                repository.insertGuardian(Guardian(name = "Dad", phone = "+91 9765432109", status = "Active"))
                repository.insertGuardian(Guardian(name = "Sister (Urgent)", phone = "+91 9456123780", status = "Pending"))
            } catch (e: Exception) {
                Log.e("SafetyViewModel", "Error clearing Room cache on logout: ${e.message}")
            }

            firebaseDbService.syncUserProfile("guest_user", "Guest Account", "guest_user@safeher.org", "+91 9999999999")
            _currentRoute.value = "login"
        }
    }

    // --- Fake Call Engine ---
    fun triggerFakeCall(show: Boolean) {
        _isFakeCallActive.value = show
    }

    // --- Voice Configuration and Toggles ---
    fun toggleVoiceTrigger(enabled: Boolean) {
        _isVoiceTriggerEnabled.value = enabled
        if (enabled) {
            voiceTriggerManager?.startListening()
        } else {
            voiceTriggerManager?.stopListening()
        }
    }

    fun onRecordAudioPermissionGranted() {
        if (_isVoiceTriggerEnabled.value) {
            voiceTriggerManager?.startListening()
        }
    }

    fun simulateVoiceCommand(phrase: String) {
        voiceTriggerManager?.simulatePhrase(phrase)
    }

    fun updateVoiceTriggerPhrase(phrase: String) {
        _voiceTriggerPhrase.value = phrase
    }

    fun toggleHighStressSimulation(enabled: Boolean) {
        _isSimulatingHighStress.value = enabled
    }

    fun updateLocation(lat: Double, lng: Double) {
        _latitude.value = lat
        _longitude.value = lng
        routeHistory.add(Pair(lat, lng))
        if (routeHistory.size > 50) {
            routeHistory.removeAt(0)
        }
        val auth = _authState.value
        val user = if (auth is AuthState.Authenticated) auth.email else "guest_user"
        firebaseDbService.syncGPSCoordinates(user, lat, lng)
    }

    // --- Core Sensor Monitoring Daemon (Simulated real-time Wi-Fi state) ---
    private fun startSensorMonitoring() {
        sensorJob?.cancel()
        sensorJob = viewModelScope.launch {
            while (true) {
                delay(1500)

                // Fluctuates slightly under normal or jumps high under stress simulation
                val isStress = _isSimulatingHighStress.value
                val newHR = if (isStress) {
                    Random.nextInt(132, 146)
                } else {
                    70 + Random.nextInt(12)
                }

                val newO2 = if (isStress) {
                    Random.nextInt(88, 92)
                } else {
                    96 + Random.nextInt(4)
                }

                val temp = if (isStress) 37.8f else 36.4f + (Random.nextInt(5) / 10f)

                val motion = if (isStress) {
                    "Violent Anomaly"
                } else {
                    val states = listOf("Still", "Walking", "Walking", "Running", "Still")
                    states[Random.nextInt(states.size)]
                }

                // Smooth gravity coordinates
                val ax = if (isStress) Random.nextFloat() * 4f else Random.nextFloat() * 0.1f
                val ay = if (isStress) Random.nextFloat() * 6f else 0.98f + (Random.nextFloat() * 0.05f)
                val az = if (isStress) Random.nextFloat() * 3f else Random.nextFloat() * 0.1f

                _sensorState.value = SensorState(
                    heartRate = newHR,
                    spo2 = newO2,
                    temperature = temp,
                    accX = ax,
                    accY = ay,
                    accZ = az,
                    movement = motion,
                    deviceOnline = true,
                    batteryLevel = if (_sensorState.value.batteryLevel > 1) _sensorState.value.batteryLevel - 1 else 99
                )

                // Push biometrics telemetry directly to cloud Firebase Realtime Database
                val auth = _authState.value
                val user = if (auth is AuthState.Authenticated) auth.email else "guest_user"
                firebaseDbService.syncBiometrics(user, newHR, newO2, temp, motion)

                // Insert sensor telemetry into local Room SQLite database as well
                val nowTime = System.currentTimeMillis()
                repository.insertTelemetry(
                    com.example.data.SensorTelemetry(
                        username = user,
                        heartRate = newHR,
                        spo2 = newO2,
                        temperature = temp,
                        accX = ax,
                        accY = ay,
                        accZ = az,
                        movement = motion,
                        batteryLevel = _sensorState.value.batteryLevel,
                        timestamp = nowTime
                    )
                )

                // Replicate exact attributes to central cloud Firebase Realtime Database
                firebaseDbService.syncSensorTelemetry(
                    userName = user,
                    heartRate = newHR,
                    spo2 = newO2,
                    temperature = temp,
                    accX = ax,
                    accY = ay,
                    accZ = az,
                    movement = motion,
                    batteryLevel = _sensorState.value.batteryLevel,
                    timestamp = nowTime
                )

                // Update ECG history
                heartRateHistory.add(newHR.toFloat())
                if (heartRateHistory.size > 25) {
                    heartRateHistory.removeAt(0)
                }

                // Simulated GPS updates (moving trail when on alert)
                val isAlert = _emergencyState.value is EmergencyState.ActiveAlert
                if (isAlert) {
                    val latOffset = (Random.nextDouble() - 0.5) * 0.0003
                    val lngOffset = (Random.nextDouble() - 0.5) * 0.0003
                    _latitude.value += latOffset
                    _longitude.value += lngOffset
                    routeHistory.add(Pair(_latitude.value, _longitude.value))
                    if (routeHistory.size > 50) {
                        routeHistory.removeAt(0)
                    }

                    // Push real-time location stream trail to Firebase Realtime Database
                    firebaseDbService.syncGPSCoordinates(user, _latitude.value, _longitude.value)
                }

                // AI Threat Score Calculation Engine (Section 11)
                calculateInteractiveThreatScore(newHR, newO2, motion)
            }
        }
    }

    private fun calculateInteractiveThreatScore(hr: Int, o2: Int, motion: String) {
        // If current state is already in countdown or active alert, don't re-trigger from sensors
        if (_emergencyState.value !is EmergencyState.Normal) return

        var score = 10
        if (hr > 130) score += 35
        if (o2 < 91) score += 25
        if (motion == "Violent Anomaly" || motion == "Running") score += 30

        score = score.coerceIn(0, 100)
        _threatScore.value = score

        // Threshold check (Threshold set to > 75)
        if (score > 75) {
            triggerEmergencyCountdown("Automated Sensor Detection (Biometrics Anomalies)", score)
        }
    }

    // --- Emergency Controller State Machine ---
    fun triggerVoiceEmergency() {
        if (_emergencyState.value is EmergencyState.Normal) {
            triggerEmergencyCountdown("Voice Trigger Word detected", 100)
        }
    }

    fun triggerManualSOS() {
        if (_emergencyState.value is EmergencyState.Normal) {
            triggerEmergencyCountdown("Immediate Manual SOS Hotkey Hold", 100)
        }
    }

    private fun triggerEmergencyCountdown(reason: String, threatScore: Int) {
        countdownJob?.cancel()
        _emergencyState.value = EmergencyState.Countdown(15, reason, threatScore)
        _currentRoute.value = "emergency"

        countdownJob = viewModelScope.launch {
            var secs = 15
            while (secs > 0) {
                delay(1000)
                secs--
                val current = _emergencyState.value
                if (current is EmergencyState.Countdown) {
                    _emergencyState.value = current.copy(secondsRemaining = secs)
                }
            }
            // Transition to COMPLETE ACTIVE EMERGENCY when countdown finishes
            activateFullEmergency(reason, threatScore)
        }
    }

    private suspend fun activateFullEmergency(reason: String, score: Int) {
        _emergencyState.value = EmergencyState.ActiveAlert(reason, System.currentTimeMillis())

        val auth = _authState.value
        val user = if (auth is AuthState.Authenticated) auth.email else "guest_user"
        // Instant synchronous update to Firebase RTDB alert node for central remote monitoring
        firebaseDbService.syncAlert(user, reason, score, "Active Alert")

        // Dispatch system-wide Heads Up peak notification that shows on Locked Screen or Home Screen
        showEmergencyHeadsUpNotification(reason, score)

        // Start live ambient recording on context channel (continuous mic capture even when in background or locked screen)
        val recFile = audioRecorder.startRecording()
        val recStatus = if (recFile != null) " [RECORDING LIVE]" else " [REC ERROR]"

        // Insert log in Room DB (Section 11)
        repository.insertLog(
            EmergencyLog(
                type = reason + recStatus,
                score = score,
                outcome = "Active Alert"
            )
        )

        // Save real-time evidence record
        val recNum = Random.nextInt(100, 999)
        repository.insertRecording(
            Recording(
                fileName = recFile?.name ?: "Evidence_AmbientAudio_$recNum.mp3",
                duration = "0:45",
                timestamp = System.currentTimeMillis(),
                sizeBytes = recFile?.length() ?: 314000
            )
        )
    }

    private fun showEmergencyHeadsUpNotification(reason: String, score: Int) {
        val context = getApplication<Application>().applicationContext
        val channelId = "safeher_urgent_channel"
        val channelName = "SafeHer Alerts"

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(channelId, channelName, importance).apply {
                description = "Critical SafeHer active panic status alert channel"
                enableVibration(true)
                vibrationPattern = longArrayOf(150, 300, 150, 400, 300, 500)
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
        )

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.stat_notify_chat)
            .setContentTitle("🚨 SafeHer Active Alert Triggered!")
            .setContentText("Emergency detected: $reason ($score%). Local Audio Tracking Live.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVibrate(longArrayOf(150, 300, 150, 400, 300, 500))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        try {
            if (androidx.core.app.ActivityCompat.checkSelfPermission(
                    context, Manifest.permission.POST_NOTIFICATIONS
                ) == android.content.pm.PackageManager.PERMISSION_GRANTED ||
                android.os.Build.VERSION.SDK_INT < 33
            ) {
                val notificationManager = NotificationManagerCompat.from(context)
                notificationManager.notify(911, builder.build())
            }
        } catch (e: Exception) {
            Log.e("SafetyViewModel", "Notification dispatch error: ${e.message}")
        }
    }

    fun cancelEmergency(pin: String): Boolean {
        // Simple cancellation PIN check
        if (pin == "1234") {
            countdownJob?.cancel()

            // Stop real microphone ambient audio capture
            val stoppedFile = audioRecorder.stopRecording()
            if (stoppedFile != null) {
                viewModelScope.launch {
                    repository.insertRecording(
                        Recording(
                            fileName = stoppedFile.name,
                            duration = "Recorded Live",
                            timestamp = System.currentTimeMillis(),
                            sizeBytes = stoppedFile.length()
                        )
                    )
                }
            }

            val stateName = when (val s = _emergencyState.value) {
                is EmergencyState.Countdown -> s.source
                is EmergencyState.ActiveAlert -> s.type
                else -> "User Triggered Alert"
            }

            val auth = _authState.value
            val user = if (auth is AuthState.Authenticated) auth.email else "guest_user"
            // Reset Firebase Alert node stream status
            firebaseDbService.syncAlert(user, "Normal State", 12, "Normal (PIN Cancelled)")

            viewModelScope.launch {
                repository.insertLog(
                    EmergencyLog(
                        type = stateName,
                        score = _threatScore.value,
                        outcome = "Cancelled (PIN Confirmed)"
                    )
                )
            }

            _emergencyState.value = EmergencyState.Normal
            _threatScore.value = 12
            _isSimulatingHighStress.value = false
            _currentRoute.value = "home"
            return true
        }
        return false
    }

    // --- Guardian Operations ---
    fun addGuardian(name: String, phone: String) {
        viewModelScope.launch {
            repository.insertGuardian(
                Guardian(
                    name = name,
                    phone = phone,
                    status = "Pending" // Starts pending as per Section 9.7 standard
                )
            )
        }
    }

    fun acceptRequest(guardian: Guardian) {
        viewModelScope.launch {
            repository.insertGuardian(guardian.copy(status = "Active"))
        }
    }

    fun removeGuardian(guardian: Guardian) {
        viewModelScope.launch {
            repository.deleteGuardian(guardian)
        }
    }

    // --- Logs Cleanup ---
    fun clearLogs() {
        viewModelScope.launch {
            repository.clearLogs()
        }
    }

    // --- Recording CRUD ---
    fun deleteRecording(recording: Recording) {
        viewModelScope.launch {
            repository.deleteRecording(recording)
        }
    }

    // --- Advanced Local SQLite Administration Daemon Tools ---
    fun wipeDatabase() {
        viewModelScope.launch {
            // Empty cached files starting with "Evidence_"
            try {
                val files = getApplication<Application>().cacheDir.listFiles()
                files?.forEach { file ->
                    if (file.name.contains("Evidence_")) {
                        file.delete()
                    }
                }
            } catch (e: Exception) {
                Log.e("SafetyViewModel", "Error purging evidence file binaries: ${e.message}")
            }

            // Remove guardians from database
            guardiansFlow.value.forEach {
                repository.deleteGuardian(it)
            }
            // Remove audio logs
            recordingsFlow.value.forEach {
                repository.deleteRecording(it)
            }
            // Flush activity logs
            repository.clearLogs()
            // Flush sensor telemetry
            repository.clearTelemetry()

            // Reset dynamic states to neutral
            guardiansFlow.value = emptyList()
            recordingsFlow.value = emptyList()
            logsFlow.value = emptyList()
            sensorTelemetryFlow.value = emptyList()

            val auth = _authState.value
            val user = if (auth is AuthState.Authenticated) auth.email else "guest_user"
            firebaseDbService.syncGuardians(user, emptyList())
            firebaseDbService.syncHistory(user, emptyList())
            firebaseDbService.syncRecordings(user, emptyList())
        }
    }

    fun factorySeedDatabase() {
        viewModelScope.launch {
            // First run fresh wipe
            wipeDatabase()
            delay(400)

            // Inject primary trusted guardians
            repository.insertGuardian(Guardian(name = "Mom (Primary)", phone = "+91 9876543210", status = "Active"))
            repository.insertGuardian(Guardian(name = "Dad", phone = "+91 9765432109", status = "Active"))
            repository.insertGuardian(Guardian(name = "Sister (Urgent)", phone = "+91 9456123780", status = "Pending"))

            // Seed historical active logs
            repository.insertLog(EmergencyLog(type = "Manual SOS Triggered", score = 95, outcome = "False Alarm"))
            repository.insertLog(EmergencyLog(type = "Voice phrase: \"Help me\"", score = 88, outcome = "Active Alert"))
            repository.insertLog(EmergencyLog(type = "Fall Detected (IMU sensor)", score = 99, outcome = "Cancelled"))

            // Inject audio evidence telemetry files
            repository.insertRecording(Recording(fileName = "Evidence_ThreatLog_216.wav", duration = "1:42", timestamp = System.currentTimeMillis() - 86450000, sizeBytes = 845000))
            repository.insertRecording(Recording(fileName = "Evidence_PanicSOS_401.wav", duration = "0:35", timestamp = System.currentTimeMillis() - 172900000, sizeBytes = 210000))

            // Seed historical sensor data
            val auth = _authState.value
            val user = if (auth is AuthState.Authenticated) auth.email else "guest_user"
            repository.insertTelemetry(com.example.data.SensorTelemetry(username = user, heartRate = 72, spo2 = 98, temperature = 36.5f, accX = 0.01f, accY = 0.99f, accZ = 0.02f, movement = "Still", batteryLevel = 84))
            repository.insertTelemetry(com.example.data.SensorTelemetry(username = user, heartRate = 85, spo2 = 99, temperature = 36.6f, accX = 0.12f, accY = 1.05f, accZ = 0.08f, movement = "Walking", batteryLevel = 83))
        }
    }
}
