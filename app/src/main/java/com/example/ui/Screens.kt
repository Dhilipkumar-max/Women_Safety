package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.*
import com.example.ui.theme.*
import com.example.viewmodel.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// --- 1. Splash Screen ---
@Composable
fun SplashScreen(viewModel: SafetyViewModel) {
    var progress by remember { mutableStateOf(0f) }
    var scaleLogo by remember { mutableStateOf(0.4f) }

    LaunchedEffect(Unit) {
        animate(
            initialValue = 0.4f,
            targetValue = 1.0f,
            animationSpec = spring(dampingRatio = 0.5f, stiffness = Spring.StiffnessLow)
        ) { valValue, _ -> scaleLogo = valValue }

        while (progress < 1.0f) {
            delay(100)
            progress += 0.05f
        }
        // Redirect based on current auth state
        viewModel.navigateTo("onboarding")
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(DeepIndigo, BackgroundDark))),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            Box(
                modifier = Modifier
                    .scale(scaleLogo)
                    .size(110.dp)
                    .background(ElectricViolet.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = "SafeHer Logo",
                    tint = ElectricViolet,
                    modifier = Modifier.size(56.dp)
                )
                // Pulsing accent ring around logo
                val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                val pulseScale by infiniteTransition.animateFloat(
                    initialValue = 1.0f,
                    targetValue = 1.3f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1200, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "pulseScale"
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .scale(pulseScale)
                        .background(ElectricViolet.copy(alpha = 0.05f), CircleShape)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "SafeHer",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimaryDark,
                fontFamily = FontFamily.SansSerif,
                letterSpacing = 1.sp
            )
            Text(
                text = "Your Companion, Always Secure",
                fontSize = 14.sp,
                color = TextSecondaryDark,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(48.dp))
            LinearProgressIndicator(
                progress = { progress },
                color = ElectricViolet,
                trackColor = SurfaceDark,
                modifier = Modifier
                    .width(180.dp)
                    .height(6.dp)
                    .clip(CircleShape)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Establishing AES Encrypted Sync...",
                fontSize = 12.sp,
                color = TextSecondaryDark
            )
        }
    }
}

