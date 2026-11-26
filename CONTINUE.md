# Continue

This file is the working handoff for the next development session. Keep it current when the milestone, branch, CI state, or immediate next steps change. It is an execution guide, not a substitute for the design documents.

## Read first

1. [Agent instructions](AGENTS.md) — repository-wide constraints for AI coding assistants.
2. [MOT0.2 plan](docs/MOT0.2_PLAN.md) — current milestone scope, phases, and acceptance criteria.
3. [Framework overview](docs/FRAMEWORK_OVERVIEW.md) — what belongs in Motion and what belongs in app/UI libraries.
4. [Platform support](docs/PLATFORM_SUPPORT.md) — current Android integration and future platform requirements.
5. [Solstice AI adoption guide](docs/AI_SOLSTICE_ADOPTION.md) — how to create the adoption plan without prematurely migrating code.
6. [Motion principles](docs/MOTION.md), [Morph study](docs/MORPH_STUDY.md), [Motion Lab design](docs/MOTION_LAB_PLAN.md), and [module API](motion/README.md).

## Project guardrails

- Motion is an independent, reusable Android/Jetpack Compose framework. It must not depend on Solstice or import application code.
- Motion Core owns behavior. The planned optional Motion UI layer may provide configurable components; app branding, navigation policy, screens, and product behavior remain with consuming apps.
- Compose remains the animation and interaction foundation. Do not introduce a separate universal physics engine.
- Prioritize interruptible motion, continuous velocity, predictable gesture ownership, and explicit input contracts.
- Desktop and iOS are not currently supported. A multiplatform port requires a separate design and verification plan.
- Do not change agreed, user-visible motion behavior or add a new public Core API without a documented proposal and owner agreement.

## Current verified baseline

- `main`: `b5fb7892b0b63e6e180b365496804198269a739a`.
- MOT0.1 is complete and merged. Its core reliability changes and tests are on `main`.
- The latest `main` workflow at the time of this handoff is [run #45767829970](https://github.com/INFORMATION89/Motion-UI/actions/runs/45767829970), completed successfully on the exact `main` SHA above.
- No other open PRs or issues were returned before this work; the current MOT0.2 planning PR is #7 and remains a draft.
- Current library platform baseline: Android / Jetpack Compose, min SDK 29, compile SDK 37, JDK 21.
- Device/emulator interaction has not been validated in this handoff; continue to report it as `NOT TESTED` / `DEFERRED`.

## MOT0.1 — complete

Core reliability work is merged. Do not reopen it without a reproduced regression or a separate, scoped requirement. The historical acceptance plan remains in [docs/MOT0.1_PLAN.md](docs/MOT0.1_PLAN.md).

## MOT0.2 — standalone Motion Lab

**Status:** implementation in progress on [PR #7](https://github.com/INFORMATION89/Motion-UI/pull/7), still draft.

The separate `:motion-lab` Android sample app is now included in Gradle and depends on `:motion`. It has its own namespace, application ID, launcher activity, and neutral Material 3 theme. Its initial gallery links to five generic demonstrations:

- Spring tokens and gesture release.
- Source-to-target morph.
- Spring-driven pager movement and overshoot, with an opt-in synchronized Motion-vs-Compose comparison.
- Dial interaction and snapping.
- Horizontal scroll edge blur.

The slow-motion control adjusts `MotionTime.scale` and the root composition restores it to `1f` on disposal. The old `lab/` source tree remains an unbuilt Solstice-era prototype; it is not a source dependency of the new app.

### Current verification state

- CI run [#80](https://github.com/INFORMATION89/Motion-UI/actions/runs/45771497832) passed Spotless, Core unit tests, `:motion:assembleRelease`, and `:motion-lab:assembleDebug` on the exact previous PR head `89749a4a7f44bc3c6f0cb347b9b3e11833ad95da`. A new synchronized page-comparison mode has since been committed and requires a fresh CI result on the updated head.
- The CI workflow includes `:motion-lab:assembleDebug` in addition to Spotless, Core unit tests, and library release assembly.
- No local Gradle build was run in this workspace.
- Emulator and physical-device interaction remain `NOT TESTED` / `DEFERRED`.

### Next steps

1. Run and inspect the check-only CI workflow on the new page-comparison commit; resolve any formatting or build issues.
2. Review the full diff for module isolation, accessibility, Motion API usage, and dependency scope.
3. Add the next useful comparison for Morph, then assess whether a sheet-like example fits the existing public API without introducing a new Core API.
4. Add focused UI/sample tests only where they validate behavior not already covered by Core tests.
5. Keep the PR draft until the app and comparisons build and the required workflow passes.

See [docs/MOT0.2_PLAN.md](docs/MOT0.2_PLAN.md) for scope and acceptance criteria. Follow [docs/MOTION_LAB_PLAN.md](docs/MOTION_LAB_PLAN.md) for the longer-term gallery and comparison direction.

## Local setup and verification

The repository specifies JDK 21 and Android SDK / Build Tools 37.

On Windows PowerShell:

```powershell
git fetch origin
git switch main
git pull --ff-only
git status --short --branch
java -version
.\gradlew.bat --version
.\gradlew.bat spotlessCheck
.\gradlew.bat :motion:testDebugUnitTest
.\gradlew.bat :motion:assembleRelease
```

On macOS/Linux, use `./gradlew` instead of `gradlew.bat`.

After MOT0.2 adds the app module, also run:

```bash
./gradlew :motion-lab:assembleDebug
```

Use the exact branch-head SHA and its CI result as evidence. Physical-device and emulator checks are separate from CI and remain deferred until actually run.
