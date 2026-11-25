# Agent instructions

These instructions apply to all changes in this repository.

## Read before editing

1. [CONTINUE.md](CONTINUE.md) for the active milestone, branch, CI state, and next work.
2. [docs/MOT0.1_PLAN.md](docs/MOT0.1_PLAN.md) for the current implementation sequence and acceptance criteria.
3. [docs/MOTION.md](docs/MOTION.md) for the agreed motion principles.
4. [docs/MORPH_STUDY.md](docs/MORPH_STUDY.md) before changing morph behavior.
5. [motion/README.md](motion/README.md) before changing library APIs.

Do not assume information from an earlier chat is current. Verify the repository state and latest CI run.

## Product and architecture constraints

- This is an independent motion framework for Jetpack Compose apps. It must work for Solstice and unrelated apps.
- Never add a dependency from Motion to Solstice or any consuming application. Application-specific styling, navigation, and product decisions stay in the app.
- Compose remains the animation and interaction foundation. Use Compose primitives such as `Animatable`, `spring`, and gesture APIs; do not invent a second general-purpose animation engine.
- Preserve the core promises: interruptible motion, velocity continuity, immediate input handling, predictable gesture ownership, and explicit numeric contracts.
- Keep the public API small and intentional. Do not add public inspection hooks, configuration knobs, dependencies, or abstractions solely to simplify one test.
- Do not change the established motion feel or agreed behavior without recording the reason and asking for a decision when the trade-off is user-visible.

## Current milestone boundaries

MOT0.1 is limited to reliability of the core motion state and physics, focused regression tests, and directly related documentation.

Do not fold these into MOT0.1:
- Motion Lab implementation or redesign.
- New animation/interaction components.
- Benchmarks or performance dashboards.
- Solstice-specific UI or migration work.
- Broad refactors unrelated to a failing test or a documented contract gap.

Follow the ordered tasks in [docs/MOT0.1_PLAN.md](docs/MOT0.1_PLAN.md). Start with regression tests for interruption and reversal in `MorphMotion`, then implement the smallest change that satisfies the intended contract.

## Implementation and test discipline

- Prefer a failing regression test before changing production code.
- For animation tests, use a deterministic `MonotonicFrameClock` and advance frames deliberately. Do not use wall-clock sleeps or timing-dependent assertions.
- Test observable behavior: final anchor, current bounds, visibility, open/closed intent, and whether motion settles. Do not expose internal velocity as public API just for assertions.
- Retarget from current animated values and preserve each `Animatable`'s current velocity when starting a replacement spring.
- New commands and gestures must supersede stale work. Check pending coroutines and initial placement races, not only the happy path.
- Validate non-finite numeric input before mutating state or launching work. Do not silently turn `NaN` or infinity into a plausible value.
- Keep the intentional finite high-velocity clamp in `MorphMotion.release`; validate the raw input before clamping.
- Preserve legitimate zero-size/transient layout anchors if validating `MorphAnchor`; reject invalid geometry rather than silently normalizing it.
- Never weaken or delete a regression assertion merely to make CI green.

## Verification

Use the Gradle wrapper. The repository targets JDK 21 and Android SDK / Build Tools 37.

Run the full workflow from the repository root:

```bash
./gradlew spotlessCheck
./gradlew :motion:testDebugUnitTest
./gradlew :motion:assembleRelease
```

On Windows PowerShell:

```powershell
.\\gradlew.bat spotlessCheck
.\\gradlew.bat :motion:testDebugUnitTest
.\\gradlew.bat :motion:assembleRelease
```

A task is not verified merely because it compiles locally. Confirm that GitHub Actions passed for the exact latest branch-head SHA. Report any unavailable local SDK/device checks honestly.

## Git and pull request rules

- Work on the active feature branch specified in `CONTINUE.md`; do not commit milestone work directly to `main`.
- Keep changes focused and commits understandable.
- Keep PR #1 in draft until all MOT0.1 acceptance criteria and CI checks pass on the latest commit.
- Do not merge, publish a release, or bump a consuming app's dependency/submodule unless explicitly requested.
- Update `CONTINUE.md`, the implementation plan, and the PR description when the branch state or remaining work materially changes.
