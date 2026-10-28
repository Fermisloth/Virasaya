# Milestone C implementation plan: optional AR feasibility

Date: 27 October 2026. Status: IMPLEMENTED; TRACKING-LOSS/REPEATABILITY STRESS OPEN. Plane placement, manual rotation, camera denial, settings recovery and 2D return were demonstrated on the physical phone. A later unlocked scan reproduced camera tracking with zero tracked planes. ARCore Instant Placement supplied a clearly labeled provisional fallback; after the user moved the phone the UI changed to “Marker tracked” and the user reported the arrow kept working (`artifacts/tracking-after-movement-report.png`). Fresh AR re-entry and Home/background/resume restored a live camera; returning to 2D preserved route step 1. Permission revocation restarted Android at the app home screen, after which permission was restored and AR launched again. Induced tracking-loss/recovery and repeated lifecycle/movement stress remain open. Cause-specific paused-tracking guidance compiled and passed state tests but was not triggered because camera tracking stayed active. The latest APK adds page-specific historical citations and source-route checks; it is installed, but the phone is now at its lock screen. See `PROGRESS.md` and `artifacts/VERIFICATION.md` for current evidence.

## Objective and boundary

Prove camera permission, a working ARCore session, one tracked anchor with a visible marker on a physical phone, understandable tracking status and a dependable return to the existing 2D route. This is the Milestone C definition in PROJECT_CONTEXT.md.

Milestone B implementation and functional checks are complete; field validation of its geometry, visitor access and sustained phone performance remains pending. C can proceed as an independent local-tracking spike without implying that B is field-validated.

Keep the existing Kotlin/Compose single module and olive/orange visual language. Scope excludes landmark recognition (Milestone D), live GPS navigation, geospatial positioning, Cloud Anchors, Depth requirements, automatic rerouting and persistent world anchors. No backend, account or paid service is needed for this local spike.

## Technical decisions

- Make AR optional: camera permission requested only after choosing AR; camera and AR hardware features must not make installation dependent on AR support. Set ARCore application metadata to optional. Keep the full manual/2D journey available on unsupported devices.
- Use the official ARCore Android SDK with a small GLSurfaceView renderer embedded through Compose AndroidView. Adapt the minimum camera-background, projection and simple geometry rendering from Google's sample, preserving applicable license notices. A procedural arrow/marker avoids model downloads and asset loaders.
- Prefer this narrow renderer over introducing SceneView plus its rendering dependency tree for one marker. ARCore 1.56.0 is pinned and builds with the existing minSdk 24, compile/target 35, AGP 8.6.1 and Kotlin 2.0.21 toolchain.
- ARCore owns the camera while the AR screen is active; do not add a competing CameraX preview. Bind session and GL lifecycle to the screen/activity and release the camera when returning to 2D.
- Prefer a hit on a tracked upward-facing horizontal plane. Enable ARCore Instant Placement in local-Y-up mode as a fallback when plane fitting has not converged; label approximate anchors provisional until ARCore reports full tracking. Instruct visitors to tap the floor and continue scanning. No arbitrary screen overlay may be presented as a tracked world marker.

## Ordered implementation slices

### C1. Compatibility and permission gate — IMPLEMENTED; PHYSICAL DENIAL CHECKED

Inspect the actual connected phone's manufacturer/model, Android version and AR service availability. Check Google's supported-device list, then use runtime availability checks as the application gate. Similar names are not evidence: a listing for iQOO Z10X does not establish iQOO Z10 support.

Add an optional “Try AR preview” action to a valid route. Model states explicitly: checking support, unsupported, services installation/update needed, camera permission needed, ready, and recoverable error. Handle asynchronous/temporary availability results without hanging or looping. Invoke requestInstall before session creation and resume the gate after the external install flow; cancellation must return safely. Explain camera usage before prompting, handle denial and permanent denial, and include the required ARCore privacy disclosure.

Acceptance: declining permission, declining installation or using an unsupported device leaves 2D navigation intact. No camera request occurs at app launch. If the target phone is unsupported, record the outcome and require another supported physical device to prove tracking; do not sideload around certification or claim AR success.

### C2. Camera and one real tracked marker — TRACKED MOVEMENT AND BASIC RE-ENTRY DEMONSTRATED

Create/resume the session only after prerequisites pass. Render the camera feed with correct display rotation and projection. Accept taps only when camera tracking is active; use a valid detected horizontal plane when available, otherwise use the clearly labeled provisional Instant Placement fallback. Place one clearly visible marker, replacing and detaching the previous anchor on re-placement.

Show tracking state in Compose without publishing every render frame as UI state. Hide directional content when camera or anchor tracking is paused/stopped. Offer a contextual recovery instruction and a permanent “Use 2D directions” control. On a fresh session, require placement again rather than restoring an invalid native anchor.

Acceptance: on a supported physical phone the marker remains attached to the selected surface as the camera moves. Record a short demonstration and device/build details. A screenshot of a static overlay is insufficient evidence.

