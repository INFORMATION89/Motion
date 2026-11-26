# Continue

This file is the working handoff for the next development session. Keep it current when the milestone, branch, CI state, or immediate next steps change. It is an execution guide, not a substitute for the design documents.

## Read first

1. [Agent instructions](AGENTS.md) — repository-wide constraints for AI coding assistants.
2. [Framework overview](docs/FRAMEWORK_OVERVIEW.md) — what belongs in Motion and what belongs in app/UI libraries.
3. [Platform support](docs/PLATFORM_SUPPORT.md) — current Android integration and future platform requirements.
4. [Solstice AI adoption guide](docs/AI_SOLSTICE_ADOPTION.md) — how to create the adoption plan without prematurely migrating code.
5. [MOT0.1 plan](docs/MOT0.1_PLAN.md) — code tasks, tests, and acceptance criteria.
6. [Motion rules](docs/MOTION.md), [morph study](docs/MORPH_STUDY.md), and [module API](motion/README.md).

## Project guardrails

- Motion is an independent, reusable Android/Jetpack Compose motion framework. It must not depend on Solstice or import application code.
- Motion is not a UI kit. Reusable interaction mechanics belong here; app components, styling, navigation policy, and product behavior remain with the app or its UI libraries.
- Compose remains the animation and interaction foundation. Do not introduce a separate universal physics engine.
- Prioritize interruptible motion, continuous velocity, predictable gesture ownership, and explicit input contracts.
- Desktop and iOS are not currently supported. A multiplatform port requires a separate design and verification plan.
- Keep milestone identifiers in the `MOT` family. MOT0.1 remains limited to core reliability, focused tests, and directly related docs.

## Consolidated work

**Status:** open draft pull request; not merged. The consolidated branch contains the MOT0.1 implementation work plus the README, AI-agent, framework-boundary, platform, and Solstice-planning documentation.

- Branch: `work/consolidated-docs-platforms`
- MOT0.1 remains draft until the MorphMotion interruption, reversal, target/source retargeting, gesture supersession, and non-finite input contracts are covered by deterministic tests.
- The earlier CI run for the MOT0.1 base commit passed, but the consolidated branch must have its own full CI result checked before it can be considered green.
- The former standalone README and cost-aware-verification PRs are intended to be superseded by this consolidated change.

### Already implemented in MOT0.1

- Validate finite `MotionValue` inputs and valid non-negative configuration.
- Copy, sort, and deduplicate `restPoints`; reject empty or non-finite values.
- Keep flick thresholds ordered and update them safely when the drag range changes.
- Validate projection and target-selection inputs in `MotionPhysics`.
- Include position as well as dimensions in `MorphMotion.progress`.
- Retarget an active morph when its destination changes and guard pending open/close continuations against stale commands.
- Add focused tests for validation, rest-point normalization, physics inputs, and geometry-based morph progress.

### Remaining MOT0.1 work

Start with deterministic interruption tests in `MorphMotionTest.kt`:

1. Open→close and close→open reversals; repeated commands must leave the latest intent in control.
2. Commands issued before initial placement finishes must not be overwritten by stale continuations.
3. Visibility must remain correct while closing or caught.
4. Target changes while opening/closed/open/settled/caught must follow the contracts in `docs/MOT0.1_PLAN.md`.
5. Source changes while closing must retarget; source changes while open must be deferred until close.
6. Drag, scrub, catch, and release must supersede stale work and preserve gesture ownership.
7. Reject non-finite `MorphMotion` drag/scrub/release inputs before mutating state or launching work.
8. Validate anchor geometry while allowing legitimate zero-size/transient anchors.

Use deterministic frame-clock tests. Do not use wall-clock sleeps, expose public velocity test hooks, or weaken assertions to make CI pass.

## Saturday session — 2026-11-28

1. Verify the latest branch head and its exact GitHub Actions run.
2. Start with Task 1 in [the MOT0.1 plan](docs/MOT0.1_PLAN.md).
3. Add a failing regression test before changing production code.
4. Implement the smallest change that satisfies the contract.
5. Complete the remaining cases above, review the full diff, and run the full CI workflow once for the coherent batch.
6. Keep device checks marked `NOT TESTED` / `DEFERRED` while hardware access is unavailable.

## Local setup and verification

The repository specifies JDK 21 and Android SDK / Build Tools 37.

On Windows PowerShell:

```powershell
git fetch origin
git switch work/consolidated-docs-platforms
git pull --ff-only
git status --short --branch
java -version
.\gradlew.bat --version
.\gradlew.bat spotlessCheck
.\gradlew.bat :motion:testDebugUnitTest
.\gradlew.bat :motion:assembleRelease
```

On macOS/Linux, use `./gradlew` instead of `gradlew.bat`.

The complete GitHub Actions workflow runs:

```bash
./gradlew spotlessCheck
./gradlew :motion:testDebugUnitTest
./gradlew :motion:assembleRelease
```

Use the exact branch-head SHA and its CI result as evidence. Physical-device checks are separate from CI and remain deferred while hardware is unavailable.
