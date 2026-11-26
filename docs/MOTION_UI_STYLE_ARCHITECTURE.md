# Motion UI styling and toolbar architecture

## Product direction

Motion UI components must not be locked to one visual identity. A component's interaction contract should remain stable while its visual treatment can be selected or customised by the consuming app.

The animated toolbar is a flagship example: keep its advanced interaction model instead of replacing it with a standard Material 3 top app bar simply because Material 3 is the default component library.

## Three independent concerns

1. **Behaviour** — tab selection, pager synchronisation, drag/long-press gestures, velocity handling, interruptibility, spring choreography, contextual action expansion, and reduced-motion behaviour. Use Motion Core for reusable motion mechanics.
2. **Style** — dimensions, shapes, colors, typography roles, surface treatment, elevation, icon treatment, and animation styling parameters that are genuinely visual.
3. **Application configuration** — tab content, labels, icons, selected state, navigation callbacks, contextual actions, accessibility descriptions, and app-specific state.

A style change must not silently change navigation semantics or remove toolbar capabilities.

## Style profiles

The intended component API should support:

- **Material 3 Expressive profile:** a familiar Material 3 visual treatment, using Material components and tokens where appropriate. It must not recreate standard controls unnecessarily.
- **App-defined profile:** consumers can supply their own tokens and component style values.
- **Solstice appearance, supplied by Solstice:** Solstice can configure the reusable toolbar to match its expressive floating pill, morphing selection, glass treatment, and contextual bud. Solstice-specific palette, glass renderer, icons, weather-derived accent, and branding remain in Solstice.

The library should provide sensible neutral defaults and a clear extension point. Avoid putting Solstice's brand assets or LGPL-derived implementation code into Motion-UI's Apache-2.0 codebase. Reimplement generic behaviour from documented requirements and independently authored code; keep the Solstice visual preset in the Solstice consumer if it contains product-specific styling.

## Animated toolbar capability contract

The reusable toolbar should be able to support, as opt-in/configurable capabilities:

- variable-width tabs with a selection surface that follows pager progress continuously;
- animated icon/label expansion and collapse;
- tap, swipe, and optional long-press-and-drag tab switching;
- interruptible transitions and velocity-aware release/retargeting;
- optional contextual action that expands from or detaches from the toolbar;
- optional secondary action state (for example pin/unpin), with semantic haptics when supported;
- app-provided tab models, icons, labels, descriptions, and callbacks;
- accessibility semantics and an alternative to gesture-only actions;
- reduced-motion adaptations that preserve state and functionality.

Do not require every consumer to enable every effect. A simpler Material 3 Expressive profile may disable glass, elastic deformation, or the contextual bud while preserving core toolbar navigation and tab behaviour.

## Delivery plan

1. Complete MOT0.3A theme/token foundation and its CI.
2. Deliver a first reusable card/surface component using the style contract.
3. Design the toolbar API as a separate proposal with behaviour/style/application configuration explicitly separated.
4. Build a standalone toolbar demo in Motion Lab with at least two visual profiles applied to the same interaction model.
5. Verify accessibility, reduced motion, gesture interruption, and app configuration before consumer adoption.
6. Integrate into Solstice only in a separate Solstice PR after the generic component is independently tested.

## Non-goals

- Do not copy Solstice/Breezy source files, icons, resources, weather logic, or branding into Motion UI.
- Do not make Material 3 Expressive the only supported visual identity.
- Do not force all consumers to use glass, elastic motion, haptics, or long-press gestures.
- Do not let visual style own navigation state or product-specific actions.
