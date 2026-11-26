# MOT0.3A — Motion UI theme foundation

**Status:** implementation in progress; owner approved MOT0.3 direction on 2026-11-26.

## Goal

Establish the optional Motion UI Android library and its app-configurable theme/token contract without adding visual components or changing Motion Core.

## Delivered in this branch

- Add the optional Motion UI Android library module.
- Include it in the standalone Gradle build.
- Set dependency direction to Motion UI → Motion Core; Core remains independent.
- Expose MotionUiTheme to supply Material 3 color scheme, typography, shapes, and Motion UI tokens.
- Expose semantic MotionUiShapes, MotionUiSpacing, and MotionUiMaterials tokens.
- Derive neutral material defaults from the app-provided Material 3 color scheme.
- Add focused unit tests for light/dark defaults and consumer overrides.
- Add Motion UI tests and release assembly to required CI.
- Add a live token preview to Motion Lab.
- Document the module and update the project handoff.

## Public API contract

- Apps own their Material 3 theme and may supply the values to MotionUiTheme.
- Apps may replace Motion UI token values without forking the library.
- Defaults are neutral and based on Material 3 surface roles; they are not a fixed Motion brand.
- Motion UI does not create replacement buttons, fields, navigation, or accessibility semantics.
- Core APIs and behavior remain unchanged.
- Token classes remain limited to values needed by shared components; this is not a second full design system.

## Out of scope

- Animated toolbar implementation and consumer migration (planned separately in MOT0.3B).
- Reusable card/page/sheet components until the toolbar contract and prototype are established.
- Glass rendering and Haze dependency (requires a separate review).
- Solstice integration or submodule changes.
- Motion Core changes, publishing, and platform expansion.
- Device/TalkBack validation unless separately scheduled and available.

## Verification

- [x] Spotless.
- [x] Motion UI unit tests.
- [x] Motion UI release assembly.
- [x] Existing Core unit tests and release build.
- [x] Motion Lab debug build.
- [x] Required CI passed on the exact final PR head.
- [x] Confirm the module has no reverse dependency from Core and no consumer-app dependency.
- [ ] Emulator/TalkBack and physical-device checks (not claimed as completed).

## Next stage

MOT0.3B defines and prototypes the animated toolbar as a first-class reusable component. It must preserve variable-width, progress-driven selection and optional contextual-action interactions while supporting multiple visual profiles. Reusable card/surface work follows this toolbar milestone.
