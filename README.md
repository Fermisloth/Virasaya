# Virasaya

Native Android Red Fort heritage-guide prototype, built with Kotlin and Jetpack Compose.

## Current visitor flow

Browse photo cards for Lahori Gate, Naubat Khana, and Diwan-i-Aam; save a landmark; read sourced history; preview its location; and choose a walking route. The native Compose frontend follows `frontend_reference.png`: dark olive surfaces, orange actions, large photos, and rounded controls.

The OpenLayers map is bundled in the APK with real OpenStreetMap buildings, paths, and landmark footprints. It works offline and supports pan, zoom, scale, and route fitting. Dijkstra runs on 400 imported walking nodes and 439 source edges. It never connects monuments with fabricated straight-line segments. Route lengths are calculated from the mapped path coordinates. The first Lahori Gate → Diwan-i-Aam route is approximately 424 m over 12 source nodes; this is a map estimate, not a field measurement.

Directions can be stepped through manually. An optional ARCore preview supports tracked floor-plane placement, tap-to-place and rotate a local route arrow, with camera permission recovery and a 2D escape. Instant Placement is disabled because its pose can refine onto a nearby hand or object. The renderer accepts only tracked upward-facing planes at least 0.35 m across in both dimensions, and explains when a placed anchor loses tracking. The updated build installed on the phone, but physical retesting is pending because Android relocked before the new screen was exercised. AR guidance is manually aligned and is not GPS/geographically registered. Current location, GPS rerouting, and camera recognition are not implemented. Google Maps can be opened for walking navigation from the visitor's own location. Present-day access, closures, and the accuracy of mapped geometry still require on-site verification. See `PROGRESS.md` and `artifacts/VERIFICATION.md` for the latest evidence.

Historical content cites the [UNESCO / ICOMOS Red Fort assessment](https://whc.unesco.org/document/8815). Geographic provenance, importer rules, photo attribution, and software licenses are documented in [data/SOURCES.md](data/SOURCES.md). Photos and history load locally; opening source links or Google Maps is external.

## Build and run

Use JDK 17 and an installed Android SDK. On Windows:

```text
gradlew.bat :app:testDebugUnitTest :app:assembleDebug
```

Install `app/build/outputs/apk/debug/app-debug.apk` with Android Studio or `adb install -r app/build/outputs/apk/debug/app-debug.apk`.

## Device check still needed

Check every start/destination pair, swapping endpoints, direction previews, bookmarks, search, map gestures, and activity recreation. Verify proposed walking routes against actual visitor paths before using the prototype as on-site guidance. Actual checks and pending device work are recorded in `PROGRESS.md`.
