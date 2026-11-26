# Motion Lab — longer-term design direction

This document records the broader product vision for Motion Lab. It is **not** the acceptance checklist for MOT0.2. The milestone scope and delivery gates live in [MOT0.2_PLAN.md](MOT0.2_PLAN.md).

Motion Lab is the standalone Android showroom and test bench for Motion's public Jetpack Compose API. It must remain generic and independent of a consuming application. Motion's current supported artifact is Android / Jetpack Compose; this document does not imply universal or multiplatform support.

## Product role

Motion Lab has two complementary jobs:

1. **Component showroom:** explain reusable content-bearing UI building blocks in context and let people inspect how they look and behave.
2. **Interaction test bench:** let people exercise the controls, input patterns, and navigation behaviors that shape how users act and move through an app.

It should not be only a gallery of isolated animations. Motion is demonstrated through complete, understandable UI situations.

## Information architecture

The top-level app should use a familiar Material 3 app structure with a consistent toolbar and two primary destinations:

- **Components** — content-bearing building blocks that present information and context. Examples include cards, pages, and place-like content surfaces.
- **Interactions** — controls and input patterns through which users provide input, trigger actions, and navigate. Examples include toolbars, buttons, dropdown menus, checkboxes, gestures, and predictive-back behavior.

A component can contain interactions; the distinction is about what the demo is teaching. A card that demonstrates its content hierarchy belongs under Components, while a button behavior or menu interaction belongs under Interactions.

Use standard Material 3 tabs/navigation and platform conventions. Do not invent a custom toolbar or navigation framework. Keep the navigation predictable and consistent across demo pages.

## Design principles

1. **Understandable quickly:** use plain language, one primary concept per demo, large touch targets, and enough surrounding context to understand why the component or interaction exists.
2. **Try everything:** each implemented demo should be directly interactive and explain what to notice.
3. **System appearance by default:** follow the device's light/dark setting automatically. Maintain semantic contrast and legibility in both schemes.
4. **Optional pure-black appearance:** offer a separate explicit pure-black/AMOLED-style option only if it can be implemented cleanly and consistently. It must not override or replace automatic system appearance.
5. **Optional predictive-back blur:** treat blur as progressive enhancement, not a requirement. Prefer AndroidX/Compose/platform predictive-back integration, apply blur only where supported, and provide a graceful fallback without blur.
6. **Fair comparisons:** compare Motion with a sensible Compose or Material baseline only when both sides can receive equivalent input and state.
7. **Useful tuning, not a control panel:** keep slow motion and meaningful tuning available without overwhelming the demo.
8. **No consumer-app coupling:** use generic content, naming, resources, and theme. Do not import consumer screens, assets, or product assumptions.
9. **Reuse before rebuilding:** use Compose, Material 3, AndroidX, and existing Motion APIs for standard controls, navigation, blur, and animation. Add new abstractions only for a demonstrated reusable gap; do not reinvent platform capabilities.

## Current MOT0.2 implementation

- Standalone `:motion-lab` app module with a neutral Material 3 shell and gallery.
- Separate demos for spring/release, Morph, page movement, dial, haptics, and scroll edge blur.
- Two top-level Material 3 destinations: Components (Morph, Page movement, Scroll edge blur) and Interactions (Springs and release, Dial, Haptics).
- Automatic light/dark appearance following the system setting with semantic Material 3 color schemes.
- Optional synchronized comparisons for Morph and page movement.
- Slow-motion control with restoration of `MotionTime.scale` on root composition disposal.
- Nine semantic haptic tokens with a temporary intensity control that resets when leaving the Haptics page.

See [MOT0.2_PLAN.md](MOT0.2_PLAN.md) for exact acceptance criteria and verification status.

## Follow-up roadmap ideas — not MOT0.2 blockers

- Consider an explicit pure-black/AMOLED theme option.
- Explore predictive-back blur as an optional, capability-gated enhancement with a no-blur fallback.
- Live miniature previews in gallery tiles and a morphing tile-to-page transition.
- More fair comparisons, potentially including sheets, menus, tabs, controls, and scroll effects.
- A collapsed tuning panel with a small number of meaningful parameters.
- Optional frame/performance diagnostics, touch visualization, and reduced-motion preview.
- Additional interaction examples such as reorder, swipe actions, and text/number transitions.

Prioritize these ideas only when they demonstrate an existing reusable public API or a separately justified Motion capability. Do not add public Core APIs just to satisfy the Lab roadmap.

## Constraints

- Motion Core remains independent of the Lab; the Lab consumes its public API.
- Compose and Material 3 remain the standard UI and accessibility foundation.
- A comparison must use equivalent shared input; do not present unsynchronized animations as an apples-to-apples result.
- Do not introduce a sheet-specific Core abstraction merely to create a comparison.
- Keep Android-only support claims accurate. Any future platform expansion requires its own architecture, implementation, tests, and CI plan.
