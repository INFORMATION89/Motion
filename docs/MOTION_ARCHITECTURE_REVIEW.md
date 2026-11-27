# Motion Core and Motion UI — architecture review

**Review date:** 2026-11-27  
**Reviewed branch:** `fix/mot03b-pointer-injection-scope`  
**Scope:** source and documentation review only; no production code changes.

## Executive assessment

The repository's actual module boundary is coherent: `:motion` contains reusable Compose motion/interaction mechanics, while `:motion-ui` is optional and depends on Core. Core does not depend on UI. The toolbar remains a component-specific interaction system in Motion Lab until its interaction and accessibility contract is validated. The Material 3 Expressive study supports this design rather than suggesting a new animation engine or a Material-shaped component catalog.

The main architectural risk is not a broken module boundary. It is the gap between the broad Core responsibility described in documentation and the currently Android/Compose-specific implementation of several facilities, especially system settings, lifecycle, haptics, and PagerState integration. This is acceptable for the explicitly Android-only artifact; it becomes relevant only if a future multiplatform commitment is approved.

## 1. Current implementation map

| Area | Current implementation | Assessment |
|---|---|---|
| Spring model and semantic presets | `MotionSpring`, `Motion`, `MotionTime` in `Motion.kt` | Strong named interaction roles; Compose SpringSpec conversion is centralized. `MotionTime` is global mutable tuning state and should remain a lab/debug facility, not become consumer-wide runtime policy. |
| Gesture physics | `MotionPhysics` | Pure calculations for projection, target selection, rubber-banding, and resistance; unit-testable. It still imports Compose `Dp` for thresholds, so it is not a completely platform-neutral module. |
| Interruptible scalar state | `MotionValue` | Good single-value contract: direct drag, catch/stop, release projection, velocity handoff, configurable rest points, and draw/layout reads. It is a Compose state primitive, not a general domain-state store. |
| Multi-property morph | `MorphMotion` / `SpringMorph` | Five independent Animatables coordinate centre, size, and corner radius; retargeting preserves velocity, and generation guards protect delayed placement. This is specialized geometry choreography, not a generic arbitrary-Shape morph engine. |
| Registered-anchor morph rendering | `Morph.kt`, `MorphStyle.kt` | Anchor registry, layout/draw-time reads, source/target content handoff, and style interpolation are already present. Shape customization is caller-supplied; arbitrary path correspondence is not solved by Core. |
| Page motion | `PageSpring.kt` | Reusable pager integration, but directly coupled to Foundation `PagerState`; keep it as an optional Compose integration rather than a dependency of generic physics. |
| Motion feedback | `Haptics.kt`, `AmbientClock.kt` | Useful semantic primitives and lifecycle/battery-aware ambient clock. Both are Android-backed capabilities, not platform-neutral Core. |
| Motion UI foundation | `MotionUiTheme.kt` | Currently only configurable MaterialTheme entry point plus shape, spacing, and surface-material tokens. No delivered public card, sheet, toolbar, or component catalog yet. |
| Toolbar experiment | Motion Lab spike | Correctly owns toolbar-specific geometry, tab identity, host progress mapping, roving focus, and contextual action behavior. Core is reused for generic motion, not made aware of toolbar concepts. |

## 2. Decisions confirmed by the source review

1. **Keep the dependency direction UI → Core.** The existing Gradle modules and docs agree. Do not move component layout, style profiles, navigation concepts, or tab state into Core.
2. **Keep the toolbar state machine in the component layer.** Stable tab IDs, host pager progress, selection callbacks, overflow/scroll policy, focus traversal, and contextual action semantics are component/consumer contracts. Only reusable mechanics should be extracted after a demonstrated second use case.
3. **Keep Compose as the animation and rendering engine.** Motion should continue to compose around `Animatable`, `SpringSpec`, pointer/drag APIs, layout, and drawing. Do not create a competing scheduler or physics animation runtime.
4. **Keep semantic motion roles, not a clone of Material's constants.** The M3 Expressive six-spec scheme is a useful model for separating spatial movement from effects. It does not justify replacing Motion's existing agreed spring character or importing Material token values into Core.
5. **Keep externally driven progress single-sourced.** Pager/scroll progress should map directly to indicator geometry, alpha, and dimensions. Independent spring settling belongs to a gesture release/settle phase, not on top of continuously supplied host progress.
6. **Keep shape morph capability explicit.** Core's registered-anchor morph handles its own rounded geometry and caller-supplied shape rendering. Do not claim arbitrary Compose Shapes can be interpolated. If general polygon morphing is ever needed, define a separate geometry contract and prove it independently.
7. **Use Material 3 for standard controls and semantics.** Motion UI should compose Material components where appropriate and focus on distinctive reusable interactions, not reimplement buttons, list items, or app bars merely because they animate.

## 3. Gaps and risks to track

