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

**Status:** planning; implementation not started.

The next phase is to add a separate `:motion-lab` Android sample app that depends on `:motion` and demonstrates its public API using generic content and Material 3. This does not create the planned `:motion-ui` component library and does not change Motion Core behavior.

Important discovery: `lab/` is not a Gradle module. Its prototype sources still use Solstice/Breezy packages, activities, resources, theme classes, and LGPL-3.0 notices. They are references, not code to bulk-move or mechanically rename. Reimplement demos cleanly against the public Motion API; preserve notices and verify provenance/license compatibility before reusing any implementation code.

### Next implementation steps

1. Add the `:motion-lab` Android application plugin/module and make it depend on `:motion`.
2. Build a neutral Material 3 theme, entry activity, and basic gallery/navigation scaffold.
3. Port one stable demo at a time, starting with public APIs already covered by Core tests.
4. Add meaningful Compose/Material baseline comparisons for selected interactions, keeping state and input equivalent.
5. Add safe slow-motion controls, sample-app build/run instructions, and a CI job for `:motion-lab:assembleDebug`.
6. Run the complete workflow once on the final coherent branch head; report emulator/device checks separately.

See [docs/MOT0.2_PLAN.md](docs/MOT0.2_PLAN.md) for detailed scope and acceptance criteria. Follow [docs/MOTION_LAB_PLAN.md](docs/MOTION_LAB_PLAN.md) for the gallery and comparison direction; don't claim any of that design is implemented until it exists.

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
