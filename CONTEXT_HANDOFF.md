# Virasaya task handoff

Updated: 27 October 2026. Read this after context loss, then check `PROGRESS.md` and the current repository state.

## Current checkpoint — Milestone C in progress

The app has an optional ARCore preview with a local horizontal-plane anchor, tap placement, manual arrow rotation, route-step controls and a 2D escape. The phone previously demonstrated surface detection, placing/rotating the arrow and a tracked anchor. Camera denial, opening Android app settings, and returning to the same 2D step were also checked. Camera permission was restored after those checks.

The latest AR change maps availability outcomes accurately, adds a direct settings action after permanent camera denial, makes anchor disposal best-effort, and shows recovery advice for ARCore's paused-tracking reasons. A live scan showed camera tracking active but zero detected planes. ARCore Instant Placement placed a visible provisional arrow; movement changed its UI status to “Marker tracked”, and the user reported the arrow kept working. AR → 2D, fresh AR entry, and Home/background/resume were checked. The latest offline build also adds page-specific history citations and audited route checks; 14 JVM tests passed and the APK was installed. The phone is now at its lock screen after that update.

A fresh AR entry previously showed a black preview and “Tracking is paused” while the phone state was uncertain. After the user unlocked it, a fresh entry showed live camera and active tracking; the earlier black view was not reproduced. The system-camera baseline remained inconclusive because the phone had relocked during that separate check. Camera permission is restored.

### Next physical check

The final APK is installed, and the phone is at its lock screen. Camera permission is restored. The user previously moved the phone until the UI changed to “Marker tracked” and reported that the arrow kept working during movement (`artifacts/tracking-after-movement-report.png`). AR → 2D step preservation, fresh AR entry and Home/background/resume have now been checked (`artifacts/lifecycle-reentry.png`, `artifacts/lifecycle-resume.png`). Permission revocation restarted the app to Home; restoring permission allowed another AR launch. Next: unlock and verify the updated p. 97 source label in landmark details, then induce tracking loss on a low-detail view and check recovery. Repeat movement and lifecycle checks before closing Milestone C.

See `artifacts/ar-anchor-tracked.png` for the earlier successful placement and `artifacts/ar-reentry-check.png` for the stationary paused state. `PROGRESS.md` is the concise milestone status; `artifacts/VERIFICATION.md` contains dated build/device evidence; `MILESTONE_C_PLAN.md` defines scope and acceptance.

Builds in this offline checkout use `artifacts/.tmp-arcore.init.gradle` and the temporary local Maven copy of the official ARCore artifact. Normal app configuration still uses Google's Maven repository. The debug APK path is `app/build/outputs/apk/debug/app-debug.apk`. No commit or push was made.
## Project goal and design authority

The competition MVP is a connected native Android journey: landmark selection → sourced history → destination → mapped walking route → 2D guidance, with optional local AR assistance. Milestone C is the active task; its physical acceptance gates are listed at the top of this handoff.

Use the root `frontend_reference.png` as the visual reference. The supplied phone screenshots show the rejected old UI; they are NOT the design target. The user specifically rejected four connected dots presented as pathfinding and asked for a real map preview, mentioning OpenLayers and Google Maps as references.

The selected direction is dark olive surfaces, orange actions, large real landmark photos, rounded controls and compact bottom navigation. Keep Kotlin/Jetpack Compose and the small single-module architecture. Read `PROJECT_CONTEXT.md`, `DESIGN.reference.md`, and `AGENTS.md` when present. No subagents were used. No commit has been made.

## Implemented work

