# Motion principles

This document defines Motion's reusable interaction principles. They apply across consuming applications and do not prescribe any application's visual identity, screen composition, or product-specific choreography. Motion is the behavior layer, not a general-purpose UI component kit.

The reusable base tokens live in `:motion` (`io.github.information89.motion.Motion`, expressed as response and damping). A consuming application may map those tokens through its own app-side scheme; consumer-specific mappings stay in that application. App-side names in examples are illustrative, not dependencies or requirements for Motion.

These principles guide implementation and review. Before changing user-visible motion behavior or adding a new public contract, record the proposal and obtain the owner's agreement.

## Pillars

### A. Fluid spring physics (no linear or eased curves)

Motion is interruptible and driven by velocity, not by duration. Use `spring()`, not `tween()`.

**Things morph, they never pop** (owner requirement, 2026-11-21). New content should emerge from a meaningful source or container where the interaction calls for a morph; avoid arbitrary replacement or unrelated fade-ins.

**Scope of Motion:** Motion provides reusable behavior beneath interactions across applications: button feedback, lists, menus, cards, sheets, page changes, drags, swipes, long presses, and related motion feedback. Motion owns generic mechanics; each application owns its components, layout, content, styling, navigation policy, and domain behavior. An application-specific need is not automatically a Motion feature: extract it only when the behavior is generic and independent of the application.

| Use | Spring | In code |
|---|---|---|
| Responsive controls (buttons, toggles, sliders) | `dampingRatio = Spring.DampingRatioNoBouncy`, `stiffness = Spring.StiffnessMediumLow` | Application-side token |
| Morphing containers (menus, sheets, pill-style controls) | `dampingRatio = 0.78f`, `stiffness = 380f` (subtle elasticity) | `Motion.container` |
| Inner content scale-in | `dampingRatio = 0.85f`, `stiffness = 400f` | `Motion.content` |
| Settling after a drag (no bounce) | `dampingRatio = 1f`, `stiffness = Spring.StiffnessMediumLow` | `Motion.settle` |
| Dismissing a container back into its source | `dampingRatio = 1f`, `stiffness = 260f` | `Motion.dismiss` |
| Small controls (switch thumbs, toggles, icon changes) | 0.2 s / 0.9 | `Motion.snappy` |

Values live in the `:motion` module as `MotionSpring` tokens, expressed as response + damping. Applications may map them into their own design-system abstractions when needed. Material components can consume suitable values through app-side theme integration.

The exception is a shared transition that's seeked. There, parts follow the fraction linearly, and the spring is in the `Animatable` that sets the fraction, so only one physical curve applies.

### B. Gesture-driven elastic springs (Animatable plus velocity)

- Pair an `Animatable` with `pointerInput` or `draggable`. The drag moves it directly, and the release velocity becomes the spring's `initialVelocity`. Don't use `animateFloatAsState` for gesture-driven motion.
- Catch things mid-flight: a new drag stops the spring where it is and carries on from there.
- **Animations never block input** (owner requirement, 2026-11-21): a new command must not be swallowed while an unrelated animation runs. The animation may be retargeted, finished, or continue underneath, according to the component's interaction contract.
- **Page-motion exception:** when a pager is physically moving, a new page-selection command may first stop or retarget that same page motion rather than starting a competing transition.
- Pick the target from momentum: project where a fling would come to rest using the appropriate Motion physics helper.
- Over-dragging rubber-bands and never stops dead.

### C. Continuous container morphing

- Buttons, pills, and floating controls may morph into the sheets and menus they open, and back. Avoid unrelated crossfades when a continuous source-to-target morph is meaningful.
- Use Compose shared-transition APIs and a custom bounds transform where appropriate.
- Text that travels (for example, a control label becoming a heading) can share bounds with scale-to-bounds.
- **The source and target share one material** (owner requirement, 2026-11-21): a morphing control and its expanded surface use the same background material. The morph changes shape, size, and content; it should not crossfade between unrelated materials.
- The `Morph` API can interpolate source and target `MorphStyle` properties (fill, border, shadow, and material amount) while preserving source content and a continuous first frame.

### D. Choreographed content reveal

When a compact control expands into a larger surface, inner content may be delayed and scaled so it does not look squashed while the container first stretches:

- **Container bounds:** start immediately, with a lightly underdamped spring where appropriate.
- **Inner content:** combine a short delayed fade with a small scale-in.
- **Closing:** content may leave first, then the container shrinks back.
- **Gesture-driven transitions:** tie content reveal to the morph's progress so pulling the gesture scrubs the reveal as well.

## Always

- Prefer a meaningful morph over a plain fade when one UI element genuinely transforms into another.
- Keep motion values consistent and documented.
- Preserve interruption, input responsiveness, accessibility, and reduced-motion behavior.

## Generic implementation examples

- **Source-to-surface morph:** a compact control expands into a larger surface and returns to its source, driven by gesture progress and velocity.
- **Modal sheet:** a sheet uses explicit presence and detent state, with drag and release behavior owned by a well-defined state holder.
- **Toolbar selection:** tab selection follows host-provided progress and remains interruptible; navigation state stays with the application.
- **Page movement:** swipes and tab requests use the shared page-spring helpers and a documented policy for interruption.
- **Haptics:** use semantic `:motion` haptic tokens through the haptics abstraction; physical output remains device-dependent.
- **Motion Lab:** use `MotionTime.scale` for slow-motion inspection and restore normal speed when leaving the sample environment.

## Framework

Motion's reusable contracts and delivery boundaries are documented in [the framework overview](FRAMEWORK_OVERVIEW.md), [the repository plan](MOTION_REPO_PLAN.md), and [the project handoff](../CONTINUE.md).
