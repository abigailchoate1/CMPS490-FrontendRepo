package com.CMPS490.weathertracker

import android.Manifest
import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.CMPS490.weathertracker.ui.theme.WeatherTrackerTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.Circle
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalPermissionsApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            WeatherTrackerTheme {
                val navController = rememberNavController()
                val context = LocalContext.current
                val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
                val backendApi = remember { BackendPredictorApi(context) }
                var userLocation by remember { mutableStateOf<LatLng?>(null) }
                var backendPrediction by remember { mutableStateOf<BackendPredictionResult?>(null) }
                var backendAlert by remember { mutableStateOf<WeatherAlertUiModel?>(null) }

                val locationPermissionState = rememberMultiplePermissionsState(
                    permissions = listOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )

                LaunchedEffect(Unit) {
                    locationPermissionState.launchMultiplePermissionRequest()
                }

                @SuppressLint("MissingPermission")
                LaunchedEffect(locationPermissionState.allPermissionsGranted) {
                    if (locationPermissionState.allPermissionsGranted) {
                        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                            if (location != null) {
                                userLocation = LatLng(location.latitude, location.longitude)
                            }
                        }
                    }
                }
                
                NavHost(navController = navController, startDestination = "weather_overview") {
                    composable("weather_overview") {
                        val mockCurrent = CurrentWeatherUiModel(
                            location = "Baton Rouge, Louisiana",
                            dayDate = "Live Demo",
                            temperature = 78,
                            condition = "Partly Cloudy",
                            highTemp = 82,
                            lowTemp = 64
                        )
                        val mockForecast = listOf(
                            DailyForecastUiModel("Today", "Oct 23", WeatherType.PartlyCloudy, 82, 64, 10, "3 Moderate", 45, "12 mph", 80, "7:12 AM", "6:34 PM", true),
                            DailyForecastUiModel("Tuesday", "Oct 24", WeatherType.Sunny, 85, 66, 0, "6 High", 40, "8 mph", 84, "7:13 AM", "6:33 PM"),
                            DailyForecastUiModel("Wednesday", "Oct 25", WeatherType.Rainy, 76, 62, 80, "1 Low", 85, "15 mph", 75, "7:14 AM", "6:32 PM"),
                            DailyForecastUiModel("Thursday", "Oct 26", WeatherType.Cloudy, 74, 60, 20, "2 Low", 50, "10 mph", 72, "7:15 AM", "6:31 PM"),
                            DailyForecastUiModel("Friday", "Oct 27", WeatherType.Stormy, 70, 58, 90, "1 Low", 90, "18 mph", 68, "7:16 AM", "6:30 PM"),
                            DailyForecastUiModel("Saturday", "Oct 28", WeatherType.Sunny, 75, 55, 0, "5 Moderate", 35, "6 mph", 75, "7:17 AM", "6:29 PM"),
                            DailyForecastUiModel("Sunday", "Oct 29", WeatherType.PartlyCloudy, 77, 57, 5, "4 Moderate", 40, "7 mph", 77, "7:18 AM", "6:28 PM")
                        )

                        LaunchedEffect(userLocation) {
                            val location = userLocation ?: return@LaunchedEffect
                            while (true) {
                                backendAlert = if (backendPrediction == null) {
                                    WeatherAlertUiModel(
                                        title = "Checking Storm Risk",
                                        description = "Requesting latest backend prediction"
                                    )
                                } else {
                                    backendAlert
                                }

                                backendAlert = try {
                                    val result = backendApi.predictLive(userLocation = location)
                                    backendPrediction = result
                                    result.toWeatherAlertUiModel()
                                } catch (exc: Exception) {
                                    backendPrediction = null
                                    WeatherAlertUiModel(
                                        title = "Predictor Unavailable",
                                        description = exc.message ?: "Could not reach backend predictor"
                                    )
                                }

                                delay(15 * 60 * 1000L)
                            }
                        }

                        WeatherOverviewScreen(
                            currentWeather = mockCurrent,
                            alert = backendAlert,
                            forecast = mockForecast,
                            userLocation = userLocation,
                            onLiveRadarClick = { navController.navigate("map_screen") }
                        )
                    }
                    composable("map_screen") {
                        MapScreen(
                            onBack = { navController.popBackStack() },
                            userLocation = userLocation,
                            backendPrediction = backendPrediction,
                        )
                    }
                }
            }
        }
    }
}

private fun BackendPredictionResult.toWeatherAlertUiModel(): WeatherAlertUiModel? {
    val probabilityPercent = (stormProbability * 100.0).roundToInt()

    return when {
        alertState == "on" -> WeatherAlertUiModel(
            title = "Storm Alert Active",
            description = "Risk $probabilityPercent% • $alertReason"
        )

        stormProbability >= 0.40 -> WeatherAlertUiModel(
            title = "Storm Risk Elevated",
            description = "Risk $probabilityPercent% • $stormLevel"
        )

        else -> null
    }
}

private fun BackendPredictionResult.toHazardZone(userLocation: LatLng?): HazardZone? {
    val location = userLocation ?: return null
    val severity = when {
        alertState == "on" -> "WARNING"
        stormProbability >= 0.40 -> "WATCH"
        else -> return null
    }

    return HazardZone(
        center = location,
        radiusMeters = 75000.0,
        severity = severity,
    )
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MapScreen(
    onBack: () -> Unit,
    userLocation: LatLng?,
    backendPrediction: BackendPredictionResult?,
) {

    val context = LocalContext.current
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    val locationPermissionState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )

    LaunchedEffect(Unit) {
        locationPermissionState.launchMultiplePermissionRequest()
    }
    val cameraPositionState = rememberCameraPositionState()

    val hazardZone = remember(userLocation, backendPrediction) {
        backendPrediction?.toHazardZone(userLocation)
    }

    @SuppressLint("MissingPermission")
    LaunchedEffect(locationPermissionState.allPermissionsGranted) {
        if (locationPermissionState.allPermissionsGranted) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    val userLatLng = LatLng(location.latitude, location.longitude)
                    cameraPositionState.position = CameraPosition.fromLatLngZoom(
                        userLatLng,
                        12f // Zoom to a level where the circle is visible
                    )
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(
                isMyLocationEnabled = locationPermissionState.allPermissionsGranted,
                mapType = MapType.SATELLITE
            ),
            uiSettings = MapUiSettings(
                myLocationButtonEnabled = true
            )
        ) {
            // Draw hazard zone on the map
            hazardZone?.let { zone ->
                val strokeColor = if (zone.severity == "WARNING") Color.Red else Color(0xFFFFC107)
                Circle(
                    center = zone.center,
                    radius = zone.radiusMeters,
                    strokeColor = strokeColor,
                    strokeWidth = 4f,
                    fillColor = strokeColor.copy(alpha = 0.22f)
                )
            }
        }

        // Back Button Overlay
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .padding(top = 48.dp, start = 16.dp)
                .background(Color.Black.copy(alpha = 0.5f), CircleShape)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = Color.White
            )
        }
    }
}
