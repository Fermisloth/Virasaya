# Verification - 26 October 2026

## Build

:app:testDebugUnitTest :app:assembleDebug succeeded offline with JDK 17 and the existing debug key after the AR availability, disposal-safety and tracking-reason updates. 41 tasks completed (8 executed, 33 up to date). Twelve JVM tests passed: six AR state tests, one landmark test and five walking-network tests. The resulting APK installed on and was opened on the physical phone after it was unlocked.

APK: app/build/outputs/apk/debug/app-debug.apk.

## Milestone B final-device checks - 26 October

Final APK installed successfully and launched on physical phone 10BFBA0J4L008B8 and emulator-5554. Phone notification shade initially obscured the app; collapsing it exposed the app normally. Opened Map and waited for bundled assets. Inspected route-device-final.png: real paths/buildings, orange route, separated landmark labels, scale and attribution render correctly. The route remains approximately 424 m from Lahori Gate to Diwan-i-Aam.

On the emulator, Saved still contained Lahori Gate after force-stop, package update and relaunch, confirming bookmark persistence.

## Earlier checks - October 25

API 37 emulator: search for Naubat returns one place; bookmarks; details/map preview; destination changes; swap; next/previous/close manual directions; portrait/landscape; Google Maps walking ACTION_VIEW handoff. No Google Maps location permission was granted. Browser map testing covered pan/zoom and small viewport sizing without JS errors.

Physical phone: inspected home/map and exercised zoom, fit route, preview directions, next step and close preview. Evidence: home-device.png, route-device.png and directions-device.png. The final label correction is captured separately in route-device-final.png.

## Limits

Functional checks are not frame-rate/battery benchmarks or field validation. Visitor access, distances and endpoint accessibility require on-site checks. AR is implemented as a local, manually aligned preview, but it has no geographic registration; current location, automatic rerouting and recognition are not implemented.

## Milestone C follow-up - 27 October

- Reopened the final APK on the physical iQOO and returned to Lahori Gate → Diwan-i-Aam, AR step 1. Camera tracking was active; the UI still reported no horizontal surface.
- Tapped the visible floor and confirmed a rendered arrow with the explicit “Provisional marker placed” state. Evidence: `tracking-continue-provisional.png`.
- At the end of the ~30-second stationary observation, the arrow remained provisional. No deliberate device movement occurred during that observation. The device stayed on the AR screen for the subsequent movement check.
- The user then moved the phone and reported that the arrow kept working. A fresh accessibility dump showed the status changed to “Marker tracked”; the camera screenshot shows the arrow rendered on the floor after movement: `tracking-after-movement-report.png`. This verifies full ARCore tracking status and a user-reported movement check.
- Returning from AR to the 2D preview preserved Lahori Gate → Diwan-i-Aam at step 1. A fresh AR entry displayed live camera again (`lifecycle-reentry.png`). Sending the active AR app to Home and reopening it restored the live camera at step 1 without a crash (`lifecycle-resume.png`). These were single checks; repeated lifecycle stress remains open.
- Revoking CAMERA while the app was active led Android to restart Virasaya at its home screen. No fatal exception was found in the sampled logcat. Camera permission was restored with the Android package manager; navigating back through the route and granting the app's existing permission opened a fresh live AR session (`permission-restored-ar.png`). This did not demonstrate the in-screen permission-loss state because Android restarted the app.
- Still to verify: induced tracking-loss/recovery, repeated movement and lifecycle stress, AR service-install cancellation, and sustained performance. The content-audit APK has since been installed; the phone is now at its lock screen and camera permission remains granted.

## Milestone C spike - 26 October

### Plane-tracking fallback follow-up

- Reproduced the reported stuck state on the unlocked physical phone: the live ARCore camera was `TRACKING` with failure reason `NONE`, but the renderer observed `planes=0` and `horizontalTracked=0` while facing the room floor. The attempt to tap without a plane therefore had no plane anchor to use.
- Enabled ARCore Instant Placement in `LOCAL_Y_UP` mode as a fallback when the regular horizontal-plane hit test has no valid result. The app still prefers a tracked upward-facing floor plane. Instant Placement anchors are explicitly marked provisional while ARCore reports approximate-distance tracking; the UI instructs the visitor to tap the floor and keep scanning until tracking refines.
- Installed the updated APK and physically tapped first on the cabinet and then on the visible floor. Both rendered an arrow and were labeled provisional; the floor screenshot is `floor-provisional-marker.png`. This proves fallback placement works; it does not prove full tracking or anchor stability during motion. A deliberate continued physical scan is still required.
- Final `:app:testDebugUnitTest :app:assembleDebug` passed after the renderer and UI changes: 12 JVM tests passed and the debug APK built. The final APK was installed on the connected phone; the screen was left at the home screen by package update.

