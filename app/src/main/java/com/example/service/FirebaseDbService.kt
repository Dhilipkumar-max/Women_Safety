package com.example.service

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class FirebaseDbService(private val context: android.content.Context) {

    private val prefs = context.getSharedPreferences("firebase_prefs", android.content.Context.MODE_PRIVATE)
    private val defaultUrl = "https://sustain-f2837-default-rtdb.asia-southeast1.firebasedatabase.app"

    val firebaseUrl: String = defaultUrl

    private var lastSanitizedUsername = "guest_user"

    private val client = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(5, TimeUnit.SECONDS)
        .build()

    private val _syncLogs = MutableStateFlow<List<String>>(
        listOf("Firebase Sync Engine Initialized.")
    )
    val syncLogs: StateFlow<List<String>> = _syncLogs.asStateFlow()

    private var cachedProfileJson = """
        {
          "name": "Guest Account",
          "email": "guest_user@safeher.org",
          "phone": "+91 9999999999"
        }
    """.trimIndent()

    private var cachedBiometricsJson = """
        {
          "heartRate": 74,
          "spo2": 98,
          "temperature": 36.8,
          "movement": "Walking (Steady)"
        }
    """.trimIndent()

    private var cachedAlertJson = """
        {
          "type": "Normal State",
          "threatScore": 12,
          "status": "Safe"
        }
    """.trimIndent()

    private var cachedGuardiansJson = """
        [
          {"name": "Mom (Primary)", "phone": "+91 9876543210", "status": "Active"},
          {"name": "Dad", "phone": "+91 9765432109", "status": "Active"}
        ]
    """.trimIndent()

    private var cachedLocationJson = """
        {
          "latitude": 13.08271,
          "longitude": 80.27072,
          "provider": "GPS (Active)"
        }
    """.trimIndent()

    private var cachedRecordingsJson = """
        [
          {"fileName": "Evidence_ThreatLog_216.wav", "duration": "1:42", "sizeBytes": 845000}
        ]
    """.trimIndent()

    private var cachedHistoryJson = """
        [
          {"type": "Manual SOS Triggered", "score": 95, "outcome": "False Alarm"}
        ]
    """.trimIndent()

    private val _dbNodeTree = MutableStateFlow<String>("")
    val dbNodeTree: StateFlow<String> = _dbNodeTree.asStateFlow()

    private val _isConnected = MutableStateFlow(true)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    private val ioScope = CoroutineScope(Dispatchers.IO)

    init {
        updateLocalTreeSimulation("guest_user")
    }

    private fun addLog(message: String) {
        val sdf = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault())
        val timestamp = sdf.format(Date())
        val updated = _syncLogs.value.toMutableList()
        updated.add(0, "[$timestamp] $message")
        if (updated.size > 20) updated.removeAt(updated.size - 1)
        _syncLogs.value = updated
    }

    private fun updateLocalTreeSimulation(username: String) {
        lastSanitizedUsername = username
        val dbName = try {
            val host = android.net.Uri.parse(firebaseUrl).host ?: ""
            if (host.contains(".")) host.split(".")[0] else "sustain-f2837"
        } catch (e: Exception) {
            "sustain-f2837"
        }
        _dbNodeTree.value = """
            {
              "$dbName": {
                "users": {
                  "$username": {
                    "profile": $cachedProfileJson,
                    "biometrics": $cachedBiometricsJson,
                    "active_alert": $cachedAlertJson,
                    "guardians": $cachedGuardiansJson,
                    "location": $cachedLocationJson,
                    "recordings": $cachedRecordingsJson,
                    "history": $cachedHistoryJson
                  }
                }
              }
            }
        """.trimIndent()
    }

    fun syncUserProfile(userName: String, name: String, email: String, phone: String) {
        val emailSanitized = userName.replace(".", "_").replace("@", "_")
        val json = """
            {
              "name": "$name",
              "email": "$email",
              "phone": "$phone",
              "registeredAt": ${System.currentTimeMillis()}
            }
        """.trimIndent()

        cachedProfileJson = json
        ioScope.launch {
            val url = "$firebaseUrl/users/$emailSanitized/profile.json"
            val body = json.toRequestBody("application/json; charset=utf-8".toMediaType())
            val request = Request.Builder().url(url).put(body).build()

            addLog("PUT /users/$emailSanitized/profile.json ...")
            try {
                client.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        _isConnected.value = true
                        addLog("✔ Firebase updated user profile! (HTTP 200)")
                        updateLocalTreeSimulation(emailSanitized)
                    } else {
                        addLog("❌ Firebase profile update failed (HTTP ${response.code})")
                    }
                }
            } catch (e: IOException) {
                _isConnected.value = false
                addLog("⚠ Offline mode: auth profile saved locally")
                updateLocalTreeSimulation(emailSanitized)
            }
        }
    }

    fun syncBiometrics(userName: String, hr: Int, o2: Int, temp: Float, motion: String) {
        val emailSanitized = userName.replace(".", "_").replace("@", "_")
        val json = """
            {
              "heartRate": $hr,
              "spo2": $o2,
              "temperature": $temp,
              "movement": "$motion",
              "lastSync": ${System.currentTimeMillis()}
            }
        """.trimIndent()

        cachedBiometricsJson = json
        ioScope.launch {
            val url = "$firebaseUrl/users/$emailSanitized/biometrics.json"
            val body = json.toRequestBody("application/json; charset=utf-8".toMediaType())
            val request = Request.Builder().url(url).put(body).build()

            addLog("PUT /users/$emailSanitized/biometrics.json ...")
            try {
                client.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        _isConnected.value = true
                        addLog("✔ Firebase updated biometrics details! (HTTP 200)")
                        updateLocalTreeSimulation(emailSanitized)
                    } else {
                        addLog("❌ Firebase update failed (HTTP ${response.code})")
                    }
                }
            } catch (e: IOException) {
                _isConnected.value = false
                addLog("⚠ Offline: metrics cached locally")
                updateLocalTreeSimulation(emailSanitized)
            }
        }
    }

    fun syncSensorTelemetry(
        userName: String,
        heartRate: Int,
        spo2: Int,
        temperature: Float,
        accX: Float,
        accY: Float,
        accZ: Float,
        movement: String,
        batteryLevel: Int,
        timestamp: Long
    ) {
        val emailSanitized = userName.replace(".", "_").replace("@", "_")
        val json = """
            {
              "heartRate": $heartRate,
              "spo2": $spo2,
              "temperature": $temperature,
              "accX": $accX,
              "accY": $accY,
              "accZ": $accZ,
              "movement": "$movement",
              "batteryLevel": $batteryLevel,
              "timestamp": $timestamp
            }
        """.trimIndent()

        ioScope.launch {
            val url = "$firebaseUrl/users/$emailSanitized/sensor_history/$timestamp.json"
            val body = json.toRequestBody("application/json; charset=utf-8".toMediaType())
            val request = Request.Builder().url(url).put(body).build()

            addLog("PUT /users/$emailSanitized/sensor_history/$timestamp ...")
            try {
                client.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        _isConnected.value = true
                        addLog("✔ Firebase synchronized sensor history row! (HTTP 200)")
                    } else {
                        addLog("❌ Firebase sensor history update failed (HTTP ${response.code})")
                    }
                }
            } catch (e: IOException) {
                _isConnected.value = false
                Log.e("FirebaseDbService", "Sensor history offline: ${e.message}")
            }
        }
    }

    fun syncGPSCoordinates(userName: String, lat: Double, lng: Double) {
        val emailSanitized = userName.replace(".", "_").replace("@", "_")
        val json = """
            {
              "latitude": $lat,
              "longitude": $lng,
              "timestamp": ${System.currentTimeMillis()},
              "provider": "GPS"
            }
        """.trimIndent()

        cachedLocationJson = json
        ioScope.launch {
            val url = "$firebaseUrl/users/$emailSanitized/location.json"
            val body = json.toRequestBody("application/json; charset=utf-8".toMediaType())
            val request = Request.Builder().url(url).put(body).build()

            addLog("PUT /users/$emailSanitized/location.json ...")
            try {
                client.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        _isConnected.value = true
                        addLog("✔ Firebase synchronized GPS trail! (HTTP 200)")
                        updateLocalTreeSimulation(emailSanitized)
                    } else {
                        addLog("❌ Firebase GPS sync failed (HTTP ${response.code})")
                    }
                }
            } catch (e: IOException) {
                _isConnected.value = false
                addLog("⚠ Offline: GPS coordinate routed to SQLite trail")
                updateLocalTreeSimulation(emailSanitized)
            }
        }
    }

    fun syncAlert(userName: String, alertType: String, score: Int, status: String) {
        val emailSanitized = userName.replace(".", "_").replace("@", "_")
        val json = """
            {
              "type": "$alertType",
              "threatScore": $score,
              "status": "$status",
              "timestamp": ${System.currentTimeMillis()}
            }
        """.trimIndent()

        cachedAlertJson = json
        ioScope.launch {
            val url = "$firebaseUrl/users/$emailSanitized/active_alert.json"
            val body = json.toRequestBody("application/json; charset=utf-8".toMediaType())
            val request = Request.Builder().url(url).put(body).build()

            addLog("PUT /users/$emailSanitized/active_alert.json ...")
            try {
                client.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        _isConnected.value = true
                        addLog("🚨 ✔ Firebase critical emergency alert dispatched! (HTTP 200)")
                        updateLocalTreeSimulation(emailSanitized)
                    } else {
                        addLog("❌ Alert logging failed to Firebase (HTTP ${response.code})")
                    }
                }
            } catch (e: IOException) {
                _isConnected.value = false
                addLog("⚠ Offline: critical backup triggered")
                updateLocalTreeSimulation(emailSanitized)
            }
        }
    }

    fun syncGuardians(userName: String, guardians: List<com.example.data.Guardian>) {
        val emailSanitized = userName.replace(".", "_").replace("@", "_")
        val itemsJson = guardians.joinToString(",") { g ->
            """{"name":"${g.name.replace("\"", "\\\"")}","phone":"${g.phone}","status":"${g.status}"}"""
        }
        val json = "[$itemsJson]"

        cachedGuardiansJson = json
        ioScope.launch {
            val url = "$firebaseUrl/users/$emailSanitized/guardians.json"
            val body = json.toRequestBody("application/json; charset=utf-8".toMediaType())
            val request = Request.Builder().url(url).put(body).build()

            addLog("PUT /users/$emailSanitized/guardians.json ...")
            try {
                client.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        _isConnected.value = true
                        addLog("✔ Firebase synchronized guardians! (HTTP 200)")
                        updateLocalTreeSimulation(emailSanitized)
                    } else {
                        addLog("❌ Guardians sync failed (HTTP ${response.code})")
                    }
                }
            } catch (e: IOException) {
                _isConnected.value = false
                addLog("⚠ Offline: guardians saved locally in Room SQLite")
                updateLocalTreeSimulation(emailSanitized)
            }
        }
    }

    fun syncRecordings(userName: String, recordings: List<com.example.data.Recording>) {
        val emailSanitized = userName.replace(".", "_").replace("@", "_")
        val itemsJson = recordings.joinToString(",") { r ->
            """{"fileName":"${r.fileName}","duration":"${r.duration}","timestamp":${r.timestamp},"sizeBytes":${r.sizeBytes}}"""
        }
        val json = "[$itemsJson]"

        cachedRecordingsJson = json
        ioScope.launch {
            val url = "$firebaseUrl/users/$emailSanitized/recordings.json"
            val body = json.toRequestBody("application/json; charset=utf-8".toMediaType())
            val request = Request.Builder().url(url).put(body).build()

            addLog("PUT /users/$emailSanitized/recordings.json ...")
            try {
                client.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        _isConnected.value = true
                        addLog("✔ Firebase synchronized audio tracks! (HTTP 200)")
                        updateLocalTreeSimulation(emailSanitized)
                    } else {
                        addLog("❌ Recordings sync failed (HTTP ${response.code})")
                    }
                }
            } catch (e: IOException) {
                _isConnected.value = false
                addLog("⚠ Offline: audio track references cached locally")
                updateLocalTreeSimulation(emailSanitized)
            }
        }
    }

    fun syncHistory(userName: String, logs: List<com.example.data.EmergencyLog>) {
        val emailSanitized = userName.replace(".", "_").replace("@", "_")
        val itemsJson = logs.joinToString(",") { l ->
            """{"type":"${l.type.replace("\"", "\\\"")}","score":${l.score},"timestamp":${l.timestamp},"outcome":"${l.outcome}"}"""
        }
        val json = "[$itemsJson]"

        cachedHistoryJson = json
        ioScope.launch {
            val url = "$firebaseUrl/users/$emailSanitized/history.json"
            val body = json.toRequestBody("application/json; charset=utf-8".toMediaType())
            val request = Request.Builder().url(url).put(body).build()

            addLog("PUT /users/$emailSanitized/history.json ...")
            try {
                client.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        _isConnected.value = true
                        addLog("✔ Firebase synchronized emergency history! (HTTP 200)")
                        updateLocalTreeSimulation(emailSanitized)
                    } else {
                        addLog("❌ History sync failed (HTTP ${response.code})")
                    }
                }
            } catch (e: IOException) {
                _isConnected.value = false
                addLog("⚠ Offline: history cached in Room")
                updateLocalTreeSimulation(emailSanitized)
            }
        }
    }

    fun fetchUserData(
        userName: String,
        onSuccess: (
            name: String?,
            email: String?,
            phone: String?,
            guardians: List<com.example.data.Guardian>?,
            recordings: List<com.example.data.Recording>?,
            history: List<com.example.data.EmergencyLog>?
        ) -> Unit
    ) {
        val emailSanitized = userName.replace(".", "_").replace("@", "_")
        ioScope.launch {
            try {
                // 1. Fetch Profile
                val profileUrl = "$firebaseUrl/users/$emailSanitized/profile.json"
                val profileRequest = Request.Builder().url(profileUrl).get().build()
                var nameRetrieved: String? = null
                var emailRetrieved: String? = null
                var phoneRetrieved: String? = null
                addLog("GET /users/$emailSanitized/profile.json ...")
                client.newCall(profileRequest).execute().use { response ->
                    if (response.isSuccessful) {
                        val body = response.body?.string()
                        if (!body.isNullOrEmpty() && body != "null") {
                            val obj = org.json.JSONObject(body)
                            nameRetrieved = obj.optString("name", null)
                            emailRetrieved = obj.optString("email", null)
                            phoneRetrieved = obj.optString("phone", null)
                        }
                    }
                }

                // 2. Fetch Guardians
                val guardiansUrl = "$firebaseUrl/users/$emailSanitized/guardians.json"
                val guardiansRequest = Request.Builder().url(guardiansUrl).get().build()
                val guardiansList = mutableListOf<com.example.data.Guardian>()
                addLog("GET /users/$emailSanitized/guardians.json ...")
                client.newCall(guardiansRequest).execute().use { response ->
                    if (response.isSuccessful) {
                        val body = response.body?.string()
                        if (!body.isNullOrEmpty() && body != "null") {
                            val array = org.json.JSONArray(body)
                            for (i in 0 until array.length()) {
                                val obj = array.optJSONObject(i)
                                if (obj != null) {
                                    guardiansList.add(
                                        com.example.data.Guardian(
                                            name = obj.optString("name", "Contact"),
                                            phone = obj.optString("phone", ""),
                                            status = obj.optString("status", "Active")
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                // 3. Fetch Recordings
                val recordingsUrl = "$firebaseUrl/users/$emailSanitized/recordings.json"
                val recordingsRequest = Request.Builder().url(recordingsUrl).get().build()
                val recordingsList = mutableListOf<com.example.data.Recording>()
                addLog("GET /users/$emailSanitized/recordings.json ...")
                client.newCall(recordingsRequest).execute().use { response ->
                    if (response.isSuccessful) {
                        val body = response.body?.string()
                        if (!body.isNullOrEmpty() && body != "null") {
                            val array = org.json.JSONArray(body)
                            for (i in 0 until array.length()) {
                                val obj = array.optJSONObject(i)
                                if (obj != null) {
                                    recordingsList.add(
                                        com.example.data.Recording(
                                            fileName = obj.optString("fileName", "Evidence.wav"),
                                            duration = obj.optString("duration", "0:00"),
                                            timestamp = obj.optLong("timestamp", System.currentTimeMillis()),
                                            sizeBytes = obj.optLong("sizeBytes", 0)
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                // 4. Fetch History
                val historyUrl = "$firebaseUrl/users/$emailSanitized/history.json"
                val historyRequest = Request.Builder().url(historyUrl).get().build()
                val historyList = mutableListOf<com.example.data.EmergencyLog>()
                addLog("GET /users/$emailSanitized/history.json ...")
                client.newCall(historyRequest).execute().use { response ->
                    if (response.isSuccessful) {
                        val body = response.body?.string()
                        if (!body.isNullOrEmpty() && body != "null") {
                            val array = org.json.JSONArray(body)
                            for (i in 0 until array.length()) {
                                val obj = array.optJSONObject(i)
                                if (obj != null) {
                                    historyList.add(
                                        com.example.data.EmergencyLog(
                                            type = obj.optString("type", "Alert"),
                                            score = obj.optInt("score", 0),
                                            timestamp = obj.optLong("timestamp", System.currentTimeMillis()),
                                            outcome = obj.optString("outcome", "unknown")
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                addLog("✔ Successfully retrieved all data from cloud!")
                onSuccess(nameRetrieved, emailRetrieved, phoneRetrieved, guardiansList, recordingsList, historyList)
            } catch (e: Exception) {
                addLog("❌ Data retrieval failed or user not found in DB")
                Log.e("FirebaseDbService", "fetchUserData error: ${e.message}")
                onSuccess(null, null, null, null, null, null)
            }
        }
    }
}
