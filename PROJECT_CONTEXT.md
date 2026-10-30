# Virasaya — Project Context for Codex

> **Resume ongoing work:** Read `PROGRESS.md` for current milestone status and `CONTEXT_HANDOFF.md` after context loss. Read `artifacts/VERIFICATION.md` for device/build evidence and `MILESTONE_C_PLAN.md` for AR scope and open acceptance checks.
>
> **Purpose:** This file is the working product and engineering brief for coding agents building Virasaya from scratch. Read it before proposing architecture or implementing features. Keep it truthful as the project evolves.
>
> **Status updated 27 October 2026:** The app has sourced landmark content, a frontend based on `frontend_reference.png`, an offline OpenLayers map, and Dijkstra routing over imported OpenStreetMap geometry. Milestone C's optional ARCore preview supports plane placement and an Instant Placement fallback, manual arrow rotation, camera-denial recovery, cause-specific tracking advice and 2D return. On the supported phone, a live scan showed active camera tracking but zero fitted planes; the fallback placed a provisional arrow, then changed to “Marker tracked” after the user moved the phone. The user reported the arrow kept working during movement. One AR re-entry, 2D return and background/resume check succeeded; tracking-loss recovery and lifecycle stress remain open. Recognition remains unimplemented. See `PROGRESS.md`, `artifacts/VERIFICATION.md`, and `MILESTONE_C_PLAN.md` for current evidence and acceptance criteria.

## 1. Project identity

> **AR placement update (29 October 2026):** The Instant Placement fallback has been disabled after the user reported that its provisional arrow could follow a hand and disappear under obstruction. Placement now requires a tracked upward-facing plane at least 0.35 m across both axes; the UI explains when a floor anchor loses tracking. The updated APK built and installed, but the phone relocked before this change could be physically verified. See `PROGRESS.md` and `artifacts/VERIFICATION.md`.

- **Name:** Virasaya
- **Description:** Smartphone-based augmented reality heritage recognition and navigation guide.
- **Initial pilot location:** Red Fort (Lal Qila), Delhi, India.
- **Primary users:** Tourists and first-time heritage-site visitors.
- **Secondary users:** Students, educators, visitors needing navigation assistance, and heritage authorities.
- **Context:** Student engineering / SEWA First technical innovation competition.
- **Target submission date:** **24 October 2026**. The exact submission time and required final artifacts are not yet confirmed.
- **Development approach:** AI-first / “vibe coding” with Codex writing much of the code, while the human builder makes decisions, runs builds, tests on device, verifies sources, and accepts or rejects changes.
- **Human availability:** Approximately 4–6 hours/day, subject to change. Prior references to a 75-day development window are **obsolete** and must not be used for planning.
- **Test device:** iQOO Z10 5G (actual Android/ARCore behavior must be verified on the phone).
- **Verified development environment:** Gradle 8.7 and Eclipse Temurin JDK 17; debug APK build and physical-device launch were confirmed for the foundation milestone.
- **Launch target:** A reproducible competition demonstration and installable Android APK—not public Play Store deployment.

## 2. The problem

At large heritage sites, visitors may struggle to:
1. Identify the structure they are viewing.
2. Find reliable, contextual historical information.
3. Understand how to get from one attraction to another on-site.

Existing alternatives include information boards, printed maps, human guides, and conventional map apps. Virasaya aims to combine **landmark recognition + verified history + site-specific pathfinding + AR-assisted directions** in a single Android experience.

Academic research has been consulted for the proposal, but **team-conducted user interviews, field observations, and user validation have not been confirmed**. Never invent interview counts, performance numbers, deployment claims, or field evidence.

## 3. MVP — the only required scope

Build one connected visitor flow:

**Open app → scan or select a Red Fort landmark → identify/confirm landmark → display verified history → choose destination → calculate route on a site-specific graph → show 2D guidance → optionally display honest AR directional guidance.**

### Required features

