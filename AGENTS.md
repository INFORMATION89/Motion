# Repository instructions for AI agents

Read this file before making changes. Then read [README.md](README.md), [the framework overview](docs/FRAMEWORK_OVERVIEW.md), [platform support](docs/PLATFORM_SUPPORT.md), [the Solstice adoption guide](docs/AI_SOLSTICE_ADOPTION.md), [motion principles](docs/MOTION.md), and the relevant implementation or design documents under `docs/`.

## Project boundaries

- This repository contains the reusable Motion framework. It is independent of Solstice and must remain useful to unrelated apps.
- The current artifact is an Android library for Jetpack Compose. Do not claim iOS, Windows, macOS, or Linux support until corresponding Gradle targets, platform implementations, tests, and CI exist.
- Never add Solstice, weather-domain, or other consumer-app dependencies, source code, assets, or assumptions to Motion.
- Motion Core (`:motion`) owns reusable motion and interaction behavior. The planned Motion UI (`:motion-ui`) layer may own configurable, cross-app component building blocks, but not a complete application framework, product screens, visual brand, navigation policy, or domain logic. Motion UI depends on Core; Core never depends on Motion UI. Follow [the framework overview](docs/FRAMEWORK_OVERVIEW.md) when deciding where code belongs.
- Compose remains the animation, rendering, and interaction foundation. Use Compose primitives such as `Animatable`, `spring`, and gesture APIs; do not invent a second general-purpose animation engine.
- Motion-specific behavior and interaction decisions must follow `docs/MOTION.md`. Do not change agreed, user-visible motion behavior without documenting the trade-off and obtaining the owner's agreement.
- Keep the library API small and intentional. Do not add public inspection hooks, configuration knobs, dependencies, or abstractions solely to simplify one test.

## Current milestone boundary

MOT0.1 is limited to reliability of the core motion state and physics, focused regression tests, and directly related documentation.

Do not fold these into MOT0.1:
- Motion Lab implementation or redesign.
- New animation/interaction components unrelated to the reliability contracts.
- Benchmarks or performance dashboards.
- Solstice-specific UI or migration work.
- A multiplatform conversion.
- Broad refactors unrelated to a failing test or a documented contract gap.

Follow the ordered tasks in [docs/MOT0.1_PLAN.md](docs/MOT0.1_PLAN.md). Start with deterministic regression tests for interruption and reversal in `MorphMotion`, then implement the smallest change that satisfies the intended contract.

## Implementation and test discipline

- Prefer a failing regression test before changing production code.
- For animation tests, use a deterministic `MonotonicFrameClock` and advance frames deliberately. Do not use wall-clock sleeps or timing-dependent assertions.
- Test observable behavior: final anchor, current bounds, visibility, open/closed intent, and whether motion settles. Do not expose internal velocity as public API just for assertions.
- Retarget from current animated values and preserve each `Animatable`'s current velocity when starting a replacement spring.
- New commands and gestures must supersede stale work. Check pending coroutines and initial placement races, not only the happy path.
- Validate non-finite numeric input before mutating state or launching work. Do not silently turn `NaN` or infinity into a plausible value.
- Keep intentional finite high-velocity clamps; validate raw input before clamping.
- Preserve legitimate zero-size/transient layout anchors if validating `MorphAnchor`; reject invalid geometry rather than silently normalizing it.
- Never weaken or delete a regression assertion merely to make CI green.

## AI-assisted planning and implementation

- Verify the current branch, submodule commit, open PRs, source tree, and latest CI before relying on earlier plans or chat context.
- For Solstice adoption work, follow `docs/AI_SOLSTICE_ADOPTION.md`. The first deliverable is an evidence-based migration plan in Solstice, not an automatic code migration or submodule bump.
- Inspect existing APIs before proposing new ones. Classify each candidate as direct reuse, app-side composition, a reusable Motion gap, or app-specific behavior.
- Prefer adapting existing components and libraries over rebuilding equivalents. Avoid duplicating spring constants, gesture physics, or component implementations in consuming apps.
- Do not broaden platform support as incidental cleanup. A platform proposal must identify source-set boundaries, platform APIs, tests, and CI requirements first.

## Cost-aware verification

1. **Plan before running checks.** Identify changed modules, acceptance criteria, the smallest useful checks, expensive checks, and device/integration requirements.
2. **Use focused checks while iterating.** Run the affected unit-test or Gradle task rather than the full workflow after every small edit. For documentation-only changes, do not run code builds that the change cannot affect.
3. **Batch coherent changes.** Complete a related implementation/documentation batch before requesting the full pull-request CI workflow. The current CI workflow runs Spotless, `:motion:testDebugUnitTest`, and `:motion:assembleRelease`; run it once when a coherent code batch is ready.
4. **Reuse valid results.** A passing result remains valid for the same commit and relevant environment. Do not rerun checks that the change cannot affect. When one job fails, inspect its logs and retry only that job where possible.
5. **Limit infrastructure retries.** Diagnose the first failure. For a genuine infrastructure failure, allow at most one retry using a materially different setup. If it fails again, stop and report the blocker instead of repeatedly spending CI resources.
6. **Keep device evidence separate.** Physical-device and Pixel checks are `NOT TESTED` / `DEFERRED` while hardware is unavailable. CI does not prove touch behavior or physical-device compatibility.
7. **Report evidence precisely.** Record the exact commit SHA, CI run, checks, results, skipped checks, and blockers. Never claim a build, test, platform, or device check passed unless its result was inspected.

## Git and pull request rules

- Work on a focused branch and use a pull request; do not push development work directly to `main`.
- Keep related changes together and commits understandable.
- Keep MOT0.1 in draft until every acceptance criterion in `docs/MOT0.1_PLAN.md` is covered by tests and the complete CI workflow passes on the exact final branch head.
- Do not merge, release, publish, tag, or bump a consuming app's submodule unless explicitly requested.
- Update `CONTINUE.md`, implementation plans, and PR descriptions when branch state or remaining work materially changes.
