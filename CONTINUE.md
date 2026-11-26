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

**Status:** PR #4 is open as a draft; not merged. The consolidated branch contains the MOT0.1 implementation work plus the README, AI-agent, framework-boundary, platform, Solstice-planning, and cost-aware verification guidance.

- Branch: `work/consolidated-docs-platforms`
- MOT0.1 remains draft until the MorphMotion interruption, reversal, target/source retargeting, gesture supersession, and non-finite input contracts are covered by deterministic tests.
- Latest PR head checked: `ab02bbb6d2a627802e4f1c428fef3247e59bb335`.
- GitHub Actions run [#38](https://github.com/INFORMATION89/Motion-UI/actions/runs/45737129108) failed at `:motion:spotlessKotlinCheck` because `MorphMotionTest.kt` needs one blank line between tests. Unit tests and library build were skipped; neither has a result for this head.
- Fix the exact formatting issue as part of the next coherent batch. Do not rerun the full workflow just for documentation edits; after the remaining MOT0.1 code/test batch is complete, run the required workflow once on its exact final head.
- PRs #1–#3 were consolidated into PR #4. Do not treat their old branch names, SHAs, or CI runs as current evidence.

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

## Next work session

1. Confirm the current branch head and inspect the exact CI run before changing code.
2. Fix the known Spotless violation in `MorphMotionTest.kt` (one blank line between test methods) in the next coherent change batch.
3. Continue with Task 1 in [the MOT0.1 plan](docs/MOT0.1_PLAN.md): deterministic interruption/reversal tests.
4. Add a failing regression test before changing production code; implement the smallest fix that satisfies the documented contract.
5. Complete retargeting, source deferral, gesture supersession, numeric-boundary, and scalar-contract audits.
6. Review the complete diff, update this handoff and the PR description, then run the full required CI workflow once on the final coherent code batch.
7. Keep device checks marked `NOT TESTED` / `DEFERRED` while hardware access is unavailable.

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