1. **Android app shell** with working screens and navigation.
2. **Three selected landmarks** at Red Fort (up to five only if there is time and suitable evidence/data). Exact landmark set has **not been finalized**.
3. **Landmark recognition** using the camera and a lightweight on-device approach, with confidence display, rejection/uncertain result handling, and **manual selection fallback**. Model approach and training dataset are not yet finalized.
4. **Verified historical content** for each selected landmark, stored locally with source attribution.
5. **Site navigation graph** containing waypoints, edges, distances or clearly marked estimates, and visitor-accessibility metadata where known.
6. **A* or Dijkstra pathfinding** between selected nodes; unit tests for route validity and disconnected cases.
7. **Readable 2D map/direction fallback** that remains functional without AR.
8. **Basic AR-assisted guidance** if the device/build supports it: camera feed + AR tracking + visibly rendered local arrow/marker connected to route steps. Do **not** claim precise geospatial alignment without measured evidence.
9. **Graceful error states** for missing camera permission, recognition uncertainty, unsupported AR device/configuration, lost tracking, unreachable route, or missing content.
10. **Debug APK, source repository, README, and recorded/demo-ready flow**.

### Explicitly out of scope for the competition MVP

- Multi-site deployment or city-wide mapping.
- Automatic historical 3D reconstructions / large digital twins.
- Advanced visual positioning with guaranteed precise outdoor AR alignment.
- Advanced conversational AI, payments, accounts, social features.
- Custom AR hardware.
- A production backend, analytics pipeline, or Play Store launch.
- Full multilingual, voice, and accessibility-aware routing systems (possible later extensions).

## 4. Technical approach (provisional, not yet committed by existing code)

| Area | Starting choice | Reason / caveat |
| --- | --- | --- |
| Platform | Native Android, Kotlin | Direct Android/ARCore integration; target one test device |
| UI | Jetpack Compose + Material 3 | Rapid UI iteration and manageable screens |
| Navigation between screens | Navigation Compose | Simple MVP flow |
| Architecture | Small single-module app, MVVM-style separation | Testability without premature complexity |
| Heritage content | Local JSON + source metadata | Works without backend; easy to audit |
| Navigation engine | Kotlin graph model + A* (or Dijkstra) | Deterministic and independently testable |
| AR | Optional ARCore with a small GLES renderer | Plane anchor preferred, provisional Instant Placement fallback, manually aligned route arrow; one full-tracking movement check and basic re-entry/background-resume verified; repeatability and tracking-loss recovery remain open |
| Recognition | Lightweight on-device classifier (implementation choice TBD) | Benchmark actual images; avoid speculative accuracy claims |
| Maps | Site map image/vector + graph overlay | Avoid dependence on unreliable indoor GPS |
| Source control | Git | Checkpoint every working milestone |

**Dependency policy:** Check current official documentation and compatibility before locking Gradle, AGP, Kotlin, Compose, SceneView, ARCore, or ML libraries. Never fabricate test/build success. Do not introduce Firebase, auth, cloud APIs, or paid services without a concrete demonstrated requirement.

## 5. Navigation and localization: critical technical distinction

**Recognizing a monument is not the same as knowing the visitor's exact position and orientation.** A classifier label alone cannot align an AR route to the physical path.

For a credible MVP:

- Use a **selected/confirmed starting landmark** or an explicitly confirmed waypoint.
- Calculate a route through a small, auditable **Red Fort visitor-path graph**.
- Present **2D step-by-step navigation as the reliable baseline**.
- Optionally use ARCore local tracking to display an arrow/marker **relative to the camera/local session**, while clearly describing limitations.
- Ask visitors to manually confirm waypoint arrival/reorientation if needed.
- Fall back to 2D on tracking failure, uncertainty, lack of permissions, or AR incompatibility.
- **Do not** depict camera-local guidance as automatically geo-registered outdoor navigation unless that has actually been implemented and validated.

Any path used in a field demo must be checked against **visitor-accessible paths and site restrictions**. Graph edges or distances may initially be illustrative/sample data, but must be labeled accordingly until verified.

## 6. Suggested UX screens

