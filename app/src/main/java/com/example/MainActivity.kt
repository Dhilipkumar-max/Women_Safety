package com.example

import android.Manifest
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.background
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.*
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.SafetyViewModel

class MainActivity : ComponentActivity() {

  private var gpsLocationListener: android.location.LocationListener? = null
  private var networkLocationListener: android.location.LocationListener? = null

  private val requestPermissionLauncher = registerForActivityResult(
    ActivityResultContracts.RequestMultiplePermissions()
  ) { permissions ->
    val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
    val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
    val recordGranted = permissions[Manifest.permission.RECORD_AUDIO] ?: false
    try {
      val viewModel = androidx.lifecycle.ViewModelProvider(this)[SafetyViewModel::class.java]
      val route = viewModel.currentRoute.value
      if (route != "splash" && route != "onboarding" && route != "login") {
        if (fineGranted || coarseGranted) {
          startRealLocationUpdates(viewModel)
        }
        if (recordGranted) {
          viewModel.onRecordAudioPermissionGranted()
        }
      }
    } catch (e: Exception) {
      android.util.Log.e("MainActivity", "Failed to start services post permission callback: ${e.message}")
    }
  }

  override fun onResume() {
    super.onResume()
    try {
      val viewModel = androidx.lifecycle.ViewModelProvider(this)[SafetyViewModel::class.java]
      val route = viewModel.currentRoute.value
      if (route != "splash" && route != "onboarding" && route != "login") {
        viewModel.onActivityResumed()
        startRealLocationUpdates(viewModel)
      }
    } catch (e: Exception) {
      android.util.Log.e("MainActivity", "Failed in onResume lifecycle: ${e.message}")
    }
  }

