# CMPS490-FrontendRepo

Android frontend for the weather app.

## Backend ML integration

The app now talks to the backend instead of hardcoding a demo alert card.

Current flow:
1. request location permission
2. register device with `POST /users/register`
3. call `POST /users/{id}/ml/predict-auto`
4. show the returned alert state in the existing overview screen

The backend base URL is configured in:
- `app/build.gradle.kts` via `BuildConfig.BACKEND_BASE_URL`

Default value:
- `http://10.0.2.2:8000`

That works for the Android emulator talking to a backend running on the host machine.
For a physical device, replace it with your computer's LAN IP and port.

## Build note

This project requires a local Android SDK.
If Gradle reports `SDK location not found`, add a `local.properties` file in the repo root:

```properties
sdk.dir=C:\\Path\\To\\Android\\Sdk
```