1. **Home:** Virasaya, Explore Red Fort, Scan Landmark, Navigate.
2. **Explore:** List of selected landmarks.
3. **Landmark details:** Photo, concise historical text, references, Navigate button.
4. **Recognition:** Camera capture/view, prediction, confidence, “Not sure” result, manual selection.
5. **Route selection:** Start/confirmed current waypoint and destination.
6. **2D route:** Map, highlighted route, waypoint names, instructions, recalculation or reset.
7. **AR guide:** Camera/AR view, one meaningful directional marker or arrow, tracking status, next waypoint, “Use 2D directions” fallback.

Do not fake successful recognition or AR overlays with static mockups and represent them as implemented features. Placeholder screens are acceptable **only when explicitly labeled as placeholders** during development.

## 7. Minimal data model

```text
Landmark {
  id: String
  name: String
  shortDescription: String
  verifiedHistory: String
  sourceReferences: List<SourceReference>
  imageAssetPaths: List<String>
  nearestWaypointId: String?
}

SourceReference {
  title: String
  organizationOrAuthor: String?
  url: String?
  accessedOrPublishedDate: String?
}

Waypoint {
  id: String
  label: String
  mapX: Double          // local map coordinates, not assumed GPS
  mapY: Double
  landmarkId: String?
  visitorAccessible: Boolean?
}

RouteEdge {
  fromWaypointId: String
  toWaypointId: String
  distanceMeters: Double?    // null if not verified
  weight: Double              // non-negative routing cost
  bidirectional: Boolean
  accessNotes: String?
  distanceIsEstimated: Boolean
}

Route {
  waypointIds: List<String>
  totalWeight: Double
  instructions: List<String>
}
```

This is a starting point; refine against concrete UI and algorithm requirements. Avoid storing invented GPS coordinates or distances as authoritative.

## 8. Implementation order: build vertical slices, not everything at once

**Milestone A — Installable shell**
- Generate project, run a debug Gradle build, install on the iQOO device.
- All basic screens navigate without crashes with clearly labeled sample content.
- Commit working code.

**Milestone B — Real local data + 2D navigation**
- Three Red Fort landmarks with sourced historical content.
- Minimal graph and route-selection UI.
- Pathfinding tests for expected shortest routes and no-route conditions.
- Visually show the route in 2D.
- Commit working code.

**Milestone C — AR feasibility spike**
- Camera permission and AR session; tracked anchor + visible marker on physical phone.
- Display tracking status and implement 2D fallback.
- If this proves unreliable, protect the working 2D demonstration.
- Commit working code.

**Milestone D — Recognition spike**
- Camera/image input → classification → confidence threshold/unknown → landmark details.
- Hold out distinct test photos; record results rather than inventing accuracy.
- Retain manual selection.
- Commit working code.

**Milestone E — Integration and evidence**
- Demonstrate recognition (or honest manual fallback) → verified details → route → 2D / limited AR guidance.
- Validate on iQOO; handle no-permission, weak recognition, tracking loss, disconnected nodes.
- Produce APK, README, test notes, screenshots, demo recording.

**Priority if compressed:** A → B → C/D as independent spikes → E. A complete reliable 2D visitor journey is better than several disconnected, untestable claims. Keep AR and recognition modular.

### Current checkpoint — Day 18 / Milestone A

- **Complete:** Android project foundation using Kotlin, Jetpack Compose, Material 3, and a small single-module structure.
- **Verified:** Debug APK built successfully, installed and launched on an iQOO Z10 5G, and the initial UI plus placeholder landmark-selection/details/route flow was exercised.
- **Next milestone:** Milestone B — add verified Red Fort content and a functional small site-specific 2D navigation graph.
- **Pending:** Route graph and shortest-path implementation, real route display, landmark recognition, AR feasibility, and field testing.

The current route flow is intentionally a placeholder. Do not describe it as calculated navigation.

**Time planning:** Deadline is October 24, 2026, and availability is 4–6 hours/day. Use **remaining working hours and dependency-based checkpoints**, not a 75-day calendar or an invented set of weeks. Work in reviewable tasks of ~1–3 hours where feasible. Reserve substantial time for integration and evidence capture.

## 9. Testing and success metrics

Measure and document, as feasible:

