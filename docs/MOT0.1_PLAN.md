# MOT0.1 — implementation plan

This document turns the remaining MOT0.1 work into code-ready tasks. It is deliberately limited to the core motion library. Do not start Motion Lab, new UI components, benchmarks, or a larger API redesign as part of this milestone.

## Goal

Finish the reliability work around `MotionValue`, `MotionPhysics`, and especially `MorphMotion`: invalid inputs must fail predictably, every new interaction must supersede stale work, and a running morph must be redirectable without a visible jump or loss of its current momentum.

## Current baseline

- Work branch: `work/consolidated-docs-platforms`.
- PR: [#4](https://github.com/INFORMATION89/Motion-UI/pull/4) — draft, open, not merged. PRs #1–#3 were consolidated into it.
- Last checked PR head: `ab02bbb6d2a627802e4f1c428fef3247e59bb335`.
- Latest checked CI: [run #38](https://github.com/INFORMATION89/Motion-UI/actions/runs/45737129108) failed at Spotless due to one missing blank line in `MorphMotionTest.kt`; unit tests and library build were skipped. This is not a passing baseline for the consolidated head.
- The baseline includes input validation and normalization in `MotionValue` / `MotionPhysics`, geometric progress in `MorphMotion`, target retargeting, command-generation protection for pending placement, and initial regression tests.
- This is a baseline, not completion: interruption, retargeting, and MorphMotion input-boundary coverage remains insufficient.

## Code map

| Area | File | Responsibility |
|---|---|---|
| Stateful scalar motion | `motion/src/main/kotlin/io/github/information89/motion/MotionValue.kt` | Drag, release, snapping, target changes, input validation |
| Pure gesture physics | `motion/src/main/kotlin/io/github/information89/motion/MotionPhysics.kt` | Projection, resistance, release target selection |
| Morph state machine | `motion/src/main/kotlin/io/github/information89/motion/SpringMorph.kt` | Five animated properties, open/close, retarget, drag/scrub/release/catch, pending placement |
| Anchor model | `motion/src/main/kotlin/io/github/information89/motion/Morph.kt` | `MorphAnchor` geometry and shared anchor registration |
| Morph tests | `motion/src/test/kotlin/io/github/information89/motion/MorphMotionTest.kt` | Deterministic frame-clock tests for `MorphMotion` |
| Existing geometry tests | `motion/src/test/kotlin/io/github/information89/motion/MorphGeometryTest.kt` | Pure interpolation and geometry boundaries |
| Existing motion tests | `motion/src/test/kotlin/io/github/information89/motion/MotionValueTest.kt`, `MotionPhysicsTest.kt` | Scalar state and physics contracts |

## Work sequence

### 1. Establish deterministic interruption tests

Extend `MorphMotionTest` using its existing `MonotonicFrameClock` / `runBlocking` test pattern. Keep tests deterministic: advance frames deliberately, do not use wall-clock sleeps.

Cover:

- `open()` followed immediately by `close()`; the final destination must be the source.
- `close()` followed by `open()` while closing is still in flight; the final destination must be the target.
- Repeated `open → close → open` and `close → open → close`; the last accepted command wins.
- Commands issued before initial placement finishes; stale placement continuations must not start an older destination after a newer command.
- The morph stays visible while it is closing and becomes settled/hidden only after it reaches the source.
- A caught morph remains visible and stationary until a later gesture or command resumes it.

Assertions should check at least:
- `isOpen` reflects the latest intent.
- `bounds` approaches the expected anchor and does not finish at an obsolete anchor.
- `isMoving` eventually becomes false after settling.
- `isVisible` remains true during a close that is still in progress.

Do not assert exact frame counts for a spring unless the test is explicitly about timing. Prefer bounds tolerances and a bounded number of simulated frames.

### 2. Verify retargeting and continuity

Cover target and source changes separately because their timing contracts are intentionally different.

**Target changes**
- While opening, update the target to different bounds. The current geometry must not jump on the update; the motion must subsequently settle at the new target.
- While closed, update the target. The closed surface must not move; the next `open()` must use the updated target.
- While already open and settled, update the target. The open surface should animate from its current geometry to the new target using the last active spring set; it must not remain at stale bounds. This keeps the visible surface attached to the anchor it represents.
- If the morph is caught by a gesture, a target update must not move it under the finger. The latest target is used when the gesture releases or the next command resumes motion.

**Source changes**
- While closing, update the source. The closing motion must redirect to the latest source without a geometry jump.
- While open, update the source. The open surface must not be disturbed; the next close must use the updated source.

**Velocity**
- Retargeting must seed each replacement spring from the corresponding `Animatable.velocity` at the time of retargeting, as the current `animate()` implementation intends.
- Test continuity through observable geometry: no one-frame snap and no reversal caused solely by retargeting. Do not add a public velocity API solely to make tests easier. Add narrowly scoped internal inspection only if there is no reliable behavioral assertion.

### 3. Verify gestures supersede animation commands

Test these while an open or close spring is active:

- `scrub(progress)` takes over immediately at the requested path position.
- `drag(delta)` advances from the current path position and does not allow an older open/close continuation to overwrite it.
- `catch()` stops the moving properties where they are and keeps the morph visible.
- `release(velocity)` chooses the correct endpoint from the projected landing and starts from the gesture's current state.
- A new `open()` or `close()` after a catch/release takes precedence over any earlier command.

Keep the semantics distinct: `drag` and `scrub` are direct placement operations; `release` hands motion back to springs; `catch` stops motion for touch ownership.

### 4. Close the numeric-input boundary gaps

The current implementation validates `MotionValue` and `MotionPhysics`, but `MorphMotion.drag`, `scrub`, and `release` still need an explicit finite-input contract.

Adopt the existing MOT0.1 failure policy: reject non-finite public numeric inputs with `IllegalArgumentException` before mutating state or launching coroutines.

Cover:
- `drag(delta)`: reject `NaN` and positive/negative infinity.
- `scrub(progress)`: reject `NaN` and infinities before `coerceIn`.
- `release(rawVelocity, deceleration)`: reject non-finite raw velocity and require finite deceleration in `(0, 1)` before clamping the finite velocity to the existing `MAX_VELOCITY`.
- Verify invalid calls leave `isOpen`, `bounds`, and command state unchanged.

Do not remove the existing finite high-velocity clamp: it is an intentional protection against extreme gesture releases.

Check geometric edge cases separately:
- Identical source and target anchors (the denominator fallback in `progress`).
- Zero-size or transient anchors that Compose can produce during layout.
- Non-finite anchor geometry / corner radius.

Validate `MorphAnchor` geometry at its boundary: all four rectangle edges and `cornerRadius` must be finite; `right >= left`, `bottom >= top`, and `cornerRadius >= 0`. Allow zero width/height because Compose can report transient zero-size layout anchors. Add tests for zero-size acceptance and invalid/inverted/non-finite geometry. Do not silently normalize corrupt anchors into plausible values.

### 5. Recheck the scalar motion contracts

Do not broaden this into a rewrite. Confirm tests already cover:
- `restPoints` defensive copy, sorting, deduplication, empty and non-finite rejection.
- `slowFlick <= fastFlick` on construction and mutation.
- Safe threshold updates when `motionDrag` range changes.
- Finite targets, velocities, deltas, projection and release configuration.
- Changing `restPoints` does not retarget an already-running animation.
- Same-target `animateTo()` still animates as agreed.

Add only any missing regression test discovered during implementation. Keep `MotionSpring` constructor/conversion validation (positive finite response and damping, finite conversion inputs) as a separate follow-up unless implementation reveals that it directly blocks MOT0.1.

### 6. Final review and PR readiness

Before marking PR #4 ready:
- Review the complete diff against `main`; remove accidental or unrelated changes.
- Confirm public KDoc describes the actual validation and retargeting semantics.
- Run the entire repository CI workflow on the final branch head:
  - `./gradlew spotlessCheck`
  - `./gradlew :motion:testDebugUnitTest`
  - `./gradlew :motion:assembleRelease`
- Confirm all three checks pass on the exact latest commit, not merely on the earlier baseline.
- Update `CONTINUE.md` and the PR #4 body with the final SHA and CI result.
- Only then remove draft status / request review. Do not merge automatically.

## Definition of done

MOT0.1 is ready to integrate only when:
1. All agreed `MotionValue` and `MotionPhysics` contracts are covered.
2. `MorphMotion` interruption, reversal, pending placement, retargeting, source deferral, and gesture supersession have regression tests.
3. Non-finite `MorphMotion` inputs cannot mutate state or poison animation geometry.
4. Retargeting does not create a visible jump and carries forward current spring velocity.
5. CI passes on the exact final branch head.
6. PR #4 has an accurate description and is no longer draft.

## Saturday session opening checklist

1. Open PR #4 and confirm the head is `work/consolidated-docs-platforms`; fetch the latest branch.
2. Check the current head SHA and latest Actions run. If CI is not green, diagnose that before starting feature work.
3. Open this plan and `CONTINUE.md`.
4. Start with **Task 1: deterministic interruption tests**, not implementation guesses.
5. Change production code only in response to a failing regression test or a concrete contract gap.
