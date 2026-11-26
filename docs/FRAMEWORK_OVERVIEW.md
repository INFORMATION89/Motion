# Motion framework overview

## The two Motion layers

The repository is intended to provide two independent, composable layers. They have different responsibilities and must not depend on each other in the wrong direction.

### Motion Core — `:motion`

Motion Core is the reusable motion and interaction engine for Compose apps. It owns behavior, not the app's component catalog:

- spring definitions and shared motion tokens;
- interruptible motion state, retargeting, and settling;
- gesture mechanics: drag, catch, scrub, release projection, velocity handoff, and elastic resistance;
- morph geometry and source/target coordination;
- reusable motion primitives such as page movement and dials;
- motion choreography, semantic haptic primitives, ambient-motion scheduling, and reduced-motion behavior.

Compose remains the animation, rendering, and input foundation. Motion Core must not introduce a second animation engine.

### Motion UI — `:motion-ui`

Motion UI is an optional Compose component layer built on Motion Core. Its first delivered stage (MOT0.3A) provides an app-configurable Material 3 theme entry point and semantic shape, spacing, and surface-material tokens. Further reusable building blocks are staged separately.

The initial intended scope is:

- reusable page frame/scaffold and header-action slots;
- configurable card and sheet building blocks;
- reusable toolbar/header action and message/feedback surfaces;
- shared component contracts and theme/token names for typography, shape, spacing, materials, and motion;
- optional glass/material wrappers only where a focused rendering dependency is justified and platform fallbacks are explicit.

Motion UI is **not** meant to replace Material 3 or become a complete application framework. It should not clone standard controls that Compose or Material already provide. The module is now included in the Gradle build and required CI. Its current public API is limited to the theme and token foundation; card, page, sheet, and glass components are not yet delivered.

Motion UI depends on Motion Core. Motion Core must never depend on Motion UI.

## The responsibility boundary

Think of an app as these cooperating layers:

1. **Application and domain** — weather data, forecasts, business rules, screen state, navigation destinations, and product-specific decisions.
2. **App theme and product design** — brand colors, visual identity, product-specific typography choices, content, and screen composition.
3. **Motion UI (optional)** — reusable component building blocks such as page frames, cards, sheets, header actions, and message surfaces, with configurable defaults.
4. **UI component libraries** — standard buttons, switches, sliders, text fields, dialogs, navigation controls, semantics, and accessibility behavior. Prefer Material 3 or another focused library instead of cloning these.
5. **Motion Core** — reusable movement and interaction behavior used by components: spring state, gesture tracking, interruption, retargeting, velocity handoff, morph mechanics, and motion feedback.
6. **Compose and the platform** — rendering, layout, pointer input, animation primitives, windowing, OS services, and hardware capabilities.

Motion builds on Compose and complements UI libraries. It does not replace the platform, standard component libraries, or the consuming app.

## What belongs in Motion Core

A behavior is a good candidate when it is generic, useful to more than one app, and can be expressed without importing app-specific UI or domain code.

- **Spring system and tokens:** reusable response/damping values and Compose spring specifications.
- **Motion state:** interruptible values, retargeting, settling, and clear state contracts.
- **Gesture mechanics:** drag, catch, scrub, release projection, velocity handoff, and elastic resistance.
- **Morph mechanics:** transitions between registered anchors, shape/geometry interpolation, and coordinated source/target behavior.
- **Reusable interaction primitives:** dials, page-spring movement, and other controls whose interaction behavior is generic.
- **Motion choreography:** reveal and hand-off helpers that connect content appearance to a motion value.
- **Motion feedback:** semantic haptic primitives, ambient-motion scheduling, and reduced-motion decisions.
- **Shared low-level rendering helpers:** only when they directly support reusable motion behavior and do not impose an app's visual identity.

A feature does not belong in Motion Core merely because it animates. The reusable behavior must be separable from the app's content and appearance.

## What belongs in Motion UI