- Connected device: vivo I2404i / model I2404, Android 16; Google Play Services for AR 1.57.262640093. Google's supported-device list includes iQOO Z10 5G. ARCore runtime availability returned supported.
- ARCore 1.56.0 optional integration compiles. Latest `:app:testDebugUnitTest :app:assembleDebug` succeeded; 12 JVM tests passed (six AR state tests, one landmark test and five walking-network tests).
- Installed physical build reached the privacy/consent screen, requested camera only after Continue, and rendered live camera background. Manual route step changes and returning to 2D preserved step 3 in an earlier physical interaction.
- User initially saw no plane and no arrow. The follow-up build queries all tracked planes and improves scan/missed-hit messages. The user then confirmed detecting a surface, placing the arrow at the tap location and rotating it. Screenshot `ar-anchor-tracked.png` captures the marker on the camera view with “Marker tracked”. Returning to 2D preserved step 2.
- Physical CAMERA denial path: the system permission prompt appeared only after tapping “Continue to camera”. Declining showed the denial state and retained the 2D escape; it returned to the same Lahori Gate → Diwan-i-Aam step. The permanent-denial state exposed “Open camera permission settings”, which opened Virasaya’s Android App info page. Camera permission was restored after the check.
- A fresh AR entry after the permission test opened the route-linked AR screen, but stayed at “Tracking is paused” with a black camera panel while the phone was stationary. Android reported CAMERA foreground/allowed and camera service activity. The phone was not physically moved during this check, so camera/anchor recovery is inconclusive; see `ar-reentry-check.png` and repeat while scanning a textured area.
- After the phone was explicitly unlocked, another fresh AR entry displayed a live camera feed with active tracking. The room view changed as the phone moved, but ARCore did not detect a plane while the camera faced the tiled floor. Evidence: `ar-reentry-unlocked.png` and `ar-live-scan-result.png`. Camera re-entry now works in this unlocked run; successful plane acquisition and anchor movement were not demonstrated. The Instant Placement fallback behavior is recorded above.
- A system-camera baseline was attempted afterward, but the phone had relocked and the captured image showed the lock screen. That result is inconclusive and must not be treated as evidence that the physical camera is black. Unlock the phone before repeating the AR scan/re-entry check.
- Still to verify: refinement to full tracking, arrow stability while moving, cause-specific paused-tracking guidance, service-install cancellation, tracking loss/recovery, background/rotation/repeated-entry stress, and sustained performance. C remains in progress.
- An indeterminate ARCore availability result now maps to a retryable error state, with a dedicated unit test; only the explicit unsupported-device result is presented as unsupported.
- When tracking pauses, the app now uses `Camera.getTrackingFailureReason()` to show cause-specific advice; a state test covers the recovery guidance. The current device run maintained active camera tracking, so the physical pause messages were not triggered.
- Renderer cleanup now tolerates an already-closed ARCore session while detaching its anchor. This is a code-level guard; repeated entry/exit and background lifecycle stress still need physical verification.

AR dependency resolution in this sandbox used a temporary local Maven bootstrap containing the official Google Maven POM/AAR; normal project configuration still resolves through Google's Maven repository. Do not retain the temporary build aid as a runtime requirement or claim that a clean networked checkout was tested.


## Red Fort source and route audit - 27 October

- Checked the Lahori Gate, Naubat Khana and Diwan-i-Am summaries against printed page 97 of UNESCO/ICOMOS assessment WHC-07/31.COM/INF.8B1. The ICOMOS evaluation corroborates the details on pp. 97-98. Added page-specific source text in the app and a claim-to-page table in `data/SOURCES.md`; the standalone source audit is `data/RED_FORT_CONTENT_AUDIT.md`.
- Audited the three-stop graph generated from the checked-in OSM extract. It contains three landmark endpoints, 400 walking nodes and 439 source edge records. Routes: Lahori Gate to Naubat Khana 281.0 m / 10 nodes; Naubat Khana to Diwan-i-Am 206.2 m / 7 nodes; Lahori Gate to Diwan-i-Am 423.9 m / 12 nodes. These are map estimates, not surveyed distances or proof of present access.
- Added JVM checks for the cited historical claims and three route lengths/node counts. Final `:app:testDebugUnitTest :app:assembleDebug` passed: 14 tests (six AR state, two landmark and six walking-network tests). The APK installed successfully. The phone relocked during the post-install UI check, so the updated source label still needs a visual check after unlocking.
