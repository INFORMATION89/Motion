# Motion Runtime and Test Coverage Audit

**Date:** 2026-11-27  
**Branch:** `fix/mot03b-pointer-injection-scope`  
**Reviewed head:** `87a71fd0e74ec6cf206949c20e005410123e4f6e`  
**Scope:** static source/test audit; no production implementation changes.

## Executive findings

The Core test suite is strongest around deterministic numeric and animation-state behavior. It covers geometry, spring retargeting, gesture interruption, velocity handoff, rest-point selection, and pure haptic scaling. The Android integration boundary is materially less tested: no Core `androidTest` source set exists in this branch, and there are no dedicated tests for system-setting observation, lifecycle suspension/resumption, battery-saver transitions, vibrator capability/fallback behavior, or actual view haptic dispatch.

Two runtime signals are confirmed to be snapshots:
- `rememberReducedMotion()` reads `Settings.Global.ANIMATOR_DURATION_SCALE` inside `remember(context)`.
- `rememberAmbientMotionAllowed()` reads `ValueAnimator.areAnimatorsEnabled()` and `PowerManager.isPowerSaveMode` inside `remember(context)`.

Neither implementation subscribes to setting/broadcast changes. A recomposition for an unrelated reason does not necessarily refresh a remembered value. This makes live system-setting changes an integration gap, not a verified runtime-reactive behavior.

## Test coverage inventory

| Area | Existing evidence | Coverage boundary |
|---|---|---|
| Motion physics | `MotionPhysicsTest` | Pure projection, target, resistance, and rubber-band calculations. |
| MotionValue | `MotionValueTest` | Deterministic frame-clock tests for drag, overshoot catch, release projection, stop-mid-flight, settling, rest-point changes, and reissuing targets. |
| Morph geometry | `MorphGeometryTest` | Pure geometry and validation; does not exercise Android settings or Compose rendering. |
| MorphMotion / SpringMorph | `MorphMotionTest` | Deterministic frame-clock tests for open/close reversal, repeated commands, source/target retargeting, continuity, and invalid inputs. |
| Haptic calculations | `FoundationsTest` | Tests pure `hapticScale` calculations, speed fade, intensity, and token presets. Does not call `MotionHaptics.perform`. |
| Ambient clock | No dedicated test found | Lifecycle repeat/cancel, frame-rate cap, `running` pause/resume, and wall-clock behavior are not covered by automated tests. |
| Reduced-motion settings | No dedicated test found | Settings read, observer lifecycle, and live transition behavior are not covered. |
| Haptic platform dispatch | No dedicated test found | Primitive support, API-level fallback, disabled feedback setting, permission/error fallback, and rate limiting are not covered at the Android boundary. |
| Motion UI tokens | `MotionUiTokensTest` | Theme defaults and overrides; no component motion behavior. |
| Toolbar prototype | `ToolbarPrototypeUiTest` | Compose semantics, host-selection invariants, LTR/RTL focus movement, contextual-action target count/hit testing, and drag completion/cancellation. No reduced-motion transition test. |

The repository test tree at the reviewed head contains Core JVM tests but no `motion/src/androidTest` tests. Motion Lab has an Android instrumentation test for the toolbar prototype; it does not provide coverage for Core's Android services.

## Runtime behavior confirmed from source

### Reduced Motion

`rememberReducedMotion()` reads the global animator duration scale once per remembered `Context`. It has no `ContentObserver`, `Flow`, receiver, or other invalidation source. Its return value is therefore not a live preference subscription.

The `Morph(reduced = ...)` rendering path places the surface at target geometry, uses the target style, omits source-content handoff, and uses alpha as the remaining progress signal. The caller still owns the `MotionValue` and its state transitions. Reduced rendering is not a global command that automatically snaps every `MotionValue`, `SpringMorph`, or other animation in the app.

### Ambient motion and lifecycle

`rememberAmbientClock()` uses `repeatOnLifecycle(RESUMED)` and a frame clock, with an FPS cap clamped to 1–120. The supplied `running` predicate is read through `rememberUpdatedState`, so its latest lambda is used without restarting the effect. The clock's time value is derived from wall-clock time, allowing it to resume at the current phase after suspension.

These are useful implementation properties, but no automated test currently proves lifecycle cancellation/restart, the FPS cap, or pause/resume behavior. Separately, `rememberAmbientMotionAllowed()` snapshots animator and battery-saver state and does not observe changes while composed.

### Haptics

The existing unit tests validate only the pure intensity/speed calculation. `MotionHaptics.perform` also depends on Android system settings, API-level primitive support, vibrator services, view fallback, and shared token rate-limit timestamps. None of those platform branches has dedicated automated coverage in Core.

One behavior worth making explicit in tests before changing it: `perform` advances the token's rate-limit timestamp before checking whether system feedback is enabled or the calculated intensity is effectively zero. Consequently, a suppressed attempt can consume that token's minimum-gap window. This may be intentional throttling; this audit records it as a contract question, not as a confirmed defect.

## Recommended work, in order

1. **Define a small internal Android system-motion signal.** Observe animator-duration-scale changes and battery-saver changes with lifecycle-aware registration; expose Compose state that updates when those settings change. Keep this Android-specific and internal until the behavior is proven. Do not add a speculative multiplatform abstraction.
2. **Specify the reduced-motion contract.** State which effects are removed, which transitions become immediate, and which state/action/feedback behavior remains. Ensure all consumers of Motion primitives can apply the policy consistently without introducing a process-global mutable switch.
3. **Add focused deterministic tests before implementation expansion.** Cover policy state changes, observer registration/disposal, ambient clock lifecycle stop/start, frame cap, and `running` toggles. Keep timing under a controllable clock; do not use sleeps.
4. **Add a narrow haptics test seam only if needed.** Verify disabled feedback, unsupported primitive fallback, dispatch failure fallback, API-level mapping, and rate-limit semantics without exposing test-only public API.
5. **Extend toolbar validation.** Add a Compose UI test for reduced-motion rendering/interaction invariants after the system signal contract is settled. Keep live device setting changes and physical haptics in the manual validation record.

## Verification boundary

This audit is based on source and test-tree inspection at the reviewed head. No Gradle build, JVM test, emulator test, device test, or CI workflow was run. No production code, public API, dependency, workflow, or consumer was changed. Dynamic system-setting changes, TalkBack, physical haptics, and lifecycle behavior remain **NOT TESTED** at runtime.
