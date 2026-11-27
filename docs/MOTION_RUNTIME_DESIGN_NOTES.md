# Motion Runtime Design Notes

**Date:** 2026-11-27  
**Branch:** `fix/mot03b-pointer-injection-scope`  
**Baseline:** `ed523d9ccf104c1e2427163092dcbe211b7c5ae0`  
**Status:** design-only; no runtime or public API changes.

## 1. Ambient clock: frame scheduling is not actually paused

Source: `motion/src/main/kotlin/io/github/information89/motion/AmbientClock.kt`.

The current loop requests `withFrameNanos` continuously while the lifecycle is RESUMED. It checks the latest `running` predicate inside every frame callback and only writes the returned state when the predicate is true and the FPS interval has elapsed.

Consequences:

- `running = false` suppresses state writes, but the coroutine still receives a callback every display frame.
- `fps` caps state updates, not frame callback scheduling. At 30 FPS on a 120 Hz display, the loop still wakes for the intervening display frames.
- The documentation's "paused while unseen" wording is stronger than the implementation. It pauses value updates, not the frame loop itself.

This is a concrete efficiency/documentation mismatch, not a correctness failure in the returned time value.

### Recommended implementation direction

Use an event-driven active/inactive boundary rather than polling the predicate on every display frame:

1. Observe changes to the supplied Compose-state-backed `running` predicate.
2. When inactive, suspend the frame loop entirely.
3. When active, schedule updates at the requested cadence, aligning each emitted update to a display frame.
4. Keep the existing RESUMED lifecycle boundary; leaving RESUMED must cancel the active loop and re-entering it must resume from wall-clock time.
5. Keep `rememberUpdatedState` or an equivalent latest-lambda mechanism so changing the predicate does not require restarting the whole composable effect.
6. Validate `fps` bounds and preserve the existing 1–120 clamp unless the public contract is deliberately revised.

A likely Compose-native shape is a snapshot observation of the predicate combined with a cancellable frame loop for the active state. The implementation must ensure the predicate's state reads are observed, the inactive branch does not request frames, and cancellation on lifecycle exit is prompt. Do not add a dependency or public abstraction.

### Required regression evidence before implementation

- Inactive state produces no further clock updates and no active frame loop.
- Switching inactive → active resumes without waiting for an unrelated recomposition.
- Switching active → inactive cancels the active loop.
- FPS pacing emits no faster than the configured cap while aligning emitted values to frame time.
- Lifecycle STOP/RESUME cancels and restarts the loop without duplicate collectors.
- The returned value resumes from current wall-clock phase, not stale accumulated elapsed time.
- A replaced `running` lambda is honored without stale captures.

Use a controllable frame clock and Compose snapshot state. No sleeps or real-time timing assertions.

## 2. System motion preferences: keep Android observation internal

Current `rememberReducedMotion()` and `rememberAmbientMotionAllowed()` are remembered snapshots. They do not observe later changes to animator duration scale or battery saver.

Recommended design boundary:

- One internal Android-specific source of system-motion state, scoped to the current composition/context and disposed with it.
- Observe animator-duration-scale changes through a `ContentObserver`.
- Observe battery-saver transitions through the documented power-save-mode broadcast.
- Publish the combined values as Compose state; avoid a process-global mutable preference cache.
- Handle registration/unregistration, context changes, lifecycle, and API-level receiver requirements explicitly.
- Keep the existing public composable names and Boolean return types initially. Do not add a new public settings API or multiplatform abstraction.

The implementation must decide whether the two existing helpers share one observer owner or use one narrowly scoped observer each. Prefer one shared internal owner only if it avoids duplicate registrations without coupling unrelated compositions or creating global state.

### Required regression evidence

- Initial values match the system APIs.
- Animator setting changes invalidate the observed value while composed.
- Battery-saver changes invalidate the observed value while composed.
- Disposal unregisters every observer/receiver exactly once.
- Recomposition and context/lifecycle changes do not leak or duplicate registrations.
- Observer callbacks arriving after disposal cannot mutate active UI state.
- Failures to read/register system signals degrade safely and are documented.

The system's live setting changes still require an emulator/device check; unit tests alone cannot prove Android broadcasts and settings observation on supported OS versions.

## 3. Haptics: clarify rate-limit semantics before touching dispatch

Current `MotionHaptics.perform` advances `HapticToken.last` before checking the system feedback setting and before calculating whether the final intensity is effectively zero. Thus a suppressed event can consume the token's minimum-gap interval.

This is currently an observable behavior, but the source does not establish whether it is intentional. Recommended contract for owner review:

- A disabled system feedback setting should not consume a token's rate-limit window.
- A computed intensity at or below the existing dispatch threshold should not consume it.
- Once a real dispatch attempt is made, the rate-limit timestamp should advance even if primitive dispatch fails and the view fallback is attempted. This prevents repeated failed dispatch attempts from flooding the fallback path.
- Preserve per-token rate limiting and current fallback behavior unless separate evidence demonstrates a defect.

Do not change this behavior in this design-only batch. Before implementation, add focused tests for disabled feedback, zero intensity, rate-limited repeat, unsupported primitive fallback, and dispatch failure fallback. Keep any platform seam internal and production-meaningful; do not add public hooks solely for tests.

## 4. Scope and sequencing

1. Implement the internal system-motion observer and its focused lifecycle/disposal tests.
2. Correct ambient-clock scheduling and add deterministic active/inactive, FPS, and lifecycle tests.
3. Clarify and test haptic rate-limit behavior, then make the smallest implementation change.
4. Add a toolbar reduced-motion rendering/interaction test only after the live signal behavior is stable.
5. Run one coherent verification batch after implementation. Do not trigger CI for this design-only document change.

No public API, dependency, component, platform target, consumer, CI workflow, or existing motion contract is changed by this note.