- `ui/ExploreScreen.kt`: search, photo carousel, persisted bookmark controls, discovery/saved filters, bottom navigation, history detail with photo credits/source and real map preview.
- `ui/VirasayaApp.kt`: navigation, SharedPreferences bookmarks, asynchronous network loading and error handling. A Material3 Surface supplies correct foreground colors.
- `ui/RouteScreen.kt`: start/destination selection, swap, actual map, approximate distance, manual step previews and Google Maps handoff. Portrait uses a column; landscape uses a scrollable side panel and map.
- `ui/OfflineMap.kt`: local WebView serving a whitelist of bundled assets using appassets origin; network access blocked. Lifecycle release destroys the WebView; clipToBounds constrains it.
- `ui/AppIcon.kt`, `ui/theme/Theme.kt`, `MainActivity.kt`: native outline icons, olive/orange theme and edge-to-edge setup.
- `navigation/WalkingNetwork.kt`: Dijkstra on real imported walking edges, geometry-derived turn previews and approximate geodesic distances.
- `app/src/main/assets/map/`: bundled OpenLayers 10.6.1 with license, offline vector geometry, route rendering, scale, attribution, zoom/north/fit controls. No map API key or backend required.
- `scripts/prepare_map.py`: reproducible OSM import. Source is `data/red-fort.osm`, downloaded October 22 from OSM bbox 77.235,28.651,77.245,28.660. Generated map has 517 features; network has 400 nodes and 439 source edges. Only consecutive mapped walking nodes are connected; explicit private/no/military restrictions are excluded. Foot oneway is respected.
- Three sourced landmarks: Lahori Gate, Naubat Khana, Diwan-i-Aam. History source: UNESCO/ICOMOS Red Fort nomination assessment, https://whc.unesco.org/document/8815. Source/asset licenses and derivation notes are in `data/SOURCES.md`.
- Licensed Wikimedia photographs are in `res/drawable-nodpi/` to avoid density upscaling. Attribution is in the app and source documentation.

Approximate example route: Lahori Gate → Diwan-i-Aam, 423.864 m over 12 mapped nodes. These are computed map distances, not surveyed measurements. Landmark positions are derived from mapped footprints and snapped to nearby existing path nodes; snap gaps are documented. The optional local AR preview is implemented, but is not geographically registered. No live GPS tracking, automatic rerouting or recognition is implemented. Current visitor access needs field verification.

## Verified evidence

Current build and device evidence, including 14 passing JVM tests and the remaining tracking-loss/lifecycle checks, is recorded in `artifacts/VERIFICATION.md`. Earlier Milestone B routing coverage includes all nine pilot endpoint pairs, mapped-edge consistency, shortest weighted routes, directed restrictions, disconnected/same-origin cases, turn geometry and snapping. XML reports are under `app/build/test-results/testDebugUnitTest/`.

Latest `:app:testDebugUnitTest :app:assembleDebug` completed successfully with 14 passing JVM tests. The resulting APK was installed on the phone, which is now at the lock screen. Exact build details and physical checks are in `artifacts/VERIFICATION.md`.

API 37 emulator checks: real map rendering, search for Naubat yielding one place, bookmarks, history/details, destination changes, swap, manual next/previous/close preview, portrait/landscape and Google Maps walking intent. Google Maps launched its location permission dialog; no permission was granted. Browser map testing also covered pan/zoom and small viewport resizing without JS errors.

The final B map-label correction is installed. Later C builds were also installed and launched on serial `10BFBA0J4L008B8` (iQOO Z10 5G). AR permission denial/settings and 2D return were exercised. The latest app-level route check returned to Lahori Gate → Diwan-i-Aam at step 1. Camera permission is restored. In the latest AR scan the camera was tracking, no plane was found, and the provisional fallback placed an arrow on the floor. After a stationary ~30-second observation, moving the phone changed the UI to “Marker tracked”; the user reported the arrow kept working. AR → 2D step preservation, fresh AR entry and one Home/background/resume check succeeded. Revoking camera permission restarted the app at Home; permission restoration and fresh AR launch succeeded. The latest source-audit APK is installed; the phone is now at its lock screen. Unlock it to verify the page-specific source label in landmark details. Repeated movement, tracking-loss and lifecycle stress remain open.

Actual screenshots in `artifacts/`:

- `home-device.png`, `route-device.png`, `directions-device.png`: physical phone, captured after successful installation. Keep these; home-device.png is no longer the earlier black locked-screen capture.
- `home-emulator.png`, `detail-emulator.png`, `route-emulator.png`, `directions-emulator.png`, `route-landscape-emulator.png`: emulator evidence.
- `map-route-browser.png`: browser evidence.

Functional checks are not frame-rate, battery or on-site accuracy measurements.

## Bugs found and fixed during verification

