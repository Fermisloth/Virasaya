# Virasaya Progress

Updated: 26 October 2026.

## Milestone A - Android foundation: COMPLETE

Android project, build setup, debug APK and initial physical-phone launch were previously verified.

## Milestone B - Heritage content and 2D navigation: IMPLEMENTED; FIELD VALIDATION PENDING

- Compose frontend follows frontend_reference.png: olive surfaces, orange actions, licensed photos, search, saved places, sourced history and map previews.
- Lahori Gate, Naubat Khana and Diwan-i-Aam have short history sourced to UNESCO/ICOMOS. See data/SOURCES.md for history, photo and geographic attribution.
- Bundled OpenLayers renders actual OSM buildings, grounds and paths offline. Dijkstra uses 400 imported walking nodes and 439 source edges, replacing the old four-waypoint schematic.
- Destination selection, swapping, pan/zoom/fit, approximate mapped distances, manual step previews and Google Maps walking handoff are implemented.
- At the Milestone B checkpoint, the routing/content suite had 11 passing JVM tests. Milestone C has since added one AR state test; the current full suite has 12 passing tests. Earlier B-only checks are detailed in `artifacts/VERIFICATION.md`.
- The Milestone B APK installed and launched on the physical iQOO and API 37 emulator October 26. Corrected map labels were visually checked on the phone. Saved Lahori Gate persisted through update and relaunch on the emulator.
- Earlier UI checks covered search, destination changes, swapping, manual directions, details, portrait/landscape and Google Maps handoff. Phone checks covered home/map rendering, zoom/fit and advancing/closing previews.
- Fixed blank WebView sizing, dark-on-dark text, landscape map collapse, clipping and overlapping labels. See artifacts/VERIFICATION.md for evidence.

## Milestone C - AR feasibility: IN PROGRESS; PROVISIONAL FALLBACK VERIFIED

Implementation plan: `MILESTONE_C_PLAN.md`.

- Official ARCore 1.56.0 optional dependency; manifest keeps camera and AR hardware optional. Connected vivo I2404i is listed as iQOO Z10 5G in Google's supported-device list; Google Play Services for AR is installed (1.57.262640093).
- Added privacy disclosure, runtime availability/install checks, camera permission only after consent, and unsupported/error/2D exits.
- Unknown ARCore availability results now lead to a retryable check-error state instead of a false “unsupported device” result; a focused unit test covers supported, unsupported and unknown states.
- Paused camera tracking now reports ARCore's specific recovery reason (initializing, low light, insufficient visual detail, excessive motion, camera contention or internal state) rather than one generic prompt.
- Anchor detachment during screen disposal is now best-effort, so a queued renderer cleanup cannot crash if the owning ARCore session has already closed.
- Implemented a local ARCore camera session, OpenGL camera background, horizontal plane hit tests, procedural route arrow, tracking-gated guidance, manual alignment controls, route step changes, and return-to-2D step preservation.
- A live physical scan reproduced the stuck state: camera tracking was active but ARCore reported zero tracked planes. Plane hits remain preferred; when plane fitting has not converged, the app now enables ARCore local-Y-up Instant Placement and reports the marker as provisional until ARCore reaches full tracking. The physical phone placed a visible arrow on a floor tap with that provisional status. Tapping a wall also demonstrated that approximate placement can follow an incorrect target; the UI now explicitly says to tap the floor and to keep scanning while AR refines it.
- The anchor was still provisional after the phone was held on the scene; no deliberate continued physical scan/motion or stable world-anchor movement has yet been verified. Treat the fallback as an unblock for placement, not proof of stable tracking.
- Physical phone previously displayed the live camera, detected a horizontal surface, and showed the user-placed/rotated arrow (`artifacts/ar-anchor-tracked.png`). After unlocking, a fresh AR entry again displayed live camera and active tracking; a scan over the visible tiled floor did not detect a plane (`artifacts/ar-reentry-unlocked.png`, `artifacts/ar-live-scan-result.png`). The earlier black/paused view is not reproduced in the unlocked run; plane acquisition and camera-motion anchor stability remain open.
- Route-linked AR step transitions and step preservation on return were exercised on phone. Surface detection, anchor placement, manual rotation, and a tracked anchor are demonstrated. Camera denial and the direct app-settings recovery link were also exercised; returning to 2D kept Lahori Gate → Diwan-i-Aam at step 1. Whether the marker stays fixed while moving the phone, re-entry camera recovery, service-install cancellation, tracking-loss recovery, repeated lifecycle stress and sustained performance remain unverified.
- Focused tests cover setup gates, indeterminate availability, recovery guidance, tracking-gated rendering/errors and route step bounds. The latest test/build run succeeded with 12 JVM tests; the APK is installed and opened on the physical phone. This session kept tracking active, so the new cause-specific pause messages were not exercised.

Do not mark C complete until ARCore refines a placed marker to full tracking, the marker stays fixed while the camera moves, and safe 2D fallback/lifecycle checks pass. Current preview is local and manually aligned; it is not geo-registered.

## Milestone D - Landmark recognition: NOT STARTED

## Milestone E - Submission and demonstration: PENDING

## Known limitations and next task

Navigation is a manual preview without live GPS tracking or automatic rerouting. Coordinates and distances derive from OSM, not field measurements. Present-day visitor access and closures remain unverified. Functional checks do not establish sustained frame rate or battery performance.

Next: the latest APK is installed and launched at the app's home screen. Reopen the Lahori Gate → Diwan-i-Aam route and its AR preview, tap a clearly visible textured floor area, then slowly move the phone over the scene until the provisional marker becomes fully tracked; verify that it stays fixed during movement. Follow with repeated AR entry/exit, background/resume and tracking-loss recovery checks. After C's physical gates pass or are recorded inconclusive, return to Red Fort content/access validation. Read CONTEXT_HANDOFF.md to resume.
