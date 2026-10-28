# Virasaya Progress

Updated: 26 October 2026.

## Milestone A - Android foundation: COMPLETE

Android project, build setup, debug APK and initial physical-phone launch were previously verified.

## Milestone B - Heritage content and 2D navigation: IMPLEMENTED; FIELD VALIDATION PENDING

- Compose frontend follows frontend_reference.png: olive surfaces, orange actions, licensed photos, search, saved places, sourced history and map previews.
- Lahori Gate, Naubat Khana and Diwan-i-Aam have short history sourced to UNESCO/ICOMOS. See data/SOURCES.md for history, photo and geographic attribution.
- Bundled OpenLayers renders actual OSM buildings, grounds and paths offline. Dijkstra uses 400 imported walking nodes and 439 source edges, replacing the old four-waypoint schematic.
- Destination selection, swapping, pan/zoom/fit, approximate mapped distances, manual step previews and Google Maps walking handoff are implemented.
- Six JVM tests passed October 25. On October 26, :app:testDebugUnitTest :app:assembleDebug succeeded with unchanged tasks up to date; XML reports show six tests, zero failures/errors.
- Final APK installed and launched on the physical iQOO and API 37 emulator October 26. Corrected map labels were visually checked on the phone. Saved Lahori Gate persisted through update and relaunch on the emulator.
- Earlier UI checks covered search, destination changes, swapping, manual directions, details, portrait/landscape and Google Maps handoff. Phone checks covered home/map rendering, zoom/fit and advancing/closing previews.
- Fixed blank WebView sizing, dark-on-dark text, landscape map collapse, clipping and overlapping labels. See artifacts/VERIFICATION.md for evidence.

## Milestone C - AR feasibility: NOT STARTED

Implementation plan prepared: `MILESTONE_C_PLAN.md`. AR implementation has not started. First gate: actual device compatibility, followed by a physical tracked-anchor proof and safe 2D fallback.

## Milestone D - Landmark recognition: NOT STARTED

## Milestone E - Submission and demonstration: PENDING

## Known limitations and next task

Navigation is a manual preview without live GPS tracking or automatic rerouting. Coordinates and distances derive from OSM, not field measurements. Present-day visitor access and closures remain unverified. Functional checks do not establish sustained frame rate or battery performance.

Next: verify the Red Fort content and every pilot route/access point on site, then complete extended phone interaction/performance checks. Preserve the 2D journey during later optional AR work. Read CONTEXT_HANDOFF.md to resume.
