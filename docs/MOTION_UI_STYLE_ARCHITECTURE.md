# Motion UI styling and toolbar architecture

## Product direction

Motion UI components must not be locked to one visual identity. A component's interaction contract remains stable while its visual treatment can be selected or customized by the consuming application.

The animated toolbar is a first-class component. Preserve its advanced interaction model rather than replacing it with a standard Material 3 top app bar simply because Material 3 is the default component library.

## Three independent concerns

1. **Behavior** — tab selection, host-progress synchronization, drag/long-press gestures, velocity handling, interruptibility, spring choreography, contextual-action expansion, and reduced-motion behavior. Use Motion Core for reusable motion mechanics.
2. **Style** — dimensions, shapes, colors, typography roles, surface treatment, elevation, icon treatment, and genuinely visual animation parameters.
3. **Application configuration** — tab content, labels, icons, selected state, navigation callbacks, contextual actions, accessibility descriptions, and application-owned state.

A style change must not silently change navigation semantics or remove toolbar capabilities.

## Style profiles

The intended component API should support:

- **Material 3 Expressive-oriented profile:** a familiar Material 3 visual treatment, using Material components and tokens where appropriate. It must not recreate standard controls unnecessarily.
- **App-defined profile:** consumers can supply their own tokens and component style values.
- **Consumer-defined appearance:** each application supplies its own visual tokens and optional content slots for the toolbar's container, selection indicator, tabs, and contextual action. Domain-derived accents, custom rendering, iconography, and branding remain in the application.

Motion UI should provide sensible neutral defaults and clear extension points. Do not include consumer-owned brand assets or third-party implementation code. Reimplement generic behavior from documented requirements and independently authored code; keep product-specific visual presets in the consuming application.

## Animated toolbar capability contract

The reusable toolbar should support the following as opt-in/configurable capabilities:

- Variable-width tabs with a selection surface that follows host progress continuously.
- Animated icon/label expansion and collapse.
- Tap activation, host-driven swipe progress, and optional long-press-and-drag tab switching.
- Interruptible transitions and velocity-aware release/retargeting.
- An optional contextual action that expands from or detaches from the toolbar.
- Optional secondary action state, with semantic haptics when supported.
- Application-provided tab models, icons, labels, descriptions, and callbacks.
- Accessibility semantics and alternatives to gesture-only actions.
- Reduced-motion adaptations that preserve state and functionality.

Do not require every application to enable every effect. A simpler Material 3 Expressive profile may disable glass, elastic deformation, or contextual-action morphing while preserving core tab behavior and accessibility.

## Delivery plan

1. Complete and review the MOT0.3A theme/token foundation.
2. Define and review the dedicated MOT0.3B toolbar contract.
3. Prototype the toolbar in Motion Lab, applying at least two visual profiles to the same interaction state and callbacks.
4. Verify accessibility, reduced motion, gesture interruption, and application configuration before public API hardening.
5. Deliver reusable card/surface components after the toolbar contract and prototype are established.
6. Consider integration into a consumer application only in a separate, independently reviewed change after the generic component is tested.

## Non-goals

- Do not copy consumer or third-party source files, icons, resources, domain logic, or branding into Motion UI.
- Do not make Material 3 Expressive the only supported visual identity.
- Do not force all consumers to use glass, elastic motion, haptics, or long-press gestures.
- Do not let visual style own navigation state or product-specific actions.