  override fun onPause() {
    super.onPause()
    try {
      val viewModel = androidx.lifecycle.ViewModelProvider(this)[SafetyViewModel::class.java]
      viewModel.onActivityPaused()
      stopRealLocationUpdates()
    } catch (e: Exception) {
      android.util.Log.e("MainActivity", "Failed in onPause lifecycle: ${e.message}")
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    // Configure high-importance Lock Screen overlay capability
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O_MR1) {
      setShowWhenLocked(true)
      setTurnScreenOn(true)
    } else {
      @Suppress("DEPRECATION")
      window.addFlags(
        android.view.WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
        android.view.WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
      )
    }

    // Dynamic list request including lockscreen recording & push status permissions
    val permissionsArray = mutableListOf(
      Manifest.permission.RECORD_AUDIO,
      Manifest.permission.ACCESS_FINE_LOCATION,
      Manifest.permission.ACCESS_COARSE_LOCATION
    )
    if (android.os.Build.VERSION.SDK_INT >= 33) {
      permissionsArray.add(Manifest.permission.POST_NOTIFICATIONS)
    }

    requestPermissionLauncher.launch(permissionsArray.toTypedArray())

    setContent {
      MyApplicationTheme {
        val viewModel: SafetyViewModel = viewModel()
        val currentRoute by viewModel.currentRoute.collectAsState()
        val isFakeCallActive by viewModel.isFakeCallActive.collectAsState()

        LaunchedEffect(currentRoute) {
          if (currentRoute != "splash" && currentRoute != "onboarding" && currentRoute != "login") {
            startRealLocationUpdates(viewModel)
            if (viewModel.isVoiceTriggerEnabled.value) {
              viewModel.onRecordAudioPermissionGranted()
            }
          } else {
            stopRealLocationUpdates()
            viewModel.onActivityPaused()
          }
        }

        Box(modifier = Modifier.fillMaxSize()) {
          // Bottom Navigation-Scaffolded Viewport for primary flows
          if (currentRoute == "splash") {
            SplashScreen(viewModel = viewModel)
          } else if (currentRoute == "onboarding") {
            OnboardingScreen(viewModel = viewModel)
          } else if (currentRoute == "login") {
            LoginAndSignupScreen(viewModel = viewModel)
          } else if (currentRoute == "emergency") {
            EmergencyScreen(viewModel = viewModel)
          } else {
            Scaffold(
              modifier = Modifier.fillMaxSize(),
              bottomBar = {
                Box(
                  modifier = Modifier
                    .fillMaxWidth()
                    .background(com.example.ui.theme.SurfaceDark),
                  contentAlignment = Alignment.Center
                ) {
                  Box(
                    modifier = Modifier
                      .fillMaxWidth()
                      .widthIn(max = 600.dp)
                  ) {
                    MainBottomNavigationBar(
                      activeScreen = currentRoute,
                      navigate = { target -> viewModel.navigateTo(target) }
                    )
                  }
                }
              }
            ) { innerPadding ->
              Box(
                modifier = Modifier
                  .fillMaxSize()
                  .background(com.example.ui.theme.BackgroundDark)
                  .padding(innerPadding),
                contentAlignment = Alignment.TopCenter
              ) {
                Box(
                  modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 600.dp)
                ) {
                  when (currentRoute) {
                    "home" -> HomeDashboardScreen(viewModel = viewModel)
                    "monitoring" -> LiveMonitoringScreen(viewModel = viewModel)
                    "guardians" -> GuardianScreen(viewModel = viewModel)
                    "location" -> LiveLocationScreen(viewModel = viewModel)
                    "evidence" -> EvidenceScreen(viewModel = viewModel)
                    "settings" -> SettingsScreen(viewModel = viewModel)
                  }
                }
              }
            }
          }

          // Full Screen Overlays (Fake call, priority interruptions)
          if (isFakeCallActive) {
            FakeCallOverlay(viewModel = viewModel)
          }
        }
      }
    }
  }

  private fun stopRealLocationUpdates() {
    val locationManager = getSystemService(Context.LOCATION_SERVICE) as android.location.LocationManager
    gpsLocationListener?.let { listener ->
      try {
        locationManager.removeUpdates(listener)
      } catch (e: Exception) {
        android.util.Log.e("MainActivity", "Failed to remove GPS updates: ${e.message}")
      }
      gpsLocationListener = null
    }
    networkLocationListener?.let { listener ->
      try {
        locationManager.removeUpdates(listener)
      } catch (e: Exception) {
        android.util.Log.e("MainActivity", "Failed to remove Network updates: ${e.message}")
      }
      networkLocationListener = null
    }
  }

  private fun startRealLocationUpdates(viewModel: SafetyViewModel) {
    val hasFine = androidx.core.app.ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED
    val hasCoarse = androidx.core.app.ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED
    
    if (!hasFine && !hasCoarse) {
      android.util.Log.w("MainActivity", "Permissions not granted to start location updates")
      return
    }
    stopRealLocationUpdates()
    val locationManager = getSystemService(Context.LOCATION_SERVICE) as android.location.LocationManager
    try {
      val enabledProviders = locationManager.getProviders(true) ?: emptyList()
      
      if (hasFine && enabledProviders.contains(android.location.LocationManager.GPS_PROVIDER)) {
        val gpsListener = object : android.location.LocationListener {
          override fun onLocationChanged(location: android.location.Location) {
            viewModel.updateLocation(location.latitude, location.longitude)
          }
          override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
          override fun onProviderEnabled(provider: String) {}
          override fun onProviderDisabled(provider: String) {}
        }
        gpsLocationListener = gpsListener
        locationManager.requestLocationUpdates(android.location.LocationManager.GPS_PROVIDER, 3000L, 2f, gpsListener)
      }
      
      if (hasCoarse && enabledProviders.contains(android.location.LocationManager.NETWORK_PROVIDER)) {
        val netListener = object : android.location.LocationListener {
          override fun onLocationChanged(location: android.location.Location) {
            viewModel.updateLocation(location.latitude, location.longitude)
          }
          override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
          override fun onProviderEnabled(provider: String) {}
          override fun onProviderDisabled(provider: String) {}
        }
        networkLocationListener = netListener
        locationManager.requestLocationUpdates(android.location.LocationManager.NETWORK_PROVIDER, 3000L, 2f, netListener)
      }

      val lastGps = if (hasFine && enabledProviders.contains(android.location.LocationManager.GPS_PROVIDER)) {
        locationManager.getLastKnownLocation(android.location.LocationManager.GPS_PROVIDER)
      } else null
      
      val lastNet = if (hasCoarse && enabledProviders.contains(android.location.LocationManager.NETWORK_PROVIDER)) {
        locationManager.getLastKnownLocation(android.location.LocationManager.NETWORK_PROVIDER)
      } else null
      
      val best = lastGps ?: lastNet
      if (best != null) {
        viewModel.updateLocation(best.latitude, best.longitude)
      }
    } catch (e: Exception) {
      android.util.Log.e("MainActivity", "Real location tracking failed to start gracefully: ${e.message}")
    }
  }
}
