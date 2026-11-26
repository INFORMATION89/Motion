# Motion Lab — longer-term design direction

This document records the broader product vision for Motion Lab. It is **not** the acceptance checklist for MOT0.2. The milestone scope and delivery gates live in [MOT0.2_PLAN.md](MOT0.2_PLAN.md).

Motion Lab is the standalone Android showroom and test bench for Motion's public Jetpack Compose API. It must remain generic and independent of Solstice. Motion's current supported artifact is Android / Jetpack Compose; this document does not imply universal or multiplatform support.

## Design principles

1. **Understandable quickly:** use plain language, one interaction per page, and large touch targets.
2. **Try everything:** every implemented demo should be directly interactive and explain what to notice.
3. **Fair comparisons:** compare Motion with a sensible Compose or Material baseline only when both sides can receive equivalent input and state.
4. **Useful tuning, not a control panel:** keep slow motion and meaningful tuning available without overwhelming the demo.
5. **No consumer-app coupling:** use generic content, naming, resources, and theme. Do not import Solstice screens, assets, or product assumptions.

## Current MOT0.2 implementation

- Standalone `:motion-lab` app module with a neutral Material 3 shell and gallery.
- Separate demos for spring/release, Morph, page movement, dial, haptics, and scroll edge blur.
- Optional synchronized comparisons for Morph and page movement.
- Slow-motion control with restoration of `MotionTime.scale` on root composition disposal.
- Nine semantic haptic tokens with a temporary intensity control that resets when leaving the Haptics page.

See [MOT0.2_PLAN.md](MOT0.2_PLAN.md) for exact acceptance criteria and verification status.

## Longer-term ideas — not MOT0.2 blockers

- Live miniature previews in gallery tiles and a morphing tile-to-page transition.
- More fair comparisons, potentially including sheets, menus, tabs, controls, and scroll effects.
- A collapsed tuning panel with a small number of meaningful parameters.
- Optional frame/performance diagnostics, touch visualization, and reduced-motion preview.
- Additional interaction examples such as reorder, swipe actions, and text/number transitions.

These ideas should be prioritized only when they demonstrate an existing reusable public API or a separately justified Motion capability. Do not add public Core APIs just to satisfy the Lab roadmap.

## Constraints

- Motion Core remains independent of the Lab; the Lab consumes its public API.
- Compose and Material 3 remain the standard UI and accessibility foundation.
- A comparison must use equivalent shared input; do not present unsynchronized animations as an apples-to-apples result.
- Do not introduce a sheet-specific Core abstraction merely to create a comparison.
- Keep Android-only support claims accurate. Any future platform expansion requires its own architecture, implementation, tests, and CI plan.
