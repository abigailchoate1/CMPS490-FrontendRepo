package com.CMPS490.weathertracker

import android.Manifest
import android.annotation.SuppressLint
import android.location.Location
import android.os.Bundle
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.CMPS490.weathertracker.ui.theme.WeatherTrackerTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            WeatherTrackerTheme {
                MapScreen()
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MapScreen() {

    val context = LocalContext.current

    val permissionState = rememberMultiplePermissionsState(
        listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.POST_NOTIFICATIONS
        )
    )

    LaunchedEffect(Unit) {
        permissionState.launchMultiplePermissionRequest()
    }

    val cameraPositionState = rememberCameraPositionState()
    var hazardZone by remember { mutableStateOf<HazardZone?>(null) }
    var wasInside by remember { mutableStateOf(false) }

    @SuppressLint("MissingPermission")
    LaunchedEffect(permissionState.allPermissionsGranted) {

        if (!permissionState.allPermissionsGranted) return@LaunchedEffect

        val fusedLocationClient =
            LocationServices.getFusedLocationProviderClient(context)

        // 🔴 Hardcoded storm zone (change for testing)
        val stormZone = HazardZone(
            center = LatLng(30.2241, -92.0198), // Example location
            radiusMeters = 2000.0,
            severity = "WARNING"
        )

        hazardZone = stormZone

        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            3000L // check every 3 seconds
        ).build()

        val locationCallback = object : LocationCallback() {

            override fun onLocationResult(result: LocationResult) {

                val location = result.lastLocation ?: return

                val userLatLng = LatLng(
                    location.latitude,
                    location.longitude
                )

                // Move camera to user
                cameraPositionState.position =
                    CameraPosition.fromLatLngZoom(userLatLng, 12f)

                val isInside = isInsideZone(userLatLng, stormZone)

                // Trigger only when crossing into zone
                if (isInside && !wasInside) {
                    NotificationHelper.showStormNotification(context)
                }

                wasInside = isInside
            }
        }

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(
                isMyLocationEnabled = permissionState.allPermissionsGranted,
                mapType = MapType.SATELLITE
            ),
            uiSettings = MapUiSettings(
                myLocationButtonEnabled = true
            )
        ) {
            hazardZone?.let { zone ->
                Circle(
                    center = zone.center,
                    radius = zone.radiusMeters,
                    strokeColor = Color.Red,
                    strokeWidth = 4f,
                    fillColor = Color.Red.copy(alpha = 0.3f)
                )
            }
        }
    }
}

/**
 * Distance calculation using real earth meters
 */
fun isInsideZone(user: LatLng, zone: HazardZone): Boolean {

    val results = FloatArray(1)

    Location.distanceBetween(
        user.latitude,
        user.longitude,
        zone.center.latitude,
        zone.center.longitude,
        results
    )

    return results[0] <= zone.radiusMeters
}