### C3. Connect the marker to an honest route-step preview — IMPLEMENTED; STEP RETURN CHECKED

Carry start ID, destination ID and current manual step into AR; keep map/step state when returning. Extract a small saveable route-selection state shared by the route screen and AR destination if needed. Reuse WalkingNetwork route/instruction output; do not build a second routing system.

Display the current instruction and destination above the camera with “Local AR preview — align manually”. The user confirms their current step, taps the floor to place a marker (provisionally when no plane is tracked), and explicitly aligns the arrow with the visible walkway using rotation controls. This alignment is user supplied: geographic coordinates alone do not identify the AR session's world origin or heading.

For the feasibility build, advancing a manual step clears placement/alignment and asks for a fresh confirmation. Do not move a marker hundreds of metres along a local session or announce arrival automatically. Clear alignment on route changes, session recreation or anchor loss; pause guidance during transient tracking loss and offer realignment after recovery.

Acceptance: the route instruction changes with the selected step, unsupported/lost-tracking states cannot show apparently trustworthy guidance, and returning to the map preserves endpoints and the selected step. There is no claim of automatic geographic alignment.

### C4. Lifecycle, failure handling and regression checks — BASIC RE-ENTRY/BACKGROUND CHECKED; EDGE CASES OPEN

Handle background/foreground, lock/unlock, rotation, back navigation, camera unavailable, permission revocation and AR service exceptions. Serialize session/render-thread access; pause rendering before pausing the session, detach anchors and close owned resources when leaving. Follow the chosen SDK sample's lifecycle ordering and verify it on device.

Use a single anchor and simple material. Avoid per-frame allocation, unnecessary recomposition and holding the camera while 2D is visible. Inspect a short physical-device trace and a sustained trial for freezes, overheating symptoms and responsiveness; record actual observations, not assumed FPS.

Acceptance: repeated AR entry/exit does not produce a black camera or crash, and the existing search/history/bookmarks/map/Google Maps flow remains functional.

## Implemented files and interfaces

| Area | Planned change |
| --- | --- |
| `gradle/libs.versions.toml`; `app/build.gradle.kts` | ARCore 1.56.0 dependency; existing toolchain retained |
| `app/src/main/AndroidManifest.xml` | Camera permission, optional camera/AR features and ARCore metadata |
| `ar/ArUiState.kt`; `ui/ArGuideScreen.kt` | Support/install/permission gate, error/retry states, route step, tracking status and 2D escape |
| `ui/ArCameraSurface.kt` | Compose/GLSurfaceView and activity lifecycle bridge |
| `ar/ArRenderer.kt` | Camera background, horizontal-plane hit test, local anchor, arrow and cleanup |
| `navigation/Routes.kt`; `ui/VirasayaApp.kt`; `ui/RouteScreen.kt` | AR destination, route-step handoff and restoration |
| `app/src/test/.../ArUiStateTest.kt`; `artifacts/VERIFICATION.md` | Five focused AR state tests and current build/device evidence |

Keep these responsibilities small; introduce no generic AR framework or additional Gradle module.

## Verification and completion gate

1. Focused JVM tests: availability/error transitions, tracking visibility decisions, route-step preservation and alignment invalidation. Reuse existing routing tests. Tests should verify behavior, not SDK internals.
2. Run :app:testDebugUnitTest and :app:assembleDebug after each source slice. Install using the existing debug key without clearing user data.
3. Physical matrix: permission grant/denial, install/update/cancel where available, surface placement, camera motion, poor texture/lighting, tracking loss/recovery, repeated entry/exit, background/resume, rotation and return to unchanged 2D state. Mark unexercised cases explicitly.
4. Capture device model, Android/AR service versions, APK identity, observed issues and a video showing a world-tracked marker. Emulator checks cover flow only and do not replace physical tracking proof.
5. Mark C complete only when a supported physical device demonstrates a live camera and tracked marker through camera movement and re-entry, with tracking status and safe 2D fallback, plus successful build/regression checks. If support or reliability fails, record C as blocked or inconclusive and retain the working 2D demonstration.

Suggested checkpoints: C1 support decision → C2 physical anchor proof → C3 route-linked manual preview → C4 regression/evidence. Do not spend effort polishing route AR before the physical anchor proof. No calendar estimate is committed until device support and renderer compatibility are established.

## Official references checked for planning

- [Enable AR / optional apps / runtime prerequisites](https://developers.google.com/ar/develop/java/enable-arcore)
- [Supported devices](https://developers.google.com/ar/devices/)
- [Hit-testing and anchor placement](https://developers.google.cn/ar/develop/java/hit-test/developer-guide?hl=en)
- [Anchor lifecycle and tracking state](https://developers.google.com/ar/reference/java/com/google/ar/core/Anchor)

This plan records intended scope and current acceptance gates. Hardware behavior is established only by the dated observations in `artifacts/VERIFICATION.md`; do not infer successful re-entry or movement stability from compilation or the earlier tracked-anchor screenshot.
