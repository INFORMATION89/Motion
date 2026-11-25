# Continue

This file is the working handoff for the next development session. Keep it current when the active milestone, branch, CI state, or immediate next steps change. It is an execution guide, not a substitute for the design documents.

## Read first

1. [README](README.md) — repository layout, build, and how apps consume Motion.
2. [Motion rules](docs/MOTION.md) — interaction principles and agreed behavior.
3. [Morph study](docs/MORPH_STUDY.md) — rationale and constraints for morphing surfaces.
4. [Repository plan](docs/MOTION_REPO_PLAN.md) — why Motion is independent and how work moves to Solstice.
5. [Motion module README](motion/README.md) — current library surface and usage examples.

## Project guardrails

- Motion is an independent, reusable motion framework for Compose apps. It must not depend on Solstice or import application code.
- Compose remains the animation and interaction foundation. Do not introduce a separate universal physics engine or replace Compose primitives without a concrete need.
- Prefer interruptible motion, continuous velocity, predictable gesture ownership, and explicit input contracts.
- Keep milestone identifiers in the `MOT` family (for example, `MOT0.1`).
- MOT0.1 is limited to the core library, its tests, and directly relevant documentation. Do not expand this milestone into Motion Lab, new UI components, or benchmarks.
- Do not change the agreed motion feel or public semantics just to make an implementation easier. Add a focused test and explain the trade-off before proposing a behavior change.

## Current work: MOT0.1

**Status:** implementation is in an open draft pull request; it is not merged.

- Branch: `mot-0.1/core-validation`
- Pull request: [#1 — MOT0.1: validate MotionValue and physics inputs](https://github.com/INFORMATION89/Motion-UI/pull/1)
- Last commit whose CI result was verified while preparing this handoff: `4e047754a6f26e25c1d528080be9e9c91a62ccea`
- Verified GitHub Actions run: `45669918489` — lint (Spotless), unit tests, and library build all passed.
- This handoff file is a new commit after that verified SHA. Check the new branch-head CI result before treating the branch as green.

### Implemented in the current branch

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

## Next work — finish MOT0.1

Do these in order. Keep each change focused and add regression coverage before changing implementation behavior.

1. **Test interruption and reversal in `MorphMotion`.**
   - Open, then close before the opening completes.
   - Close, then reopen before the closing completes.
   - Issue repeated open/close commands while the first layout/placement is pending.
   - Verify the final `isOpen` state and that the geometry settles at the matching source or target, without a stale coroutine pulling it elsewhere.

2. **Test retargeting and velocity continuity.**
   - Move the target while opening; verify the morph redirects from its current bounds instead of jumping.
   - Move the source while closing; verify it lands at the source's latest bounds.
   - Change the source while open; verify the change is applied on the next close, not to the currently open surface.
   - Where test infrastructure permits, assert that retargeting preserves the current spring velocity.

3. **Test gesture commands superseding pending animations.**
   - Cover `drag`, `scrub`, `release`, and `catch` while a prior open/close is pending or running.
   - Confirm that stale continuations cannot overwrite the newest interaction.

4. **Audit numeric boundaries in `MorphMotion`.**
   - Check non-finite drag/scrub progress, release velocity, and deceleration.
   - Add validation only where it is consistent with the existing API contract; do not silently clamp invalid values unless that is already the documented behavior.
   - Check degenerate source/target geometry and progress calculation for finite, predictable results.

5. **Review the diff and rerun the complete CI checks.**
   - Resolve test failures and formatting issues; do not weaken assertions just to get a green build.
   - Update the PR description so it reports the actual latest CI result.
   - Keep the PR in draft until the interruption and retargeting gaps above are covered. Then review the full diff and decide whether it is ready to merge.

## Verification

The GitHub Actions workflow currently runs these commands:

```bash
./gradlew spotlessCheck
./gradlew :motion:testDebugUnitTest
./gradlew :motion:assembleRelease
```

Use GitHub Actions as the authoritative verification when working through the connected repository. Do not report tests as passed unless the run for the current branch head has completed successfully.

## Later documentation cleanup

Do this after MOT0.1 is stable; keep it separate from the core behavior work:

- Review `docs/MOTION.md` and `motion/README.md` for Solstice-era wording and references to files that are no longer present, including `docs/PLAN.md`.
- Confirm the links between the root README, `docs/`, and the module README.
- Make the product documentation describe Motion as a real, independent library rather than as a repository-extraction project.
- Preserve design history where useful, but move stale migration notes out of user-facing product documentation.

## Working rule

At the start of a session, verify the branch, pull request, latest commit, and its CI result before editing. Update this file when those facts change. Never assume that a previously green run still validates a newer commit.
