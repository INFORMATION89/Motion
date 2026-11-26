# Continue

This file is the working handoff for the next development session. Keep it current when the milestone, branch, CI state, or immediate next steps change. It is an execution guide, not a substitute for the design documents.

## Read first

1. [Project rules](docs/RULES.md) — how Motion work is planned, implemented, reviewed, and accepted.
2. [Agent instructions](AGENTS.md) — repository-wide constraints for AI coding assistants.
3. [MOT0.2 plan](docs/MOT0.2_PLAN.md) — completed milestone scope and acceptance evidence.
4. [MOT0.3 proposal](docs/MOT0.3_PLAN.md) — proposed optional Motion UI module and staged component work.
5. [Framework overview](docs/FRAMEWORK_OVERVIEW.md) — what belongs in Motion and what belongs in app/UI libraries.
6. [Platform support](docs/PLATFORM_SUPPORT.md) — current Android integration and future platform requirements.
7. [Solstice AI adoption guide](docs/AI_SOLSTICE_ADOPTION.md) — how to create the adoption plan without prematurely migrating code.
8. [Motion principles](docs/MOTION.md), [Morph study](docs/MORPH_STUDY.md), [Motion Lab design](docs/MOTION_LAB_PLAN.md), and [module API](motion/README.md).

## Project guardrails

- Motion is an independent, reusable Android/Jetpack Compose framework. It must not depend on Solstice or import application code.
- Motion Core owns behavior. The planned optional Motion UI layer may provide configurable components; app branding, navigation policy, screens, and product behavior remain with consuming apps.
- Compose remains the animation and interaction foundation. Do not introduce a separate universal physics engine.
- Prioritize interruptible motion, continuous velocity, predictable gesture ownership, and explicit input contracts.
- Desktop and iOS are not currently supported. A multiplatform port requires a separate design and verification plan.
- Do not change agreed, user-visible motion behavior or add a new public Core API without a documented proposal and owner agreement.

## Current verified baseline

- `main`: `dac8df73387e87839192b867cfc2ed57b8df4561` (PR #11 squash merge).
- MOT0.1 is complete and merged.
- MOT0.2 is complete and merged through PR #7; its follow-up gallery organization and automatic system theme are merged through PR #8.
- PR #9 (Project Rules), PR #7 (Standalone Motion Lab), PR #8 (Components / Interactions and system theme), PR #10 (MOT0.2 documentation closeout), and PR #11 (repository baseline refresh) are all merged. There are no open Motion PRs or open issues at this handoff.
- Latest verification: CI #112 passed on PR #11's exact head `c4656f6da9317ae4a80f29948721bbc6a70b6560`, including the complete `Verify and build` step. PR #11's squash merge commit is `dac8df73387e87839192b867cfc2ed57b8df4561`. CI #108 passed on the exact PR #8 head; CI #110 passed for PR #10.
- Current library platform baseline: Android / Jetpack Compose, min SDK 29, compile SDK 37, JDK 21. iOS and desktop (Windows, macOS, Linux) remain unsupported.
- Emulator and physical-device validation, TalkBack runtime behavior, contrast inspection, and real haptic output remain `NOT TESTED` / `DEFERRED`. CI success is not device validation.

## MOT0.1 — complete

Core reliability work is merged. Do not reopen it without a reproduced regression or a separate, scoped requirement. The historical acceptance plan remains in [docs/MOT0.1_PLAN.md](docs/MOT0.1_PLAN.md).

## MOT0.2 — complete

The standalone `:motion-lab` Android sample is merged into `main`. It depends on `:motion`, has its own application identity and launcher activity, and remains independent of Solstice and the planned `:motion-ui` module.

The Lab currently includes:
- Components and Interactions top-level Material 3 tabs.
- Automatic light/dark appearance following the system setting.
- Spring/release, Morph, page movement, dial, semantic haptics, and scroll edge-blur demonstrations.
- Synchronized Motion-vs-Compose comparisons for Morph and page movement.
- Slow-motion control that restores `MotionTime.scale` when the root composition is disposed.

PR #7 delivered the standalone app foundation and demos. PR #8 added the two-section navigation and system theme. Both are merged. The exact PR #8 head passed CI #108 before squash merge. See [docs/MOT0.2_PLAN.md](docs/MOT0.2_PLAN.md) for milestone scope and [docs/MOTION_LAB_PLAN.md](docs/MOTION_LAB_PLAN.md) for the longer-term Lab direction.

### Remaining validation

- Install and exercise the Lab on an emulator and a physical Android device.
- Verify tab navigation, demo interactions, light/dark transitions, screen-reader labels and traversal, and contrast.
- Verify real haptic output on supported hardware and record device/API details.
- Keep all of the above marked deferred until actually performed.

## Next milestone — MOT0.3 proposal

MOT0.2 is closed. The next proposed engineering milestone is the optional `:motion-ui` layer, scoped in [docs/MOT0.3_PLAN.md](docs/MOT0.3_PLAN.md). This is a proposal, not implementation authorization. Review its token/theme contract, first component APIs, Haze dependency gate, and reduced-motion behavior with the owner before adding the module. Keep UI optional and separate from Core; do not begin a multiplatform port or add public APIs without a documented gap, contract, tests, and owner agreement.

Opus review remains optional for a later high-complexity or high-risk task; it is not a blocker for routine work.

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

For MOT0.2, also run:

```bash
./gradlew :motion-lab:assembleDebug
```

Use the exact branch-head SHA and its CI result as evidence. Physical-device and emulator checks are separate from CI and remain deferred until actually run.
