# Motion framework overview

## What Motion is

Motion is the motion and interaction layer of a Compose application. It provides reusable behavior for moving, dragging, morphing, settling, and responding to input.

It is intended to make interaction behavior consistent across an app and reusable across unrelated apps. An app should be able to adopt Motion without adopting Solstice's visual style, screens, domain model, or product decisions.

Motion is **not** a complete UI framework or a replacement for Jetpack Compose, Material, or an app's design system.

## The responsibility boundary

Think of an app as a set of layers:

1. **Application and domain** — weather data, forecasts, business rules, screen state, navigation destinations, and product-specific decisions.
2. **UI components and design system** — buttons, cards, menus, sheets, text fields, typography, color, spacing, shapes, accessibility labels, and visual identity.
3. **Motion** — reusable movement and interaction behavior used by those components: spring state, gesture tracking, interruption, retargeting, velocity handoff, morph mechanics, and motion feedback.
4. **Compose and the platform** — rendering, layout, pointer input, animation primitives, windowing, OS services, and hardware capabilities.

Motion builds on Compose; it does not replace the layers below or absorb the layers above.

## What belongs in Motion

A behavior is a good candidate when it is generic, useful to more than one app, and can be expressed without importing app-specific UI or domain code.

- **Spring system and tokens:** reusable response/damping values and Compose spring specifications.
- **Motion state:** interruptible values, retargeting, settling, and clear state contracts.
- **Gesture mechanics:** drag, catch, scrub, release projection, velocity handoff, and elastic resistance.
- **Morph mechanics:** transitions between registered anchors, shape/geometry interpolation, and coordinated source/target behavior.
- **Reusable interaction primitives:** dials, page-spring movement, and other controls whose interaction behavior is generic.
- **Motion choreography:** reveal and hand-off helpers that connect content appearance to a motion value.
- **Motion feedback:** semantic haptic primitives, ambient-motion scheduling, and reduced-motion decisions.
- **Shared low-level rendering helpers:** only when they directly support a reusable motion behavior and do not impose an app's visual identity.

A feature does not belong in Motion merely because it animates. The reusable behavior must be separable from the app's content and appearance.

## What belongs in other UI libraries

| Concern | Best owner | Motion's relationship |
|---|---|---|
| Layout, measurement, drawing, pointer input, and animation runtime | Jetpack Compose UI, Foundation, and Animation | Use these primitives; do not reimplement them. |
| Standard buttons, switches, sliders, navigation bars, dialogs, and accessibility semantics | Material 3 or another component library | Motion can provide movement/state mechanics when needed; the component library owns the component structure and semantics. |
| Shared visual language: colors, typography, shape, spacing, component variants | The app's design system or theme library | Motion should not define an app's brand or theme. |
| Navigation routes, back stack, deep links, screen lifecycle | Navigation library or application architecture | Motion may animate transitions but does not own routing or navigation policy. |
| Images, maps, charts, weather icons, media, and domain widgets | Specialized libraries and app modules | Motion may animate their containers or interactions; it does not implement their domain content. |
| Window management, platform sensors, OS-specific input, and hardware APIs | Platform libraries or narrow platform adapters | Motion may expose a stable abstraction where the capability is genuinely part of motion feedback. |
| Screens, weather cards, forecasts, and Solstice-specific glass/sky styling | Solstice | Keep product-specific content and visual treatment in the app. |

Use an existing UI library when it already solves the structural or visual problem. Do not create a second Button, TextField, navigation system, or theme layer just to attach an animation.

## A practical ownership test

Ask these questions before adding code:

1. Would this behavior make sense in an unrelated Compose app with different branding and different content?
2. Can it be described as reusable movement or interaction behavior rather than a specific screen or component design?
3. Can it use the existing Compose and Motion primitives without importing app code?
4. Is there already a suitable Compose, Material, or specialized-library implementation?

If the behavior is generic and not already covered, propose it for Motion. If only its appearance or content is app-specific, keep that part in the app and compose it with Motion. If a standard component already exists, use that component instead of cloning it.

## How an app should use Motion

- Build the UI with Compose and the app's chosen component/design-system libraries.
- Keep domain state, navigation, screen composition, and product-specific styling in the app.
- Use Motion for the reusable interaction mechanics beneath those components.
- Prefer existing tokens and APIs over app-local spring constants, duplicate physics, or one-off gesture engines.
- Add app-side wrappers only to connect Motion behavior to the app's state and visual language.
- Propose a new public Motion API only when a reusable capability is missing; describe its contract and tests before implementing it.

For Solstice, this means the library should own generic mechanics, while Solstice remains responsible for the weather experience, its glass/sky appearance, component composition, and product behavior.

## Platform boundary

The current implementation targets Android and Jetpack Compose only. The framework's architectural direction may support additional targets later, but the existence of shared design principles does not mean those targets are currently supported. See [Platform support](PLATFORM_SUPPORT.md).
