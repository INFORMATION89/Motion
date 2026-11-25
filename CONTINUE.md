# Continue

This is the working handoff for the next development session. Keep it current when the milestone, branch, CI state, or immediate next steps change. It is an execution guide, not a substitute for the design documents.

## Read first

1. [Agent instructions](AGENTS.md) — repository-wide constraints for AI coding assistants.
2. [MOT0.1 implementation plan](docs/MOT0.1_PLAN.md) — exact code tasks, test cases, and acceptance criteria for the next coding session.
2. [Motion rules](docs/MOTION.md) — interaction principles and agreed behavior.
3. [Morph study](docs/MORPH_STUDY.md) — rationale and constraints for morphing surfaces.
4. [Repository plan](docs/MOTION_REPO_PLAN.md) — independence from Solstice and the app integration workflow.
5. [Root README](README.md) and [module README](motion/README.md) — repository setup and current library surface.

## Project guardrails

- Motion is an independent, reusable motion framework for Compose apps. It must not depend on Solstice or import application code.
- Compose remains the animation and interaction foundation. Do not introduce a separate universal physics engine or replace Compose primitives without a concrete need.
- Prioritize interruptible motion, continuous velocity, predictable gesture ownership, and explicit input contracts.
- Keep milestone identifiers in the `MOT` family (for example, `MOT0.1`).
- MOT0.1 is limited to the core library, its tests, and directly relevant documentation. Do not expand it into Motion Lab, new UI components, or benchmarks.
- Do not change the agreed motion feel or public semantics just to make an implementation easier. Add a focused test and explain the trade-off before proposing a behavior change.

## Current work: MOT0.1

**Status:** implementation is in an open draft pull request; it is not merged.

- Branch: `mot-0.1/core-validation`
- Pull request: [#1 — MOT0.1: validate MotionValue and physics inputs](https://github.com/INFORMATION89/Motion-UI/pull/1)
- Baseline commit `c636413e1e4790675c38ea36e8c3c22ac84d0aa7` passed GitHub Actions run `45674503787`: Spotless, unit tests, and library build.
- The execution plan was added in `d0ee112c3ffd163c2ecd554b18ad9a0f6e5c9929` and refined in `617068f08c3a9a778e490d8758a9227fdfe95d4b`; `AGENTS.md` was added in `79544d011d6ad008ca8b5dd771d1faaed696d75e`.
- Current branch head before this handoff refresh: `8d875ad2e1d0ad8f05899a7fefb1b6037db33c63`; its GitHub Actions run `45703168869` was in progress. Recheck the exact branch head before coding or marking anything green.
- The PR remains draft while the MorphMotion interruption and retargeting coverage is incomplete.

### Implemented

- Validate finite `MotionValue` inputs and valid non-negative configuration.
- Copy, sort, and deduplicate `restPoints`; reject empty or non-finite values.
- Keep flick thresholds ordered and update them safely when the drag range changes.
- Validate projection and target-selection inputs in `MotionPhysics`.
- Include position as well as dimensions in `MorphMotion.progress`.
- Retarget an active morph when its destination changes, and guard pending open/close continuations against stale commands.
- Add tests for validation, rest-point normalization, physics inputs, and geometry-based morph progress.

### Agreed MOT0.1 behavior

- Invalid numeric inputs and invalid configurations fail eagerly with `IllegalArgumentException`.
- `restPoints` are defensively copied, finite, non-empty, sorted, and deduplicated.
- `slowFlick` and `fastFlick` are finite and non-negative; `fastFlick >= slowFlick`.
- `deceleration` must be finite and in the open interval `(0, 1)`.
- Changing `restPoints` does not retarget an animation already in progress; the new list applies to the next interaction.
- A new motion command starts from the current animated state and preserves velocity where the API supports it.
- `animateTo()` animates even when the requested destination equals the current target.
- `MorphMotion` target changes retarget from the current geometry and velocity. Source changes while the morph is open are deferred until the next close.
- Non-finite drag deltas are rejected rather than allowed to corrupt the motion state.

## Saturday coding session — 2026-11-28

Start with [the MOT0.1 plan](docs/MOT0.1_PLAN.md), Task 1: deterministic interruption tests in `MorphMotionTest.kt`.

Order:
1. Verify the latest branch head and its CI run; do not rely on a run for an older SHA.
2. Add failing tests for open/close reversal, repeated commands, and pending initial placement.
3. Implement only the behavior required to satisfy those tests.
4. Test target retargeting, source deferral, velocity continuity, and gesture supersession.
5. Close the remaining non-finite input boundaries in `MorphMotion`.
6. Review the full diff and rerun all CI checks before changing the PR out of draft.

Detailed scenarios and acceptance criteria are in `docs/MOT0.1_PLAN.md`.

## Local setup and verification

The repository specifies **JDK 21** and **Android SDK / Build Tools 37**. This session cannot inspect the developer machine, so verify these locally before the Saturday session.

On Windows PowerShell, from the repository root:

```powershell
git fetch origin
# If the branch already exists locally:
git switch mot-0.1/core-validation
git pull --ff-only
# If it does not exist locally, use this instead of the two switch/pull lines:
# git switch --track -c mot-0.1/core-validation origin/mot-0.1/core-validation

git status --short --branch
java -version
.\gradlew.bat --version
.\gradlew.bat spotlessCheck
.\gradlew.bat :motion:testDebugUnitTest
.\gradlew.bat :motion:assembleRelease
```

The GitHub Actions workflow runs:

```bash
./gradlew spotlessCheck
./gradlew :motion:testDebugUnitTest
./gradlew :motion:assembleRelease
```

Use GitHub Actions as the authoritative verification when working through the connected repository. Do not report tests as passed unless the run for the current branch head has completed successfully.

## Later documentation cleanup

Keep this separate from MOT0.1 code work:
- Review `docs/MOTION.md` and `motion/README.md` for Solstice-era wording and references to files that are no longer present, including `docs/PLAN.md`.
- Confirm the links between the root README, `docs/`, and the module README.
- Make product documentation describe Motion as an independent library rather than as a repository-extraction project.
- Preserve useful design history, but move stale migration notes out of user-facing product documentation.

## Working rule

At the start of every session, verify the branch, pull request, latest commit, and its CI result before editing. Update this file when those facts change. Never assume that a previously green run still validates a newer commit.