- **App:** installs, launches, navigates, no crash during scripted demo.
- **Recognition:** per-class predictions, unknown/low-confidence behavior, accuracy, macro F1, relevant angle/lighting/occlusion conditions, number and provenance of test images.
- **Latency:** capture-to-result and user-flow timings on the actual test device.
- **Routing:** expected path and cost on known graphs; no-path and same-origin/destination behavior.
- **Map/navigation:** route instructions align with graph, and any real-world paths/distances are verified before public claims.
- **AR:** session startup success, tracking status, marker rendering, whether alignment is merely local vs verified geographic, recovery/fallback.
- **User tasks:** can a participant identify a landmark, open history, pick a destination, and follow the demonstrated guidance?

For all metrics: keep **measured / estimated / planned** clearly separate. Record test date, conditions, device, sample size, limitations, and failures. If field testing is not possible, label tests as controlled/off-site; do not claim an on-site pilot.

## 10. Competition expectations

Virasaya is intended for a **SEWA First student technical innovation submission**. The earlier described process includes a technical proposal and potentially a five-minute recorded pitch / early prototype demonstration with design artifacts, calculations, cost breakdown, milestones, and risk analysis. The exact October 24 deliverable package **must be confirmed**; Codex must not assume all artifacts are formally required by that deadline.

Proposal/pitch material should show:
- Real user problem, target users, and evidence (properly attributed).
- Existing alternatives and a fair gap comparison.
- Bounded solution and architecture / flow.
- What is genuinely different, without unverified “first-ever” claims.
- Technical drawings/model and engineering choices.
- Transparent cost/resources estimate.
- Milestones and success metrics.
- Actual implementation and test evidence.
- Honest entry/current versus target TRL.
- Cautious novelty/IP potential, not unsupported patentability conclusions.
- References and source attribution.

Avoid presenting generated mockups as field photographs, components as a tested integrated system, or planned results as measured results.

## 11. Working contract for Codex

When acting as the implementation agent:

1. **Inspect the repo first.** Compare this file against existing code. Existing verified implementation takes priority over speculative text here.
2. **State the immediate task and acceptance criteria** before making a large change. Implement one small feature or vertical slice at a time.
3. **Prefer the simplest working option.** Avoid architecture astronautics and unnecessary dependencies.
4. **Build frequently.** Run relevant Gradle build/tests when tool access allows; explicitly report when not run.
5. **Do not claim hardware verification.** Only the human can confirm behaviors observed on the physical iQOO device unless agent-accessible logs/tests prove them.
6. **Expose unknowns honestly.** Ask for permissions, pictures, historical sources, asset licensing, exact site map, or demo constraints when actually blocking work. Use explicit temporary sample data otherwise.
7. **Never fabricate data, citations, device compatibility, train/test splits, locations, distances, accuracy, or completed features.**
8. **Keep manual and 2D fallbacks functional.** Errors in AR or CV must not break the basic experience.
9. **Preserve working state.** Make checkpoint commits after successful milestones and avoid unnecessary rewrites.
10. **Report at the end of each task:** changed files, tested build command/results, real-device steps for the human, known issues, and best next step.

### Standard definition of done for a coding task

- Implementation matches stated scope.
- Builds and relevant automated tests pass **when runnable**; failures are reported accurately.
- UI is usable on a physical phone, or outstanding hardware validation is clearly identified.
- Feature has honest error handling/fallbacks.
- README or development notes updated for important integration decisions.

## 12. Unconfirmed details (do not silently assume)

- Exact October 24 submission time and required artifacts.
- Existing repo/source code/build state (none demonstrated in this brief).
- Development computer OS/specs, Android Studio, and SDK availability.
- Other team members, roles, and effective contributions.
- Budget and acceptable paid services.
- Final three Red Fort landmarks.
- Red Fort site visit, visitor paths, permissions, field photos, data and media rights.
- Whether the demonstration must run live/on-site or can be recorded/off-site.
- Whether the experience must work offline end-to-end.
- Test volunteers and availability.

**North star:** Ship one honest, coherent, demonstrable Android experience—**landmark → historical information → route → 2D navigation / limited AR assistance**—rather than a broad collection of unfinished features.