### A. Reduced motion is not yet one reactive policy

- `rememberReducedMotion()` in `Morph.kt` reads Android's animator-duration setting inside `remember(context)`. It is a snapshot, not a subscription to setting changes while the composable remains alive.
- `rememberAmbientMotionAllowed()` similarly snapshots animator availability and battery-saver state.
- The toolbar spike reads the animator-enabled signal during composition; the spike document already records live preference changes as unverified.
- Core APIs expose local decisions such as `Morph(reduced = ...)` and `MotionValue.snapTo(...)`; there is no single policy that automatically adapts every MotionValue/SpringMorph operation.

**Decision:** do not silently introduce a public global reduced-motion framework in this review. Before public toolbar API hardening, define a consistent reactive Android signal and document which motion is removed, simplified, or preserved. Reduced motion must preserve state, actions, and accessibility; it should not be equated with disabling all interaction feedback. Add deterministic tests for policy transitions and retain manual system-setting validation.

### B. Core is intentionally Android/Compose-specific today

- Android settings are read in `Morph.kt`; Android lifecycle and PowerManager/ValueAnimator are used by `AmbientClock.kt`; vibrator and Android view feedback are used by `Haptics.kt`.
- `PageSpring.kt` takes Foundation `PagerState`; `MotionPhysics` uses Compose `Dp` thresholds.
- The current Gradle artifact is an Android library and the README/platform guide say Android only.

**Decision:** this is not a defect under the current platform commitment. Do not begin Kotlin Multiplatform conversion or add speculative expect/actual layers. If platform support becomes a product goal, first separate pure numeric physics from Compose units and design narrow platform adapters for system settings, lifecycle, and haptics; then add targets, tests, docs, and CI together.

### C. Global mutable tuning and feedback tokens need careful scope

- `MotionTime.scale` is a process-wide mutable slow-motion switch.
- `Haptic.intensity` and individual `HapticToken` properties are mutable shared objects; rate-limit timestamps also live on the shared token.

**Decision:** keep these as Motion Lab/debug facilities for now. Do not use them as per-component or per-consumer configuration. A future public tuning surface would need an explicit ownership/scope model (composition, instance, or app) and tests for concurrent consumers; do not broaden it pre-emptively.

### D. Motion UI is a foundation, not yet a component system

The current public implementation is a theme/token entry point. It does not yet contain the component-level motion/style contracts described as future direction in the overview. This is accurate as long as docs and release claims continue to distinguish delivered APIs from planned components.

**Decision:** add component-specific style contracts only alongside an accepted component contract. Keep behavior, style, and app configuration separate; prefer semantic style values over a large bag of low-level animation constants.

### E. Morph boundaries and semantics require targeted verification

Core has meaningful morph geometry and content handoff, but it does not automatically own the semantic transition between source and destination content. That must be specified by the component using the morph. M3's list/app-bar patterns reinforce the need to align visual crossfades with semantics, focus, and touch exploration.

**Decision:** every future morphing component must document which node is actionable/focusable at each phase, how focus is retained or transferred, and how reduced motion changes the transition. Keep tests on observable visibility, bounds, and semantics rather than exposing internal animation values as public API.

## 4. Recommended sequence

1. **Now — no production changes:** preserve this architecture review as a decision record. Continue source review only where it answers a concrete Motion design question.
2. **Before public toolbar API:** finish the manual validation record for PR #26, especially live reduced-motion changes, TalkBack, roving focus, RTL, font scaling, contextual-action hit testing, and rapid gesture interruption. Resolve the isolated test compilation fix only under the existing CI/owner-approval budget.
3. **At toolbar API design:** specify a small controlled API around stable IDs, host progress, callbacks, independent action semantics, style slots/tokens, and reduced-motion behavior. Do not expose toolbar internals or raw per-property spring constants.
4. **After toolbar contract is accepted:** use a second genuinely distinct component to identify any Core extraction. Do not extract abstractions solely because one toolbar implementation uses them.
5. **Only after an explicit platform decision:** plan any multiplatform refactor as a separate milestone with source-set design, adapters, target-specific tests, docs, and CI.

## 5. Evidence and verification boundary

Reviewed source files include `Motion.kt`, `MotionPhysics.kt`, `MotionValue.kt`, `Morph.kt`, `MorphSettings.kt`, `MorphStyle.kt`, `SpringMorph.kt`, `PageSpring.kt`, `AmbientClock.kt`, `Haptics.kt`, `MotionUiTheme.kt`, and the framework/style/toolbar design documents. The review also used the AndroidX Material 3 Expressive source study in `docs/REFERENCE_MATERIAL3_EXPRESSIVE.md`.

This is a static source/documentation review, not a build, test, benchmark, or device validation. No production code, public API, dependency, CI configuration, or consumer repository was changed. The findings describe the isolated branch's reviewed source as of 2026-11-27.
