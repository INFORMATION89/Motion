# Motion UI styling and toolbar architecture

## Product direction

Motion UI components must not be locked to one visual identity. A component's interaction contract should remain stable while its visual treatment can be selected or customised by the consuming app.

The animated toolbar is a flagship component: preserve its advanced interaction model instead of replacing it with a standard Material 3 top app bar simply because Material 3 is the default component library.

## Three independent concerns

1. **Behaviour** — tab selection, host progress synchronisation, drag/long-press gestures, velocity handling, interruptibility, spring choreography, contextual action expansion, and reduced-motion behaviour. Use Motion Core for reusable motion mechanics.
2. **Style** — dimensions, shapes, colors, typography roles, surface treatment, elevation, icon treatment, and animation styling parameters that are genuinely visual.
3. **Application configuration** — tab content, labels, icons, selected state, navigation callbacks, contextual actions, accessibility descriptions, and app-specific state.

A style change must not silently change navigation semantics or remove toolbar capabilities.

## Style profiles

The intended component API should support:

- **Material 3 Expressive profile:** a familiar Material 3 visual treatment, using Material components and tokens where appropriate. It must not recreate standard controls unnecessarily.
- **App-defined profile:** consumers can supply their own tokens and component style values.
- **Consumer-defined appearance:** the consuming application supplies its own visual tokens and optional content slots for the toolbar's container, selection indicator, tabs, and contextual action. Domain-derived accents, custom rendering, iconography, and branding remain in the consuming application.

The library should provide sensible neutral defaults and a clear extension point. Do not include consumer-owned brand assets or third-party implementation code in Motion UI. Reimplement generic behaviour from documented requirements and independently authored code; keep product-specific visual presets in the consuming application.

## Animated toolbar capability contract

The reusable toolbar should be able to support, as opt-in/configurable capabilities:

- variable-width tabs with a selection surface that follows host progress continuously;
- animated icon/label expansion and collapse;
- tap, host swipe, and optional long-press-and-drag tab switching;
- interruptible transitions and velocity-aware release/retargeting;
- optional contextual action that expands from or detaches from the toolbar;
- optional secondary action state (for example pin/unpin), with semantic haptics when supported;
- app-provided tab models, icons, labels, descriptions, and callbacks;
- accessibility semantics and an alternative to gesture-only actions;
- reduced-motion adaptations that preserve state and functionality.

Do not require every consumer to enable every effect. A simpler Material 3 Expressive profile may disable glass, elastic deformation, or the contextual bud while preserving core toolbar navigation and tab behaviour.

## Delivery plan

1. Complete and review the MOT0.3A theme/token foundation.
2. Define the animated toolbar contract in a dedicated proposal (see [MOT0.3B toolbar plan](MOT0.3B_TOOLBAR_PLAN.md)).
3. Prototype the toolbar in Motion Lab, applying at least two visual profiles to the same interaction state and callbacks.
4. Verify accessibility, reduced motion, gesture interruption, and consumer configuration before public API hardening.
5. Deliver reusable card/surface components after the toolbar contract and prototype are established.
6. Integrate into the consuming application only in a separate the consuming application PR after the generic component is independently tested.

## Non-goals

- Do not copy consumer or third-party source files, icons, resources, domain logic, or branding into Motion UI.
- Do not make Material 3 Expressive the only supported visual identity.
- Do not force all consumers to use glass, elastic motion, haptics, or long-press gestures.
- Do not let visual style own navigation state or product-specific actions.
