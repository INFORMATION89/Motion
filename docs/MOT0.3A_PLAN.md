# MOT0.3A — Motion UI theme foundation

**Status:** implementation in progress; owner approved MOT0.3 direction on 2026-11-26.

## Goal

Establish the optional `:motion-ui` Android library and its app-configurable theme/token contract without adding visual components or changing Motion Core.

## Delivered in this branch

- Add `:motion-ui` as an Android library module.
- Include it in the standalone Gradle build.
- Set dependency direction to `:motion-ui` → `:motion`; Core remains independent.
- Expose `MotionUiTheme` to supply Material 3 color scheme, typography, shapes, and Motion UI tokens.
- Expose semantic `MotionUiShapes`, `MotionUiSpacing`, and `MotionUiMaterials` tokens.
- Derive neutral material defaults from the app-provided Material 3 color scheme.
- Add focused unit tests for light/dark defaults and consumer overrides.
- Add Motion UI tests and release assembly to required CI.
- Document the module and update the project handoff.

## Public API contract

- Apps own their Material 3 theme and may supply the values to `MotionUiTheme`.
- Apps may replace Motion UI token values without forking the library.
- Defaults are neutral and based on Material 3 surface roles; they are not a fixed Motion brand.
- Motion UI does not create replacement buttons, fields, navigation, or accessibility semantics.
- Core APIs and behavior remain unchanged.
- Token classes remain limited to values needed by the first shared surfaces; this is not a second full design system.

## Out of scope

- Card/page/sheet components (subsequent MOT0.3B deliveries).
- Glass rendering and Haze dependency (conditional MOT0.3C review).
- the consuming application integration or submodule changes.
- Motion Core changes, publishing, and platform expansion.
- Device/TalkBack validation unless separately scheduled and available.

## Verification

- [ ] Spotless.
- [ ] `:motion-ui:testDebugUnitTest`.
- [ ] `:motion-ui:assembleRelease`.
- [ ] Existing Core tests and release build.
- [ ] Motion Lab debug build.
- [ ] Required CI passes on the exact final PR head.
- [ ] Confirm the module has no reverse dependency from Core and no consumer-app dependency.

## Next stage

After MOT0.3A is accepted, deliver a configurable card surface as the first MOT0.3B component. Keep page frame, sheet building blocks, and glass rendering separate so each has a clear contract and focused verification.
