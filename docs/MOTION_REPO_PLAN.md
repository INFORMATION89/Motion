# Motion repository plan

This document preserves the original repository-extraction decisions from 2026-11-21. The repository is now [INFORMATION89/Motion-UI](https://github.com/INFORMATION89/Motion-UI); the current implementation state below takes precedence over historical target-state descriptions in the plan.

## Current state — 2026-11-25

- Motion is an independent Android library with a standalone Gradle build and a single included library module, `:motion`.
- The module uses the Android library Gradle plugin and Jetpack Compose Animation/Foundation. It is not a Kotlin Multiplatform project.
- The current target is Android 10 / API 29+, compile SDK 37, Android Build Tools 37, and JDK 21.
- `lab/` contains prototype sources; it is not currently included as a Gradle module or CI target.
- CI runs Spotless, `:motion:testDebugUnitTest`, and `:motion:assembleRelease`.
- Solstice consumes Motion through a Git submodule and Gradle composite build. It pins the Motion commit through the submodule pointer.
- Desktop (Windows/macOS/Linux) and iOS support are future design work, not supported targets.
- Physical-device and Pixel validation remain `NOT TESTED` / `DEFERRED` while the required hardware is unavailable. Passing CI does not establish device behavior.

## Original decisions (2026-11-21)

- **Workflow: Git submodule + Gradle composite build.** Solstice keeps Motion at `motion/` as a submodule pinned to an exact Motion commit and builds it from source through `includeBuild`. Nothing is currently published to Maven.
- **Independence:** Motion must not import Solstice code or depend on any consuming app. Generic motion behavior belongs in Motion; app styling, screens, assets, and product logic remain in the consuming app.
- **History:** the extracted Motion repository keeps the project-owned Motion history, not the upstream Breezy Weather history.
- **Motion Lab direction:** a separate Material You Expressive app was planned to demonstrate Motion with standard Material 3 components rather than Solstice's visual styling. The lab is not yet a Gradle module.

## Intended repository structure

The intended structure remains:

```text
Motion-UI/
  motion/       Android motion library (Gradle module :motion)
  lab/          prototype sources; future standalone demo app/module
  docs/         architecture, motion rules, research, and plans
  gradle/       wrapper and version catalog
  .github/      CI
  LICENSE       Apache-2.0
  README.md     project entry point
```

Do not describe `lab/` as a built or CI-validated app until it is included in the Gradle build and has its own verification.

## How changes reach Solstice

1. Make and test a reusable change in Motion.
2. Confirm the Motion CI workflow passes on the exact commit intended for adoption.
3. In Solstice, fetch Motion and check out that exact tested commit inside the `motion/` submodule.
4. Build and test Solstice with that pointer.
5. Commit the updated submodule pointer in Solstice with a message that identifies the SHA and reason.

A change should be prototyped in Solstice only when useful, but the generic implementation must land in Motion first. Do not copy app code into Motion. Prefer an exact commit checkout over an unreviewed moving branch update.

## Original extraction candidates

The 2026-11-21 inventory identified these areas for review:

| Solstice area | Potential reusable behavior | App-specific boundary |
|---|---|---|
| Shell/page navigation | page movement, settle, and generic gesture mechanics | route policy, screens, page content |
| Toolbar interaction | pill movement, press, and transitions between states | toolbar composition, glass/sky look, icons, app state |
| Segmented-row press morph | reusable press/morph behavior | row content, labels, settings semantics |
| Sheet state | detents, drag, and source-to-sheet mechanics | search UI, screen state, product-specific content |

Treat this as a historical candidate list. Re-inspect current Solstice source, call sites, and tests before planning a move. See [the AI adoption guide](AI_SOLSTICE_ADOPTION.md).

## Planned phases

1. **Library reliability:** complete the core motion state, physics, validation, and interruption contracts with focused deterministic tests.
2. **Motion Lab:** make the prototype a standalone, independently verifiable app using standard Material 3 components.
3. **Reusable interaction extraction:** move generic behavior into Motion one piece at a time, each with tests and a corresponding Lab example when that improves validation.
4. **Consumer workflow:** document contribution, version pinning, and the Solstice update process.

Each phase should have a scoped plan, acceptance criteria, cost-aware verification, and an explicit list of deferred device checks. Do not expand a milestone into unrelated UI components, platform ports, or benchmarks.
