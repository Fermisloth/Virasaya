# Virasaya

Minimal native Android MVP shell for the Red Fort heritage-guide concept.

## Milestone 1

This milestone implements a Compose shell, manual selection of the three pilot landmark names, confirmation/details state, back navigation, and a clearly labeled route placeholder. Historical text, recognition, map data, distances, routing, camera, and AR are not implemented.

## Build and run

Requirements: Android Studio with an Android SDK and a JDK compatible with the configured Android Gradle Plugin.

```text
gradlew.bat testDebugUnitTest
gradlew.bat assembleDebug
```

Install `app/build/outputs/apk/debug/app-debug.apk` on an emulator or device from Android Studio or with `adb install`.

The wrapper scripts and configuration are included. The wrapper JAR is not available in the current environment; Android Studio or a Gradle installation can supply/generate it before command-line builds run.