- Black text on dark surfaces: root Material3 Surface now supplies LocalContentColor.
- Blank Android map: WebView page root had zero height despite viewport height. CSS uses 100vh and fixed inset map; resize/fit guards handle initial sizing.
- Landscape map collapsed: responsive side panel preserves map space.
- Map transient overflow: AndroidView clipToBounds added.
- Single search result now says “1 place”.
- Phone screenshot exposed overlapping Naubat and destination labels. The final JS change moves intermediate labels below markers (`offsetY:isEnd||selected?-30:30`); the corrected route labels were captured in `route-device-final.png` and installed before Milestone C work.

## Historical Milestone B continuation

The checklist from the October 26 map-label task is complete and superseded. Current source/build/device status is maintained in `PROGRESS.md` and `artifacts/VERIFICATION.md`; resume Milestone C from its physical camera-recovery and anchor-motion gates.

## Build and device commands

Workspace: `D:\Projects\Virasaya`, PowerShell. Use cached dependencies. Existing user debug key is required to update the installed app without uninstalling/data loss.

```powershell
$env:ANDROID_USER_HOME = 'D:\Projects\Virasaya\.android'
$env:JAVA_HOME = 'C:\Program Files\Eclipse Adoptium\jdk-17.0.20.101-hotspot'
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
$env:GRADLE_USER_HOME = 'D:\Projects\Virasaya\.gradle'
.\gradlew.bat --offline '-Pkotlin.compiler.execution.strategy=in-process' '-Pandroid.injected.signing.store.file=C:\Users\Swee2\.android\debug.keystore' '-Pandroid.injected.signing.store.password=android' '-Pandroid.injected.signing.key.alias=androiddebugkey' '-Pandroid.injected.signing.key.password=android' :app:testDebugUnitTest :app:assembleDebug
```

For this offline checkout, add `--init-script artifacts/.tmp-arcore.init.gradle` to the Gradle command so the temporary local copy of the official ARCore artifact is available.

ADB executable: `C:\Users\Swee2\AppData\Local\Android\Sdk\platform-tools\adb.exe`. Check `adb devices` anew. Emulator serial was `emulator-5554`. Physical phone connectivity can change. Use `install -r`, then `shell am start -W -n com.virasaya/.MainActivity`. Force-stop Virasaya before updating to avoid emulator package-update overlay.

`scripts/android_ui.py` uses current accessibility labels and binary-safe screenshots:

```powershell
python scripts/android_ui.py --serial emulator-5554 dump
python scripts/android_ui.py --serial emulator-5554 tap 'Map'
python scripts/android_ui.py --serial emulator-5554 screenshot route-emulator
```

The helper writes transient `artifacts/window.xml`; screenshots go to artifacts. A UI dump may precede WebView loading: wait for map scale/controls before screenshot. Rotation was checked via `adb shell wm user-rotation lock 1` and restored with lock 0.

Hidden emulator was started for this task with AVD Medium_Phone_API_37.0, no-window/no-audio/read-only/software graphics. Logs are in artifacts. If cleaning up, identify current process first; never kill a stale recorded PID. ADB port 9222 forwarding may remain from WebView diagnostics and can be removed. Temporary diagnostic JS may mutate runtime CSS; do not reuse it as proof of the final bundled code.

If ADB becomes unauthorized, the earlier solution used the existing user key with ANDROID_USER_HOME=C:\Users\Swee2\.android and ADB_VENDOR_KEYS=C:\Users\Swee2\.android\adbkey before restarting the ADB server. Do not alter keys or uninstall the app.

## Documentation and worktree notes

README.md, PROJECT_CONTEXT.md, PROGRESS.md, MILESTONE_C_PLAN.md and artifacts/VERIFICATION.md describe the current frontend/map and Milestone C state. There are many uncommitted source/assets and unrelated untracked IDE/tool files; inspect before staging or deleting. Preserve the reference PNG and source attribution.

Skills already read/applied: frontend-design, playwright-skill, writing-for-agents, and diagnosing-bugs. Continue with AR tracking-loss recovery and repeatability checks; then proceed to verified Red Fort landmark content and a small site-specific navigation graph, the next engineering task.
