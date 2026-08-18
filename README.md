# Xiaomi 14T Android App

This repository contains a minimal Android app project for a Xiaomi 14T device.

## Features
- Material 3 design
- Android 14 compatible project structure
- Ready to expand with your own UI and logic

## Setup
1. Install Java 17.
2. Install Android Studio or the Android SDK command-line tools.
3. Ensure the Android SDK path is available in `local.properties` (for example `sdk.dir=C:\\Users\\<you>\\AppData\\Local\\Android\\Sdk`).
4. Run `./gradlew assembleDebug` to build the app.

## Run it on your Xiaomi 14T
1. Open this folder as a project in Android Studio.
2. Let Android Studio install the required Gradle plugin dependencies.
3. Connect your Xiaomi 14T by USB.
4. Enable Developer options and USB debugging on the phone.
5. Click Run and choose your device.

## Main files
- `app/src/main/java/com/example/xiaomi14tapp/MainActivity.kt` – app entry point
- `app/src/main/res/values/strings.xml` – app label
- `app/src/main/res/values/themes.xml` – theme configuration

## Notes
The app is intentionally simple as a starter. You can extend it with:
- battery monitoring
- camera controls
- settings screen
- custom Xiaomi-inspired design
