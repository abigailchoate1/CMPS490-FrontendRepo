# CMPS490-FrontendRepo

Android front-end for the CMPS 490 Weather Tracker senior project. Displays an interactive map with real-time user location and notifies users of predicted dangerous weather (flood/storm) based on weather risk data and the users current location.

---

## Frontend Tech Stack

### Language & Platform
| Technology | Version |
|---|---|
| Kotlin | 2.0.21 |
| Android SDK (min) | 26 (Android 8.0 Oreo) |
| Android SDK (target/compile) | 36 |
| Java Compatibility | Java 11 |

### Build System
| Tool | Version |
|---|---|
| Gradle Wrapper | 9.1.0 |
| Android Gradle Plugin (AGP) | 9.0.0 |
| Kotlin Compose Plugin | 2.0.21 |

---

## Dependencies

### Core AndroidX
| Library | Version |
|---|---|
| `androidx.core:core-ktx` | 1.10.1 |
| `androidx.lifecycle:lifecycle-runtime-ktx` | 2.6.1 |
| `androidx.activity:activity-compose` | 1.8.0 |

### Jetpack Compose (BOM `2024.09.00`)
| Library | Notes |
|---|---|
| `androidx.compose.ui:ui` | Core Compose UI |
| `androidx.compose.ui:ui-graphics` | Graphics primitives |
| `androidx.compose.ui:ui-tooling-preview` | Preview support |
| `androidx.compose.material3:material3` | Material Design 3 components |
| `androidx.compose.ui:ui-tooling` | Debug tooling |
| `androidx.compose.ui:ui-test-manifest` | Debug test manifest |

### Google Maps & Location
| Library | Version |
|---|---|
| `com.google.maps.android:maps-compose` | 4.3.0 |
| `com.google.android.gms:play-services-maps` | 18.2.0 |
| `com.google.android.gms:play-services-location` | 21.0.1 |
| `com.google.accompanist:accompanist-permissions` | 0.34.0 |

### Testing
| Library | Version |
|---|---|
| `junit:junit` | 4.13.2 |
| `androidx.test.ext:junit` | 1.1.5 |
| `androidx.test.espresso:espresso-core` | 3.5.1 |
| `androidx.compose.ui:ui-test-junit4` | (via Compose BOM) |

---

## Key Imports Used

### Android / AndroidX
- `android.Manifest` — runtime permission constants (`ACCESS_FINE_LOCATION`, `ACCESS_COARSE_LOCATION`)
- `android.os.Bundle`
- `androidx.activity.ComponentActivity` — base Activity class for Compose
- `androidx.activity.compose.setContent`
- `androidx.activity.enableEdgeToEdge`
- `androidx.compose.foundation.layout.*` — layout composables (`Box`, `fillMaxSize`)
- `androidx.compose.runtime.*` — state management (`remember`, `mutableStateOf`, `LaunchedEffect`, `getValue`, `setValue`)
- `androidx.compose.ui.Modifier`
- `androidx.compose.ui.graphics.Color`
- `androidx.compose.ui.platform.LocalContext`

### Google Maps Compose
- `com.google.maps.android.compose.GoogleMap`
- `com.google.maps.android.compose.Circle`
- `com.google.maps.android.compose.MapProperties`
- `com.google.maps.android.compose.MapType`
- `com.google.maps.android.compose.MapUiSettings`
- `com.google.maps.android.compose.rememberCameraPositionState`

### Google Play Services
- `com.google.android.gms.location.LocationServices` — `FusedLocationProviderClient`
- `com.google.android.gms.maps.model.CameraPosition`
- `com.google.android.gms.maps.model.LatLng`

### Accompanist
- `com.google.accompanist.permissions.ExperimentalPermissionsApi`
- `com.google.accompanist.permissions.rememberMultiplePermissionsState`

---

## Repository Sources
- Google Maven (`maven.google.com`)
- Maven Central
- Gradle Plugin Portal