A component belongs in Motion UI when its structure is reusable across apps and benefits from a shared interaction contract, but it is not already a standard component best supplied by Material or another library.

Good candidates include configurable page frames, card/sheet containers, header-action layouts, message surfaces, and the theme contracts needed to configure them.

Motion UI should:

- expose slots and configuration rather than hardcode a consuming app's content;
- provide neutral defaults that apps can override;
- use Motion Core for movement and interaction mechanics;
- use Compose and Material components for standard controls and accessibility semantics;
- depend on focused libraries such as Haze for specialized rendering instead of reimplementing them;
- remain optional, so an app can use Motion Core without adopting Motion UI.

It must not contain Solstice screens, weather models, forecasts, navigation policy, sky rendering, weather icons, or a fixed Solstice brand palette.

## What belongs in other UI libraries

| Concern | Best owner | Motion's relationship |
|---|---|---|
| Layout, measurement, drawing, pointer input, and animation runtime | Jetpack Compose UI, Foundation, and Animation | Use these primitives; do not reimplement them. |
| Standard buttons, switches, sliders, text fields, dialogs, navigation bars, and accessibility semantics | Material 3 or another component library | Motion UI may compose these; it should not create competing replacements. |
| Shared reusable page frames, configurable cards/sheets, header-action slots, and message surfaces | Planned Motion UI, when generic | Provide configurable building blocks, not app-specific screens. |
| Shared visual language: colors, typography, shape, spacing, component variants | The app's design system/theme, with neutral Motion UI token contracts where useful | The app supplies its values; Motion must not define an app's brand. |
| Navigation routes, back stack, deep links, screen lifecycle | Navigation library or application architecture | Motion may animate transitions but does not own routing or navigation policy. |
| Blur and advanced backdrop rendering | Haze or another specialized rendering library | Motion UI may wrap a dependency behind a configurable surface; it should not implement a competing blur engine. |
| Images, maps, charts, weather icons, media, and domain widgets | Specialized libraries and app modules | Motion may animate their containers or interactions; it does not implement their domain content. |
| Window management, platform sensors, OS-specific input, and hardware APIs | Platform libraries or narrow platform adapters | Motion may expose a stable abstraction only where the capability is genuinely part of motion feedback. |
| Screens, weather cards, forecasts, sky rendering, and Solstice-specific glass/sky styling | Solstice | Keep product-specific content and visual treatment in the app. |

Use an existing UI library when it already solves the structural, semantic, or visual problem. Do not create a second Button, TextField, navigation system, or theme layer just to attach an animation.

## A practical ownership test

Ask these questions before adding code:

1. Is this generic motion behavior that could serve an unrelated app? If yes, consider Motion Core.
2. Is this a reusable component structure that several apps could configure differently? If yes, consider Motion UI.
3. Is this a standard control or accessibility pattern already handled by Material/Compose? If yes, use that library.
4. Is this product content, branding, navigation policy, or domain behavior? If yes, keep it in the app.
5. Does a focused library already solve the specialized problem? If yes, reuse it.

## How an app should use Motion

- Build screens and standard controls with Compose and the app's chosen UI component libraries.
- Keep domain state, navigation, product-specific styling, and screen composition in the app.
- Use Motion Core for reusable motion mechanics.
- Adopt Motion UI only when its reusable components fit the app; it is optional.
- Pass the app's own theme values into configurable Motion UI components.
- Prefer existing tokens and APIs over app-local spring constants, duplicate physics, or one-off gesture engines.
- Propose a new public API only when a reusable capability is missing; describe its contract and tests before implementing it.

For Solstice, Motion Core should own generic motion mechanics; Motion UI may later own neutral reusable frames, cards, sheets, and header building blocks; Solstice remains responsible for the weather experience, navigation policy, and its glass/sky identity.

## Platform boundary

The current implementation targets Android and Jetpack Compose only. Motion UI is also Android/Compose-oriented. Shared design principles do not mean iOS or desktop targets are currently supported. See [Platform support](PLATFORM_SUPPORT.md).
