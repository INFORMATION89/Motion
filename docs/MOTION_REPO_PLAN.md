# Motion repository plan

This document preserves the original repository-extraction decisions from 2026-11-21. The repository is now [INFORMATION89/Motion-UI](https://github.com/INFORMATION89/Motion-UI); the current implementation state below takes precedence over historical target-state descriptions in the plan.

## Current state — 2026-11-26

- Motion is an independent Android/Jetpack Compose project with a standalone Gradle build and three included modules: the reusable library `:motion`, optional UI library `:motion-ui`, and standalone sample application `:motion-lab`.
- The module uses the Android library Gradle plugin and Jetpack Compose Animation/Foundation. It is not a Kotlin Multiplatform project.
- The current target is Android 10 / API 29+, compile SDK 37, Android Build Tools 37, and JDK 21.
- `:motion-lab` is a built Gradle application module and is included in CI through `:motion-lab:assembleDebug`. The legacy `lab/` directory remains a separate, unbuilt prototype and is not the source of the new app.
- CI runs Spotless, Core unit tests/release build, Motion UI unit tests/release build, and the Motion Lab debug build in the required build workflow.
- the consuming application consumes Motion through a Git submodule and Gradle composite build. It pins the Motion commit through the submodule pointer.
- Desktop (Windows/macOS/Linux) and iOS support are future design work, not supported targets.
- Physical-device and Pixel validation remain `NOT TESTED` / `DEFERRED` while the required hardware is unavailable. Passing CI does not establish device behavior.

## MOT0.3A status — in progress

- The optional `:motion-ui` Android library is now included in Gradle and depends on `:motion`; Core has no dependency on UI.
- Its initial public API is `MotionUiTheme`, `MotionUiThemeTokens.current`, and configurable semantic shape, spacing, and surface-material tokens.
- Neutral defaults derive surface colors from the caller's Material 3 color scheme. Apps can inject their own Material 3 theme values and replace Motion UI tokens.
- No Haze dependency, glass rendering implementation, component surfaces, consumer code, or submodule changes are included in this first foundation batch.
- Exact acceptance remains pending required CI on the final PR head.

## Original decisions (2026-11-21)

- **Workflow: Git submodule + Gradle composite build.** the consuming application keeps Motion at `motion/` as a submodule pinned to an exact Motion commit and builds it from source through `includeBuild`. Nothing is currently published to Maven.
- **Independence:** Motion must not import consumer code or depend on any consuming app. Generic motion behavior belongs in Motion; app styling, screens, assets, and product logic remain in the consuming app.
- **History:** the extracted Motion repository keeps the project-owned Motion history, not the upstream upstream project Weather history.
- **Motion Lab direction (historical):** a separate Material You Expressive app was planned to demonstrate Motion with standard Material 3 components rather than the consuming application's visual styling. This plan has since been delivered as `:motion-lab`; the old `lab/` prototype remains separate.

## Intended repository structure

The intended structure remains:

```text
Motion-UI/
  motion/       Android motion library (Gradle module :motion)
  motion-lab/   standalone Android sample app (Gradle module :motion-lab)
  lab/          legacy prototype sources; not built or included in CI
  docs/         architecture, motion rules, research, and plans
  gradle/       wrapper and version catalog
  .github/      CI
  LICENSE       Apache-2.0
  README.md     project entry point
```

Do not describe the legacy `lab/` directory as a built or CI-validated app. The current standalone app is `:motion-lab`, included in Gradle and verified by the required CI build.

## How changes reach the consuming application

1. Make and test a reusable change in Motion.
2. Confirm the Motion CI workflow passes on the exact commit intended for adoption.
3. In the consuming application, fetch Motion and check out that exact tested commit inside the `motion/` submodule.
4. Build and test the consuming application with that pointer.
5. Commit the updated submodule pointer in the consuming application with a message that identifies the SHA and reason.

A change should be prototyped in the consuming application only when useful, but the generic implementation must land in Motion first. Do not copy app code into Motion. Prefer an exact commit checkout over an unreviewed moving branch update.

## Original extraction candidates

The 2026-11-21 inventory identified these areas for review:

| the consuming application area | Potential reusable behavior | App-specific boundary |
|---|---|---|
| Shell/page navigation | page movement, settle, and generic gesture mechanics | route policy, screens, page content |
| Toolbar interaction | pill movement, press, and transitions between states | toolbar composition, glass/sky look, icons, app state |
| Segmented-row press morph | reusable press/morph behavior | row content, labels, settings semantics |
| Sheet state | detents, drag, and source-to-sheet mechanics | search UI, screen state, product-specific content |

Treat this as a historical candidate list. Re-inspect current consumer source, call sites, and tests before planning a move. See [the AI adoption guide](AI_SOLSTICE_ADOPTION.md).

## Planned phases

1. **Library reliability — complete:** core motion state, physics, validation, and interruption contracts have focused deterministic tests.
2. **Motion Lab — complete:** the standalone `:motion-lab` app is included in Gradle and required CI, with Material 3 Components / Interactions destinations and automatic system light/dark appearance.
3. **Reusable interaction extraction — next engineering phase:** evaluate generic behavior from real consumer use cases, move one capability at a time, and add focused tests plus a Lab example when useful. Do not assume every UI interaction belongs in Motion Core.
4. **Consumer workflow — documented:** contribution, exact-commit version pinning, and the the consuming application update process are described. Actual the consuming application submodule updates remain separate, explicitly reviewed consumer changes.

Each phase should have a scoped plan, acceptance criteria, cost-aware verification, and an explicit list of deferred device checks. Do not expand a milestone into unrelated UI components, platform ports, or benchmarks.
