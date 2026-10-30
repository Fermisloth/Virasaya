# Virasaya Progress

Updated: 29 October 2026.

## Milestone A - Android foundation: COMPLETE

Android project, build setup, debug APK and initial physical-phone launch were verified. See `artifacts/VERIFICATION.md` for dated evidence.

## Milestone B - Heritage content and 2D navigation: IMPLEMENTED; FIELD VALIDATION PENDING

- Compose frontend follows `frontend_reference.png`: olive surfaces, orange actions, licensed photos, search, saved places, sourced history and map previews.
- Lahori Gate, Naubat Khana and Diwan-i-Aam have history checked against UNESCO/ICOMOS assessment WHC-07/31.COM/INF.8B1, pp. 97-98. Claim-to-page notes are in `data/SOURCES.md` and `data/RED_FORT_CONTENT_AUDIT.md`.
- Bundled OpenLayers renders imported OSM buildings, grounds and paths offline. Dijkstra routes through 400 mapped walking nodes and 439 source edges. The graph and distances are map-derived estimates; visitor access and closures remain unverified.
- Destination selection, swapping, pan/zoom/fit, manual step previews and Google Maps walking handoff are implemented. There is no live GPS tracking or automatic rerouting.
- The latest route/content audit covered all three landmark pairs. Lahori Gate to Naubat Khana: 281.0 m / 10 nodes; Naubat Khana to Diwan-i-Am: 206.2 m / 7 nodes; Lahori Gate to Diwan-i-Am: 423.9 m / 12 nodes. These are not surveyed distances.
- On 27 October, `:app:testDebugUnitTest :app:assembleDebug` succeeded with 14 JVM tests; the resulting APK was installed. See `artifacts/VERIFICATION.md`.

## Milestone C - AR feasibility: IMPLEMENTED; PHYSICAL STRESS CHECKS OPEN

- Optional ARCore preview now places only on tracked upward-facing floor planes at least 0.35 m across, with manual arrow rotation, route-step preview and return to 2D. Instant Placement is disabled because its pose refinement could move the arrow onto a hand or other nearby surface.
- On the physical iQOO, an Instant Placement marker changed to “Marker tracked” after phone movement; the user reported the arrow kept working. AR-to-2D step preservation, one fresh AR entry and one background/resume check succeeded.
- Camera denial/settings recovery was checked. Tracking-loss/recovery, repeated movement and lifecycle stress, AR service-install cancellation and sustained performance remain unverified. AR guidance is local and manually aligned, not geographically registered.
- On 29 October, the physical phone visually showed the Lahori Gate citation as UNESCO World Heritage Centre / ICOMOS, assessment WHC-07/31.COM/INF.8B1, p. 97 (`artifacts/source-citation-device.png`). A fresh AR entry showed live camera and retained Lahori Gate → Diwan-i-Aam, step 1. Floor tapping rendered the arrow; follow-up screenshots still showed “Provisional marker placed” (`artifacts/ar-marker-post-placement.png`, `artifacts/ar-tracking-recovery-check.png`).
- A reported hand-following/arrow-disappearance issue led to a plane-only placement fix and explicit lost-anchor recovery text. The updated APK compiled and installed, but Android relocked before the new screen could be exercised. Physical confirmation of floor-only placement and loss/recovery remains open; repeated lifecycle/movement stress, AR service-install cancellation and sustained performance also remain open. AR guidance is local and manually aligned, not geographically registered. Full evidence and limits are in `CONTEXT_HANDOFF.md`, `MILESTONE_C_PLAN.md` and `artifacts/VERIFICATION.md`.

## Milestone D - Landmark recognition: NOT STARTED

## Milestone E - Submission and demonstration: PENDING

## Known limitations and next task

The 29 October physical check confirmed the updated source citation UI and a fresh live AR camera/arrow placement. Tracking-loss recovery still needs a deliberate physical obstruction and recovery scan. Before submission, verify landmark access points and walking routes on site and perform sustained phone interaction/performance checks. Preserve the working 2D journey.
