# Virasaya — Codex Agent Instructions

Read `PROJECT_CONTEXT.md`, `DESIGN.reference.md`, and relevant progress documentation before implementation. Treat the existing, verified repository state as authoritative.

## Primary objective
Build a demonstrable Android APK for the October 24, 2026 competition submission.

Prioritize the complete visitor journey:
landmark selection/recognition → verified history → destination → graph route → 2D guidance → optional limited AR assistance.

## Engineering rules
- Use Kotlin, Jetpack Compose, and a small single-module architecture unless existing code or verified requirements justify another approach.
- Inspect relevant files before editing.
- Implement one small, testable vertical slice at a time.
- Preserve the working Android build and avoid unnecessary dependency upgrades.
- Avoid unnecessary libraries, backends, cloud services, or architectural complexity.
- Prefer official Android documentation when verifying APIs and dependencies.
- Prioritize functional 2D navigation over AR.
- Keep recognition and AR optional behind working manual-selection and 2D fallbacks.
- Never invent historical facts or sources, geographic coordinates, route distances, validation metrics, model accuracy, or hardware test results.
- Clearly distinguish implemented, tested, estimated, and planned behavior.

## Context and token efficiency
- Search for relevant symbols and files before reading large amounts of code.
- Avoid repeatedly reading unchanged files.
- Keep explanations concise.
- Use skills only when relevant.
- Avoid subagents for simple changes.
- Run focused tests before broader test suites.
- Preserve existing working implementations.
- Run the Gradle build after code changes when possible.

## Definition of done
For each task:
1. Implement the requested functionality.
2. Run the relevant build and automated tests when possible, including the Gradle build after source changes.
3. Report modified files and actual verification results.
4. Identify any steps requiring verification on the physical Android device.
5. State known limitations and the next highest-priority task. The next engineering task is verified Red Fort landmark content and a small site-specific navigation graph.

Do not report success for builds or tests that were not executed.