// --- 2. Onboarding Screen ---
@Composable
fun OnboardingScreen(viewModel: SafetyViewModel) {
    var step by remember { mutableStateOf(1) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 600.dp)
                .fillMaxHeight()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Top Bar
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "SafeHer Setup",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = ElectricViolet
                )
                Text(
                    text = "Skip",
                    fontSize = 14.sp,
                    color = TextSecondaryDark,
                    modifier = Modifier.clickable { viewModel.navigateTo("login") }
                )
            }

            // Animated step graphics and text
            Crossfade(targetState = step, label = "stepAnim") { currentStep ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    when (currentStep) {
                        1 -> {
                            Icon(
                                imageVector = Icons.Default.Watch,
                                contentDescription = "Wearable Connection",
                                tint = ElectricViolet,
                                modifier = Modifier.size(120.dp).padding(16.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Pair Wearable Smart Device",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimaryDark,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Connect your ESP32 safety wristband over local Bluetooth/Wi-Fi to monitor pulse parameters and high-frequency G forces continuously.",
                                fontSize = 14.sp,
                                color = TextSecondaryDark,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 12.dp)
                            )
                        }
                        2 -> {
                            Icon(
                                imageVector = Icons.Default.Group,
                                contentDescription = "Guardian Circles",
                                tint = SafetyGreen,
                                modifier = Modifier.size(120.dp).padding(16.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Add Trusted Circle",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimaryDark,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Configure up to five guardians, parents, or roommates. They automatically receive high-priority alerts with live maps during real threats.",
                                fontSize = 14.sp,
                                color = TextSecondaryDark,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 12.dp)
                            )
                        }
                        3 -> {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = "Security permissions",
                                tint = EmergencyRed,
                                modifier = Modifier.size(120.dp).padding(16.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Grant Safety Clearances",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimaryDark,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "We prioritize privacy. SafeHer requires Foreground Microphone access for voice activation triggers and Location permissions for GPS feeds.",
                                fontSize = 14.sp,
                                color = TextSecondaryDark,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 12.dp)
                            )
                        }
                    }
                }
            }

            // Bottom controls
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Indicators dots
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(bottom = 24.dp)
                ) {
                    for (i in 1..3) {
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .size(if (i == step) 16.dp else 8.dp, 8.dp)
                                .background(
                                    color = if (i == step) ElectricViolet else SurfaceDark,
                                    shape = CircleShape
                                )
                        )
                    }
                }

                Button(
                    onClick = {
                        if (step < 3) {
                            step++
                        } else {
                            viewModel.navigateTo("login")
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricViolet),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    Text(
                        text = if (step == 3) "Finish Setup" else "Continue Dashboard",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

// --- 3. Login / Signup Screen ---
@Composable
fun LoginAndSignupScreen(viewModel: SafetyViewModel) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var isNewAccount by remember { mutableStateOf(false) }

    val authState by viewModel.authState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 600.dp)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (isNewAccount) "Create SafeHer Account" else "Welcome back to SafeHer",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimaryDark,
                textAlign = TextAlign.Center
            )
            Text(
                text = if (isNewAccount) "Join our protective coverage network" else "Sign in to stay continuously protected",
                fontSize = 14.sp,
                color = TextSecondaryDark,
                modifier = Modifier.padding(top = 4.dp),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Email Field
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email Address") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ElectricViolet,
                    unfocusedBorderColor = SurfaceDark,
                    focusedLabelColor = ElectricViolet,
                    focusedTextColor = TextPrimaryDark,
                    unfocusedTextColor = TextPrimaryDark
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Phone Field if registration
            if (isNewAccount) {
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Emergency Contact Phone") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElectricViolet,
                        unfocusedBorderColor = SurfaceDark,
                        focusedLabelColor = ElectricViolet,
                        focusedTextColor = TextPrimaryDark,
                        unfocusedTextColor = TextPrimaryDark
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
            } else {
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Optional Hotline Phone") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElectricViolet,
                        unfocusedBorderColor = SurfaceDark,
                        focusedLabelColor = ElectricViolet,
                        focusedTextColor = TextPrimaryDark,
                        unfocusedTextColor = TextPrimaryDark
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Password Field
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ElectricViolet,
                    unfocusedBorderColor = SurfaceDark,
                    focusedLabelColor = ElectricViolet,
                    focusedTextColor = TextPrimaryDark,
                    unfocusedTextColor = TextPrimaryDark
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (authState is AuthState.Loading) {
                CircularProgressIndicator(color = ElectricViolet)
            } else {
                Button(
                    onClick = {
                        if (email.contains("@") && password.length >= 4) {
                            if (isNewAccount) {
                                viewModel.register(email.substringBefore("@"), email, phone)
                            } else {
                                viewModel.login(email, phone)
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricViolet),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    Text(
                        text = if (isNewAccount) "Create Account" else "Sign In",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = if (isNewAccount) "Already have an account? Sign In" else "New to SafeHer? Create free Account",
                    color = ElectricViolet,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.clickable { isNewAccount = !isNewAccount }
                )
            }
        }
    }
}

// --- Common Custom Bottom Navigation Bar ---
@Composable
fun MainBottomNavigationBar(activeScreen: String, navigate: (String) -> Unit) {
    NavigationBar(
        containerColor = SurfaceDark,
        tonalElevation = 8.dp,
        modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        NavigationBarItem(
            selected = activeScreen == "home",
            onClick = { navigate("home") },
            icon = { Icon(Icons.Default.Home, contentDescription = "Dashboard") },
            label = { Text("Dashboard", fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = ElectricViolet,
                selectedTextColor = ElectricViolet,
                unselectedIconColor = TextSecondaryDark,
                unselectedTextColor = TextSecondaryDark,
                indicatorColor = SurfaceDarkSecondary
            )
        )
        NavigationBarItem(
            selected = activeScreen == "monitoring",
            onClick = { navigate("monitoring") },
            icon = { Icon(Icons.Default.MonitorHeart, contentDescription = "Monitor") },
            label = { Text("Biometrics", fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = ElectricViolet,
                selectedTextColor = ElectricViolet,
                unselectedIconColor = TextSecondaryDark,
                unselectedTextColor = TextSecondaryDark,
                indicatorColor = SurfaceDarkSecondary
            )
        )
        NavigationBarItem(
            selected = activeScreen == "guardians",
            onClick = { navigate("guardians") },
            icon = { Icon(Icons.Default.Group, contentDescription = "Guardians") },
            label = { Text("Guardians", fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = ElectricViolet,
                selectedTextColor = ElectricViolet,
                unselectedIconColor = TextSecondaryDark,
                unselectedTextColor = TextSecondaryDark,
                indicatorColor = SurfaceDarkSecondary
            )
        )
        NavigationBarItem(
            selected = activeScreen == "location",
            onClick = { navigate("location") },
            icon = { Icon(Icons.Default.LocationOn, contentDescription = "Location") },
            label = { Text("Live Tracker", fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = ElectricViolet,
                selectedTextColor = ElectricViolet,
                unselectedIconColor = TextSecondaryDark,
                unselectedTextColor = TextSecondaryDark,
                indicatorColor = SurfaceDarkSecondary
            )
        )
        NavigationBarItem(
            selected = activeScreen == "evidence",
            onClick = { navigate("evidence") },
            icon = { Icon(Icons.Default.Folder, contentDescription = "Evidence") },
            label = { Text("Evidence", fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = ElectricViolet,
                selectedTextColor = ElectricViolet,
                unselectedIconColor = TextSecondaryDark,
                unselectedTextColor = TextSecondaryDark,
                indicatorColor = SurfaceDarkSecondary
            )
        )
    }
}

// --- 4. Home Dashboard Screen ---
@Composable
fun HomeDashboardScreen(viewModel: SafetyViewModel) {
    val sensorState by viewModel.sensorState.collectAsState()
    val threatScore by viewModel.threatScore.collectAsState()
    val isVoiceEnabled by viewModel.isVoiceTriggerEnabled.collectAsState()
    val isSimulatingStress by viewModel.isSimulatingHighStress.collectAsState()

    // Circular press hold tracking variables
    var isPressedState by remember { mutableStateOf(false) }
    var holdPercentage by remember { mutableStateOf(0f) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(isPressedState) {
        if (isPressedState) {
            animate(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec = tween(1500, easing = LinearEasing)
            ) { value, _ ->
                holdPercentage = value
                if (value >= 0.99f) {
                    viewModel.triggerManualSOS()
                    isPressedState = false
                    holdPercentage = 0f
                }
            }
        } else {
            holdPercentage = 0f
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 16.dp)
        ) {
            // Header Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "SafeHer",
                        color = TextPrimaryDark,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "IoT Protection Hub Connected",
                        color = SafetyGreen,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { viewModel.navigateTo("settings") },
                        modifier = Modifier
                            .size(40.dp)
                            .background(SurfaceDarkSecondary, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = TextPrimaryDark,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Central Status Alert Card
                item {
                    val statusColor = if (threatScore > 75) EmergencyRed else if (threatScore > 40) WarningAmber else SafetyGreen
                    val statusText = if (threatScore > 75) "CRITICAL DANGER THRESHOLD" else if (threatScore > 40) "CAUTION ENROUTE" else "STATUS: SAFE AND MONITORED"
                    val descText = if (threatScore > 75) "Urgent biometrics anomalies tracked!" else if (threatScore > 40) "Elevated G forces / stress" else "ESP32 sensors and voice triggers active"

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(statusColor.copy(alpha = 0.12f))
                            .pointerInput(Unit) {
                                detectTapGestures {
                                    // Make status card actionable to show a notification dialog
                                }
                            }
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(20.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .background(statusColor, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = statusText,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = statusColor
                                )
                                Text(
                                    text = descText,
                                    fontSize = 12.sp,
                                    color = TextSecondaryDark,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }
                        }
                    }
                }

                // Grid of Sensor statistics
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Pulse BPM
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(16.dp))
                                .background(SurfaceDark)
                                .clickable { viewModel.navigateTo("monitoring") }
                                .padding(16.dp)
                        ) {
                            Column {
                                Icon(
                                    imageVector = Icons.Default.Favorite,
                                    contentDescription = "Pulse",
                                    tint = EmergencyRed,
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "${sensorState.heartRate} BPM",
                                    color = TextPrimaryDark,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (sensorState.heartRate > 120) "Stress Alert" else "Normal Pulse",
                                    color = if (sensorState.heartRate > 120) WarningAmber else TextSecondaryDark,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }
                        }

                        // Blood Oxygen SpO2
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(16.dp))
                                .background(SurfaceDark)
                                .clickable { viewModel.navigateTo("monitoring") }
                                .padding(16.dp)
                        ) {
                            Column {
                                Icon(
                                    imageVector = Icons.Default.Air,
                                    contentDescription = "SpO2",
                                    tint = ElectricViolet,
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "${sensorState.spo2}% O₂",
                                    color = TextPrimaryDark,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (sensorState.spo2 < 92) "Critically Low" else "Oxygen Optimal",
                                    color = if (sensorState.spo2 < 92) EmergencyRed else TextSecondaryDark,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }
                        }
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Temp
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(16.dp))
                                .background(SurfaceDark)
                                .padding(16.dp)
                        ) {
                            Column {
                                Icon(
                                    imageVector = Icons.Default.Thermostat,
                                    contentDescription = "Temperature",
                                    tint = WarningAmber,
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "${String.format("%.1f", sensorState.temperature)} °C",
                                    color = TextPrimaryDark,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Body Temperature",
                                    color = TextSecondaryDark,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }
                        }

                        // Motion Category
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(16.dp))
                                .background(SurfaceDark)
                                .padding(16.dp)
                        ) {
                            Column {
                                Icon(
                                    imageVector = Icons.Default.DirectionsRun,
                                    contentDescription = "G Force Motion",
                                    tint = SafetyGreen,
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = sensorState.movement,
                                    color = TextPrimaryDark,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                                Text(
                                    text = "IoT Gyro Status",
                                    color = TextSecondaryDark,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }
                        }
                    }
                }

                // Interactive Diagnostic Simulation Controls
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(SurfaceDarkSecondary.copy(alpha = 0.5f))
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Interactive Evaluation Lab",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Simulate events to verify the automatic detection, emergency notifications and cancellation pipelines locally.",
                            color = TextSecondaryDark,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = { viewModel.toggleHighStressSimulation(!isSimulatingStress) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isSimulatingStress) WarningAmber else SurfaceDark
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = if (isSimulatingStress) "Stop High HR" else "Simulate Stress",
                                    fontSize = 12.sp,
                                    color = Color.White
                                )
                            }

                            Button(
                                onClick = { viewModel.triggerVoiceEmergency() },
                                colors = ButtonDefaults.buttonColors(containerColor = SurfaceDark),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = "Simulate Speech",
                                    fontSize = 12.sp,
                                    color = Color.White
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            onClick = { viewModel.triggerFakeCall(true) },
                            colors = ButtonDefaults.buttonColors(containerColor = ElectricViolet),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "🔔 Simulate Incoming Fake Call",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }

                // Giant SOS Press-and-Hold Panel
                item {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp)
                    ) {
                        Text(
                            text = "EMERGENCY OVERRIDE",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(150.dp)
                                .pointerInput(Unit) {
                                    detectTapGestures(
                                        onPress = {
                                            isPressedState = true
                                            try {
                                                awaitRelease()
                                            } finally {
                                                isPressedState = false
                                                holdPercentage = 0f
                                            }
                                        }
                                    )
                                }
                        ) {
                            // Circular background waves
                            val infiniteTransition = rememberInfiniteTransition(label = "sosPulse")
                            val outerPulse by infiniteTransition.animateFloat(
                                initialValue = 150f,
                                targetValue = 180f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(1400, easing = LinearOutSlowInEasing),
                                    repeatMode = RepeatMode.Restart
                                ),
                                label = "sosOuter"
                            )

                            Box(
                                modifier = Modifier
                                    .size(outerPulse.dp)
                                    .background(EmergencyRed.copy(alpha = 0.1f), CircleShape)
                            )

                            // Base holds state Canvas
                            Canvas(modifier = Modifier.size(140.dp)) {
                                drawCircle(
                                    color = SurfaceDarkSecondary,
                                    radius = size.width / 2f
                                )
                                // Hold path sweep
                                drawArc(
                                    color = EmergencyRed,
                                    startAngle = -90f,
                                    sweepAngle = holdPercentage * 360f,
                                    useCenter = false,
                                    style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                                )
                            }

                            // Central Button Inner
                            Box(
                                modifier = Modifier
                                    .size(110.dp)
                                    .background(EmergencyRed, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "SOS",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 26.sp,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "HOLD 2S",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = Color.White.copy(alpha = 0.8f)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (isPressedState) "Keep holding button..." else "Hold button down in absolute emergencies",
                            fontSize = 12.sp,
                            color = if (isPressedState) EmergencyRed else TextSecondaryDark,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

// --- 5. Live Monitoring Screen ---
@Composable
fun LiveMonitoringScreen(viewModel: SafetyViewModel) {
    val sensorState by viewModel.sensorState.collectAsState()
    val hrHistory = viewModel.heartRateHistory

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 16.dp)
        ) {
            // Header Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { viewModel.navigateTo("home") },
                    modifier = Modifier
                        .size(40.dp)
                        .background(SurfaceDarkSecondary, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = TextPrimaryDark
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "ECG & Bio Analytics",
                    color = TextPrimaryDark,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Real-time Canvas Pulse Wave (ECG style)
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(SurfaceDark)
                            .padding(16.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "LIVE SENSOR WAVEFORM",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ElectricViolet
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .background(SafetyGreen, CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Sync: 2s interval",
                                        fontSize = 11.sp,
                                        color = TextSecondaryDark
                                    )
                                }
                            }

                            // ECG Sweep Draw
                            Canvas(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                                    .padding(vertical = 8.dp)
                            ) {
                                if (hrHistory.isNotEmpty()) {
                                    val width = size.width
                                    val height = size.height
                                    val spacing = width / (hrHistory.size - 1)

                                    val points = hrHistory.mapIndexed { idx, bpm ->
                                        // Normalize heart rates (range of 50 to 150 map into height)
                                        val normalizedY =
                                            height - ((bpm - 50f) / 100f * height).coerceIn(0f, height)
                                        Offset(idx * spacing, normalizedY)
                                    }

                                    val path = Path().apply {
                                        moveTo(points.first().x, points.first().y)
                                        points.drop(1).forEach { pt ->
                                            lineTo(pt.x, pt.y)
                                        }
                                    }

                                    drawPath(
                                        path = path,
                                        color = ElectricViolet,
                                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                                    )
                                }
                            }

                            // Live Rate Indicator Foot
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Current: ${sensorState.heartRate} BPM",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = if (sensorState.heartRate > 120) "HIGH CORRELATION ALERT" else "Steady rhythm",
                                    color = if (sensorState.heartRate > 120) WarningAmber else SafetyGreen,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // G-Force Sensor Accel Graph
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(SurfaceDark)
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "6-AXIS G-FORCE VELOCITY IMU",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = SafetyGreen
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        // Progress representation of Accel metrics
                        Text(
                            text = "X-Axis displacement: ${String.format("%.2f", sensorState.accX)}g",
                            color = TextSecondaryDark,
                            fontSize = 12.sp
                        )
                        LinearProgressIndicator(
                            progress = { (sensorState.accX.coerceIn(0f, 5f) / 5f) },
                            color = SafetyGreen,
                            trackColor = SurfaceDarkSecondary,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(4.dp))
                        )

                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Y-Axis displacement: ${String.format("%.2f", sensorState.accY)}g",
                            color = TextSecondaryDark,
                            fontSize = 12.sp
                        )
                        LinearProgressIndicator(
                            progress = { (sensorState.accY.coerceIn(0f, 5f) / 5f) },
                            color = ElectricViolet,
                            trackColor = SurfaceDarkSecondary,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(4.dp))
                        )

                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Z-Axis displacement: ${String.format("%.2f", sensorState.accZ)}g",
                            color = TextSecondaryDark,
                            fontSize = 12.sp
                        )
                        LinearProgressIndicator(
                            progress = { (sensorState.accZ.coerceIn(0f, 5f) / 5f) },
                            color = WarningAmber,
                            trackColor = SurfaceDarkSecondary,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(4.dp))
                        )
                    }
                }

                // Health Insights recommendations List
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    ) {
                        Text(
                            text = "IoT Health Insights & History",
                            color = TextPrimaryDark,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(SurfaceDarkSecondary.copy(alpha = 0.5f))
                                .padding(14.dp)
                        ) {
                            Column {
                                Text(
                                    text = "Blood Oxygen O₂ Levels",
                                    color = TextPrimaryDark,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Your current average SpO2 is ${sensorState.spo2}%. Standard optimal saturated oxygen thresholds range from 95% to 100%. Fall anomalies or trauma might cause temporary dips.",
                                    color = TextSecondaryDark,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                    }
                }

                // Section 3: Firebase Data Stream Logs
                item {
                    val firebaseLogs by viewModel.firebaseLogsFlow.collectAsState()
                    val firebaseConnected by viewModel.firebaseConnectedFlow.collectAsState()

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp, bottom = 24.dp)
                    ) {
                        Text(
                            text = "Cloud Sync Activity Feed",
                            color = TextPrimaryDark,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(SurfaceDark)
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .background(
                                                if (firebaseConnected) SafetyGreen else WarningAmber,
                                                CircleShape
                                            )
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (firebaseConnected) "SECURE CLOUD SYNC ACTIVE" else "LOCAL CACHING - SQLITE SYNC",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (firebaseConnected) SafetyGreen else WarningAmber
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(ElectricViolet.copy(alpha = 0.12f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "LIVE",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = ElectricViolet
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = "DATABASE TRANSACTIONS",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextSecondaryDark,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            // Sync process transaction console logs
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(150.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(SurfaceDarkSecondary)
                                    .padding(8.dp)
                            ) {
                                LazyColumn(modifier = Modifier.fillMaxSize()) {
                                    items(firebaseLogs) { logMsg ->
                                        Text(
                                            text = logMsg,
                                            color = if (logMsg.contains("✔")) SafetyGreen else if (logMsg.contains("❌") || logMsg.contains("⚠")) WarningAmber else TextPrimaryDark,
                                            fontSize = 10.sp,
                                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- 6. Emergency Active Screen ---
@Composable
fun EmergencyScreen(viewModel: SafetyViewModel) {
    val emergencyState by viewModel.emergencyState.collectAsState()
    val threatScore by viewModel.threatScore.collectAsState()
    val activeGuardians by viewModel.guardiansFlow.collectAsState()

    var showPinDialog by remember { mutableStateOf(false) }
    var pinValue by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "siren")
    val sirenColorAlpha by infiniteTransition.animateFloat(
        initialValue = 0.05f,
        targetValue = 0.45f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "sirenColorAlpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark),
        contentAlignment = Alignment.TopCenter
    ) {
        // Blinking red siren overlays
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(EmergencyRed.copy(alpha = sirenColorAlpha))
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 600.dp)
                .fillMaxHeight()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header Top
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 24.dp)
            ) {
                Text(
                    text = "🚨 EMERGENCY ACTIVE 🚨",
                    color = EmergencyRed,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Live GPS & Audio Broadcast Activated",
                    color = TextPrimaryDark,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            // Central State Box
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                when (val current = emergencyState) {
                    is EmergencyState.Countdown -> {
                        Text(
                            text = "TRIGGER REASON: ${current.source.uppercase()}",
                            color = WarningAmber,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        // Visual circular timer count
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.size(140.dp)
                        ) {
                            CircularProgressIndicator(
                                progress = { current.secondsRemaining / 15f },
                                color = WarningAmber,
                                strokeWidth = 8.dp,
                                modifier = Modifier.size(130.dp)
                            )
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "${current.secondsRemaining}",
                                    fontSize = 44.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                                Text(
                                    text = "SECONDS",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextSecondaryDark
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "SafeHer will automatically send encrypted links containing your live coordinates to guardians in ${current.secondsRemaining} seconds.",
                            fontSize = 12.sp,
                            color = TextSecondaryDark,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 14.dp)
                        )
                    }
                    is EmergencyState.ActiveAlert -> {
                        Text(
                            text = "STATUS: EN ROUTE ALERTS BROADCASTED",
                            color = EmergencyRed,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(24.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(SurfaceDark)
                                .padding(16.dp)
                        ) {
                            Column {
                                Text(
                                    text = "SECURE EVIDENCE CAPTURE ENABLED",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = EmergencyRed
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Mic,
                                        contentDescription = "Broadcasting Audio",
                                        tint = EmergencyRed,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "Recording audio files to evidence list...",
                                        fontSize = 12.sp,
                                        color = Color.White
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.ShareLocation,
                                        contentDescription = "Broadcasting GPS",
                                        tint = SafetyGreen,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "Broadcasting coordinates: Real-time update",
                                        fontSize = 12.sp,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                    else -> {
                        Text(text = "Safe Mode Active", color = SafetyGreen)
                    }
                }
            }

            // Contact Lists alerted logs
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "ALERTEE RECIPIENT GUARDIANS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    activeGuardians.take(3).forEach { guardian ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(SurfaceDark)
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Recipient",
                                    tint = TextSecondaryDark,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = guardian.name,
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = if (emergencyState is EmergencyState.Countdown) "Waiting countdown..." else "Alert Message Dispatched ✅",
                                color = if (emergencyState is EmergencyState.Countdown) WarningAmber else SafetyGreen,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Action Buttons
            Column(modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = {
                        showPinDialog = true
                        pinValue = ""
                        pinError = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SafetyGreen),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    Text(
                        text = "I AM SAFE — CANCEL SOS",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))
                Button(
                    onClick = {
                        // Launch native emergency dialer trigger
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SurfaceDark),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    Text(
                        text = "📱 CALL LOCAL HOTLINE (112)",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        // Secure PIN Cancel Alert Dialog
        if (showPinDialog) {
            Dialog(onDismissRequest = { showPinDialog = false }) {
                Box(
                    modifier = Modifier
                        .width(300.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(SurfaceDark)
                        .padding(24.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Enter PIN",
                            tint = ElectricViolet,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Confirm Clearance PIN",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 18.sp
                        )
                        Text(
                            text = "Input secure PIN (Default: 1234) to confirm user is fully safe.",
                            color = TextSecondaryDark,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                        )

                        OutlinedTextField(
                            value = pinValue,
                            onValueChange = { pinValue = it },
                            placeholder = { Text("xxxx") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ElectricViolet,
                                unfocusedBorderColor = SurfaceDarkSecondary,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.width(120.dp)
                        )

                        if (pinError) {
                            Text(
                                text = "Invalid PIN! Correct code is 1234",
                                color = EmergencyRed,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            TextButton(onClick = { showPinDialog = false }) {
                                Text("Go Back", color = TextSecondaryDark)
                            }

                            Button(
                                onClick = {
                                    if (viewModel.cancelEmergency(pinValue)) {
                                        showPinDialog = false
                                    } else {
                                        pinError = true
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = SafetyGreen),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Deactivate Alert", color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- 7. Guardian Management Screen ---
@Composable
fun GuardianScreen(viewModel: SafetyViewModel) {
    val guardians by viewModel.guardiansFlow.collectAsState()
    val searchQuery by viewModel.guardianSearchQuery.collectAsState()
    val searchResults by viewModel.guardianSearchResults.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var nameInput by remember { mutableStateOf("") }
    var phoneInput by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 16.dp)
        ) {
            // Header Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Trusted Circles",
                    color = TextPrimaryDark,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )

                IconButton(
                    onClick = { showAddDialog = true },
                    modifier = Modifier
                        .size(40.dp)
                        .background(ElectricViolet, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Guardian",
                        tint = Color.White
                    )
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Warning Banner
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(SurfaceDark)
                            .padding(14.dp)
                    ) {
                        Text(
                            text = "Your guardians automatically receive SMS templates with maps whenever SafeHer's IoT threat score crosses 75%. Pending requests require action.",
                            fontSize = 12.sp,
                            color = TextSecondaryDark
                        )
                    }
                }

                if (guardians.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 48.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.PeopleOutline,
                                contentDescription = "No Guardians",
                                tint = TextSecondaryDark,
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = "Zero Guardians Registered",
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Add urgent contact people to secure coverage.",
                                color = TextSecondaryDark,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                } else {
                    items(guardians) { guardian ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(SurfaceDark)
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = guardian.name,
                                    color = Color.White,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = guardian.phone,
                                    color = TextSecondaryDark,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(top = 2.dp)
                                )

                                // Status Indicator
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(top = 8.dp)
                                ) {
                                    val isAct = guardian.status == "Active"
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .background(if (isAct) SafetyGreen else WarningAmber, CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = guardian.status,
                                        color = if (isAct) SafetyGreen else WarningAmber,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (guardian.status == "Pending") {
                                    IconButton(
                                        onClick = { viewModel.acceptRequest(guardian) },
                                        modifier = Modifier
                                            .size(36.dp)
                                            .background(SafetyGreen.copy(alpha = 0.15f), CircleShape)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Accept",
                                            tint = SafetyGreen
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                }

                                IconButton(
                                    onClick = { viewModel.removeGuardian(guardian) },
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(EmergencyRed.copy(alpha = 0.15f), CircleShape)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Remove",
                                        tint = EmergencyRed,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Add Contact Pop-up Dialog
        if (showAddDialog) {
            Dialog(onDismissRequest = {
                showAddDialog = false
                viewModel.updateGuardianSearchQuery("")
            }) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(SurfaceDark)
                        .padding(20.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Add Trusted Guardian",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 18.sp
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        // Live User Directory Search
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { viewModel.updateGuardianSearchQuery(it) },
                            label = { Text("Search App User Base") },
                            placeholder = { Text("Type name, email or number", color = TextSecondaryDark) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Search directory",
                                    tint = ElectricViolet
                                )
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ElectricViolet,
                                unfocusedBorderColor = SurfaceDarkSecondary,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Matches Dropdown/List Panel
                        if (searchQuery.isNotBlank() && searchResults.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 130.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(SurfaceDarkSecondary)
                                    .padding(8.dp)
                            ) {
                                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                                    searchResults.forEach { item ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(8.dp))
                                                .clickable {
                                                    nameInput = item.name
                                                    phoneInput = item.phone
                                                }
                                                .padding(horizontal = 8.dp, vertical = 6.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text(
                                                    text = item.name,
                                                    color = Color.White,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Text(
                                                    text = item.email,
                                                    color = TextSecondaryDark,
                                                    fontSize = 10.sp
                                                )
                                            }
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(ElectricViolet.copy(alpha = 0.2f))
                                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                            ) {
                                                Text(
                                                    text = "Select",
                                                    color = ElectricViolet,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        OutlinedTextField(
                            value = nameInput,
                            onValueChange = { nameInput = it },
                            label = { Text("Selected Full Name") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ElectricViolet,
                                unfocusedBorderColor = SurfaceDarkSecondary,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = phoneInput,
                            onValueChange = { phoneInput = it },
                            label = { Text("Contact Phone Number") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ElectricViolet,
                                unfocusedBorderColor = SurfaceDarkSecondary,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            TextButton(onClick = {
                                showAddDialog = false
                                viewModel.updateGuardianSearchQuery("")
                            }) {
                                Text("Cancel", color = TextSecondaryDark)
                            }

                            Button(
                                onClick = {
                                    if (nameInput.isNotBlank() && phoneInput.isNotBlank()) {
                                        viewModel.addGuardian(nameInput, phoneInput)
                                        nameInput = ""
                                        phoneInput = ""
                                        viewModel.updateGuardianSearchQuery("")
                                        showAddDialog = false
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = ElectricViolet),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Send Invitation", color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- 8. Live Location Tracking Screen ---
@Composable
fun LiveLocationScreen(viewModel: SafetyViewModel) {
    val lat by viewModel.latitude.collectAsState()
    val lng by viewModel.longitude.collectAsState()
    val pathHistory = viewModel.routeHistory

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 16.dp)
        ) {
            // Header Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Live GPS Tracking Map",
                    color = TextPrimaryDark,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Custom Styled Vector Map canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .padding(horizontal = 20.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(SurfaceDark)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height

                    // Draw abstract grid lines representing streets
                    val gridPaint = SurfaceDarkSecondary
                    for (i in 1..4) {
                        drawLine(
                            color = gridPaint,
                            start = Offset(i * (w / 5), 0f),
                            end = Offset(i * (w / 5), h),
                            strokeWidth = 2.dp.toPx()
                        )
                        drawLine(
                            color = gridPaint,
                            start = Offset(0f, i * (h / 5)),
                            end = Offset(w, i * (h / 5)),
                            strokeWidth = 2.dp.toPx()
                        )
                    }

                    // Draw simulated escape path history traces
                    val pathColor = ElectricViolet
                    if (pathHistory.size > 1) {
                        val points = pathHistory.mapIndexed { idx, pair ->
                            // Map coordinate deviations slightly for visual motion
                            val xOffset = w / 2f + (idx * 15f) - (pathHistory.size * 5f)
                            val yOffset = h / 2f + (idx * idx * 0.4f) - (pathHistory.size * 2f)
                            Offset(xOffset.coerceIn(0f, w), yOffset.coerceIn(0f, h))
                        }

                        val trackPath = Path().apply {
                            moveTo(points.first().x, points.first().y)
                            points.drop(1).forEach { pt ->
                                lineTo(pt.x, pt.y)
                            }
                        }

                        drawPath(
                            path = trackPath,
                            color = pathColor,
                            style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }

                    // Current position circle marker
                    drawCircle(
                        color = ElectricViolet.copy(alpha = 0.25f),
                        radius = 28.dp.toPx(),
                        center = Offset(w / 2f, h / 2f)
                    )
                    drawCircle(
                        color = ElectricViolet,
                        radius = 8.dp.toPx(),
                        center = Offset(w / 2f, h / 2f)
                    )
                }

                // GPS coordinate HUD Tag float
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp)
                        .background(BackgroundDark.copy(alpha = 0.8f), RoundedCornerShape(8.dp))
                        .padding(8.dp)
                ) {
                    Text(
                        text = "Precise 3m Accuracy",
                        fontSize = 11.sp,
                        color = SafetyGreen,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Geographic Details HUD
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(SurfaceDark, RoundedCornerShape(12.dp))
                            .padding(14.dp)
                    ) {
                        Column {
                            Text(text = "LATITUDE", fontSize = 11.sp, color = TextSecondaryDark)
                            Text(
                                text = String.format("%.5f", lat),
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(SurfaceDark, RoundedCornerShape(12.dp))
                            .padding(14.dp)
                    ) {
                        Column {
                            Text(text = "LONGITUDE", fontSize = 11.sp, color = TextSecondaryDark)
                            Text(
                                text = String.format("%.5f", lng),
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }
                }

                // Street Location
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SurfaceDark, RoundedCornerShape(12.dp))
                        .padding(14.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = "Address",
                            tint = SafetyGreen
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(text = "ESTIMATED STREET RESOLUTION", fontSize = 11.sp, color = TextSecondaryDark)
                            Text(
                                text = "Anna Nagar East Main St, Chennai, IN",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }
                }

                // Simulated Share CTA
                Button(
                    onClick = {
                        // Triggers template copy helper
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricViolet),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    Icon(imageVector = Icons.Default.Share, contentDescription = "Share", tint = Color.White)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Share Mock Live Map Link",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

// --- 9. Evidence & Recordings Screen ---
@Composable
fun EvidenceScreen(viewModel: SafetyViewModel) {
    val recordings by viewModel.recordingsFlow.collectAsState()
    val alarmLogs by viewModel.logsFlow.collectAsState()

    var activeTab by remember { mutableStateOf("Recordings") }
    var playingFileId by remember { mutableStateOf<Int?>(null) }
    var wavePercentage by remember { mutableStateOf(0f) }

    LaunchedEffect(playingFileId) {
        if (playingFileId != null) {
            wavePercentage = 0f
            while (playingFileId != null) {
                delay(100)
                wavePercentage = if (wavePercentage >= 1.0f) 0f else wavePercentage + 0.05f
            }
        } else {
            wavePercentage = 0f
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 16.dp)
        ) {
            // Header Top Bar - Modernized with dedicated SQLite Console Selection
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 20.dp)
            ) {
                Text(
                    text = "Log Book & Evidence",
                    color = TextPrimaryDark,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(14.dp))

                // High density mobile responsive three-column tab bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SurfaceDark, RoundedCornerShape(10.dp))
                        .padding(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (activeTab == "Recordings") ElectricViolet else Color.Transparent)
                            .clickable { activeTab = "Recordings" }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Audio Clips",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (activeTab == "Recordings") Color.White else TextSecondaryDark
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (activeTab == "Alarms") ElectricViolet else Color.Transparent)
                            .clickable { activeTab = "Alarms" }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Threat Logs",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (activeTab == "Alarms") Color.White else TextSecondaryDark
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (activeTab == "Database") ElectricViolet else Color.Transparent)
                            .clickable { activeTab = "Database" }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "local SQLite",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (activeTab == "Database") Color.White else TextSecondaryDark
                        )
                    }
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (activeTab == "Recordings") {
                    if (recordings.isEmpty()) {
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 64.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MicOff,
                                    contentDescription = "No Records",
                                    tint = TextSecondaryDark,
                                    modifier = Modifier.size(64.dp)
                                )
                                Spacer(modifier = Modifier.height(14.dp))
                                Text(
                                    text = "No audio evidence logged yet.",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Recordings will appear when emergency launches.",
                                    color = TextSecondaryDark,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                    } else {
                        items(recordings) { file ->
                            val isPlaying = playingFileId == file.id
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(SurfaceDark)
                                    .padding(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.LibraryMusic,
                                            contentDescription = "Audio track",
                                            tint = ElectricViolet
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = file.fileName,
                                                color = Color.White,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "Size: ${String.format("%.2f", file.sizeBytes / 1024f / 1024f)} MB | Dur: ${file.duration}",
                                                color = TextSecondaryDark,
                                                fontSize = 11.sp,
                                                modifier = Modifier.padding(top = 2.dp)
                                            )
                                        }
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        IconButton(
                                            onClick = {
                                                playingFileId = if (isPlaying) null else file.id
                                            },
                                            modifier = Modifier
                                                .size(36.dp)
                                                .background(ElectricViolet.copy(alpha = 0.15f), CircleShape)
                                        ) {
                                            Icon(
                                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                                contentDescription = "Play/pause",
                                                tint = ElectricViolet
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        IconButton(
                                            onClick = { viewModel.deleteRecording(file) },
                                            modifier = Modifier
                                                .size(36.dp)
                                                .background(EmergencyRed.copy(alpha = 0.15f), CircleShape)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Delete track",
                                                tint = EmergencyRed,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }

                                // Interactive Audio Waveform Slider when active
                                if (isPlaying) {
                                    Spacer(modifier = Modifier.height(14.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(text = "0:00", fontSize = 11.sp, color = TextSecondaryDark)
                                        Spacer(modifier = Modifier.width(8.dp))

                                        // Fake wave design
                                        Canvas(
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(18.dp)
                                        ) {
                                            val parts = 35
                                            val partW = size.width / parts
                                            for (ip in 0 until parts) {
                                                val hPercent = if (ip < (wavePercentage * parts)) {
                                                    0.2f + (ip % 3) * 0.25f
                                                } else {
                                                    0.15f + (ip % 2) * 0.05f
                                                }
                                                val lineH = size.height * hPercent
                                                val startX = ip * partW + partW / 2f
                                                drawLine(
                                                    color = if (ip < (wavePercentage * parts)) ElectricViolet else SurfaceDarkSecondary,
                                                    start = Offset(startX, size.height / 2f - lineH / 2f),
                                                    end = Offset(startX, size.height / 2f + lineH / 2f),
                                                    strokeWidth = 3.dp.toPx(),
                                                    cap = StrokeCap.Round
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(text = file.duration, fontSize = 11.sp, color = TextSecondaryDark)
                                    }
                                }
                            }
                        }
                    }
                } else if (activeTab == "Alarms") {
                    // Alarms Log History Tab
                    if (alarmLogs.isEmpty()) {
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 64.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.NotificationsOff,
                                    contentDescription = "No Logs",
                                    tint = TextSecondaryDark,
                                    modifier = Modifier.size(64.dp)
                                )
                                Spacer(modifier = Modifier.height(14.dp))
                                Text(
                                    text = "Clean Safety Record",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Zero automated emergency threats logged.",
                                    color = TextSecondaryDark,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                    } else {
                        item {
                            Button(
                                onClick = { viewModel.clearLogs() },
                                colors = ButtonDefaults.buttonColors(containerColor = SurfaceDark),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Clear Alarm Logs history", color = EmergencyRed, fontSize = 13.sp)
                            }
                        }

                        items(alarmLogs) { log ->
                            val isCleard = log.outcome.contains("Cancel")
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(SurfaceDark)
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (isCleard) Icons.Default.EventBusy else Icons.Default.ReportProblem,
                                        contentDescription = "Alert status icon",
                                        tint = if (isCleard) SafetyGreen else EmergencyRed,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = log.type,
                                            color = Color.White,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "Threat Score: ${log.score}/100",
                                            color = TextSecondaryDark,
                                            fontSize = 11.sp,
                                            modifier = Modifier.padding(top = 2.dp)
                                        )
                                    }
                                }

                                Text(
                                    text = log.outcome,
                                    color = if (isCleard) SafetyGreen else EmergencyRed,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                } else {
                    // Local SQLite / Room Database Center Tab - Real, Live & Fully Functional
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(SurfaceDark)
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "SQLITE METADATA ENGINE",
                                color = ElectricViolet,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("ORM Framework:", color = TextSecondaryDark, fontSize = 12.sp)
                                Text("Room Persistence API", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Database File:", color = TextSecondaryDark, fontSize = 12.sp)
                                Text("safety_database", color = Color.White, fontSize = 12.sp, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("DB Version:", color = TextSecondaryDark, fontSize = 12.sp)
                                Text("1.0 (SQLite 3.x backend)", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("SQLite Integrity:", color = TextSecondaryDark, fontSize = 12.sp)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(modifier = Modifier.size(6.dp).background(SafetyGreen, CircleShape))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("VERIFIED GOOD", color = SafetyGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(SurfaceDark)
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "LIVE TABLE SCHEMA METRICS",
                                color = SafetyGreen,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(14.dp))

                            // Table 1: Guardians Table Info
                            val guardiansCount = viewModel.guardiansFlow.collectAsState().value.size
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Table: guardians", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    Text("Columns: id, name, phone, status", color = TextSecondaryDark, fontSize = 10.sp)
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(SurfaceDarkSecondary)
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text("$guardiansCount rows", color = SafetyGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(SurfaceDarkSecondary))
                            Spacer(modifier = Modifier.height(12.dp))

                            // Table 2: Emergency Logs Info
                            val emergencyLogsCount = alarmLogs.size
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Table: emergency_logs", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    Text("Columns: id, type, score, timestamp, outcome", color = TextSecondaryDark, fontSize = 10.sp)
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(SurfaceDarkSecondary)
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text("$emergencyLogsCount rows", color = SafetyGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(SurfaceDarkSecondary))
                            Spacer(modifier = Modifier.height(12.dp))

                            // Table 3: Recordings Info
                            val recordingsCount = recordings.size
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Table: recordings", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    Text("Columns: id, fileName, duration, timestamp, sizeBytes", color = TextSecondaryDark, fontSize = 10.sp)
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(SurfaceDarkSecondary)
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text("$recordingsCount rows", color = SafetyGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(SurfaceDarkSecondary))
                            Spacer(modifier = Modifier.height(12.dp))

                            // Table 4: Sensor Telemetry Info
                            val telemetryCount = viewModel.sensorTelemetryFlow.collectAsState().value.size
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Table: sensor_telemetry", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    Text("Columns: id, username, heartRate, spo2, temperature, accX/Y/Z, movement, battery, timestamp", color = TextSecondaryDark, fontSize = 10.sp)
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(SurfaceDarkSecondary)
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text("$telemetryCount rows", color = SafetyGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    item {
                        val telemetryList = viewModel.sensorTelemetryFlow.collectAsState().value
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(SurfaceDark)
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "SENSOR DATA STORE CONSOLE (ALL USERS)",
                                    color = ElectricViolet,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(ElectricViolet.copy(alpha = 0.2f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text("CLOUDSYNC ACTIVE", color = ElectricViolet, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            
                            if (telemetryList.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 20.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("No sensor data stored yet. Logging starts in 1.5s...", color = TextSecondaryDark, fontSize = 11.sp)
                                }
                            } else {
                                Text(
                                    text = "Showing latest 10 of ${telemetryList.size} telemetry records captured locally & synced to Firebase (/users/{user}/sensor_history/...):",
                                    color = TextSecondaryDark,
                                    fontSize = 10.sp,
                                    modifier = Modifier.padding(bottom = 12.dp)
                                )
                                
                                telemetryList.take(10).forEach { item ->
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(SurfaceDarkSecondary)
                                            .padding(10.dp)
                                    ) {
                                        Column {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(6.dp)
                                                            .background(if (item.movement.contains("Anomaly")) EmergencyRed else SafetyGreen, CircleShape)
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(
                                                        text = item.username,
                                                        color = Color.White,
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.SemiBold
                                                    )
                                                }
                                                val sdf = remember { java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.US) }
                                                Text(
                                                    text = sdf.format(java.util.Date(item.timestamp)),
                                                    color = TextSecondaryDark,
                                                    fontSize = 10.sp
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(6.dp))
                                            
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Column {
                                                    Text("BIOMETRICS", color = TextSecondaryDark, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                                    Text("Heart Rate: ${item.heartRate} bpm, SpO2: ${item.spo2}%, Temp: ${item.temperature}°C", color = Color.White, fontSize = 10.sp)
                                                }
                                                Column(horizontalAlignment = Alignment.End) {
                                                    Text("IMU ACCELEROMETER", color = TextSecondaryDark, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                                    Text("X:${String.format(java.util.Locale.US, "%.2f", item.accX)} Y:${String.format(java.util.Locale.US, "%.2f", item.accY)} Z:${String.format(java.util.Locale.US, "%.2f", item.accZ)} G", color = Color.White, fontSize = 10.sp)
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "MOTION: ${item.movement.uppercase()}",
                                                    color = if (item.movement.contains("Anomaly")) EmergencyRed else WarningAmber,
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Text(
                                                    text = "🔋 Battery: ${item.batteryLevel}%",
                                                    color = TextSecondaryDark,
                                                    fontSize = 9.sp
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(SurfaceDark)
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "MASTER DATABASE CONSOLE CONTROLS",
                                color = WarningAmber,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(14.dp))

                            // Button 1: Factory Reset / Seed default tables
                            Button(
                                onClick = { viewModel.factorySeedDatabase() },
                                colors = ButtonDefaults.buttonColors(containerColor = ElectricViolet),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(imageVector = Icons.Default.Refresh, contentDescription = "Seed", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Factory Reset & Seed DB Data", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Button 2: Wipe all database tables
                            Button(
                                onClick = { viewModel.wipeDatabase() },
                                colors = ButtonDefaults.buttonColors(containerColor = EmergencyRed.copy(alpha = 0.2f)),
                                border = BorderStroke(1.dp, EmergencyRed),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(imageVector = Icons.Default.Delete, contentDescription = "Wipe", tint = EmergencyRed, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Wipe Local SQLite Database", color = EmergencyRed, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Info block about SQLite file persistence
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(SurfaceDarkSecondary)
                                    .padding(10.dp)
                            ) {
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(imageVector = Icons.Default.Security, contentDescription = "Security Info", tint = WarningAmber, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("ACID Compliance Active", color = WarningAmber, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "All SQLite tables use WAL (Write-Ahead Logging) for atomic crash recovery. State synchronization to active observers utilizes flow streams.",
                                        color = TextSecondaryDark,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- 10. Settings Configuration Screen ---
@Composable
fun SettingsScreen(viewModel: SafetyViewModel) {
    val isVoiceEnabled by viewModel.isVoiceTriggerEnabled.collectAsState()
    val triggerPhrase by viewModel.voiceTriggerPhrase.collectAsState()
    val voiceStatus by viewModel.voiceStatusFlow.collectAsState()
    val lastRecognizedText by viewModel.lastRecognizedTextFlow.collectAsState()
    val isVoiceActive by viewModel.isVoiceActiveFlow.collectAsState()

    var customPhraseInput by remember { mutableStateOf(triggerPhrase) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 16.dp)
        ) {
            // Header Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { viewModel.navigateTo("home") },
                    modifier = Modifier
                        .size(40.dp)
                        .background(SurfaceDarkSecondary, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = TextPrimaryDark
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "SafeHer Preferences",
                    color = TextPrimaryDark,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Section 1: Voice Active Trigger
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(SurfaceDark)
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "VOICE TRIGGER ACTIVATION",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = ElectricViolet
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Enable Background Listening",
                                    color = TextPrimaryDark,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Listen for keyword threats matching custom voice sentences even in sleep.",
                                    color = TextSecondaryDark,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }
                            Switch(
                                checked = isVoiceEnabled,
                                onCheckedChange = { viewModel.toggleVoiceTrigger(it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = ElectricViolet,
                                    checkedTrackColor = ElectricViolet.copy(alpha = 0.5f)
                                )
                            )
                        }

                        if (isVoiceEnabled) {
                            Spacer(modifier = Modifier.height(14.dp))
                            OutlinedTextField(
                                value = customPhraseInput,
                                onValueChange = {
                                    customPhraseInput = it
                                    viewModel.updateVoiceTriggerPhrase(it)
                                },
                                label = { Text("Custom Trigger Word Sentence") },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = ElectricViolet,
                                    unfocusedBorderColor = SurfaceDarkSecondary,
                                    focusedTextColor = TextPrimaryDark,
                                    unfocusedTextColor = TextPrimaryDark
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            // Audio Signal Stream diagnostics card
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(SurfaceDarkSecondary)
                                    .padding(12.dp)
                            ) {
                                Column {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = "On-Device Speech Recognizer",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = ElectricViolet
                                        )
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(8.dp)
                                                    .background(
                                                        if (isVoiceActive) SafetyGreen else TextSecondaryDark,
                                                        CircleShape
                                                    )
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = if (isVoiceActive) "MIC ACTIVE" else "PAUSED",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isVoiceActive) SafetyGreen else TextSecondaryDark
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = voiceStatus,
                                        fontSize = 12.sp,
                                        color = TextPrimaryDark,
                                        fontWeight = FontWeight.Medium
                                    )
                                    if (lastRecognizedText.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Last recognized: \"$lastRecognizedText\"",
                                            fontSize = 11.sp,
                                            color = TextSecondaryDark
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Interactive Trigger Phrases Button list
                            Text(
                                text = "TAP TO TEST SPEECH VOICE SIGNAL",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextSecondaryDark,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf("Help me", "Emergency", "Save me").forEach { term ->
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(ElectricViolet.copy(alpha = 0.12f))
                                            .clickable { viewModel.simulateVoiceCommand(term) }
                                            .padding(vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "\"$term\"",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = ElectricViolet
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Section 2: Medical Identity Info
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(SurfaceDark)
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "UHR IDENTITY & MEDICAL FILE",
                            color = SafetyGreen,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "Blood Classification group: B +",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Medical Allergy status: Penicillin, severe dust allergies",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Emergency Dial code hotline: 112 / Global police",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // General Sign-out option
                item {
                    Button(
                        onClick = { viewModel.logout() },
                        colors = ButtonDefaults.buttonColors(containerColor = EmergencyRed.copy(alpha = 0.15f)),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Log Out SafeHer Account", color = EmergencyRed, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
