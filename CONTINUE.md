# Continue

This file is the working handoff for the next development session. Keep it current with the branch, CI state, verified evidence, and immediate next actions.

## Read first

1. [Project rules](docs/RULES.md) and [agent instructions](AGENTS.md).
2. [MOT0.3A execution plan](docs/MOT0.3A_PLAN.md).
3. [Framework overview](docs/FRAMEWORK_OVERVIEW.md) and [platform support](docs/PLATFORM_SUPPORT.md).
4. [Motion UI theme API](motion-ui/README.md) and [Core API](motion/README.md).
5. [consumer adoption guide](docs/AI_CONSUMER_ADOPTION.md).

## Guardrails

- Motion is an independent Android / Jetpack Compose framework.
- `:motion-ui` is optional and depends on `:motion`; Core must never depend on UI.
- Apps own their brand values, domain, screens, routing, and product behavior. Motion UI provides configurable reusable building blocks only.
- Material 3 remains the source for standard controls, typography, and accessibility semantics.
- Do not copy consumer-app or third-party source, assets, resources, or branding into Motion.
- Do not add Haze until its exact version, licence, maintenance, compatibility, and fallback are reviewed.
- Android API 29+, compile SDK 37, JDK 21. iOS and desktop are unsupported.
- Use focused checks during iteration and one complete required CI run on the final PR head. Report device, TalkBack, contrast, and haptic checks as deferred unless actually run.

## Verified project state

- MOT0.1 and MOT0.2 are complete.
- MOT0.3 has owner approval to begin MOT0.3A.
- Implementation branch: `feat/mot03a-theme-foundation`.
- MOT0.3A creates the optional `:motion-ui` Android library and includes it in Gradle.
- Initial API: `MotionUiTheme`, `MotionUiThemeTokens.current`, `MotionUiTokens`, `MotionUiShapes`, `MotionUiSpacing`, and `MotionUiMaterials`.
- Token defaults are derived from the caller's Material 3 color scheme; app overrides are supported.
- Focused unit tests cover default light/dark material values and custom token overrides.
- Required CI has been updated to run Motion UI tests and release assembly.
- Verification of the implementation branch is pending; do not treat source creation as a passing build.
- The earlier documentation proposal remains tracked separately in PR #12 until its disposition is decided.

## MOT0.3A acceptance

- [ ] `:motion-ui` builds as an independent Android library.
- [ ] Dependency direction is UI → Core only.
- [ ] Theme defaults and app overrides pass focused tests.
- [ ] Spotless and full required CI pass on the exact final PR head.
- [ ] README, API docs, and CI reflect the delivered module.
- [ ] No consumer-specific code, submodule pointer, Haze dependency, or unsupported platform work is introduced.

## Next steps

1. Inspect and fix the first CI run for compile, formatting, or dependency issues.
2. Confirm the module API and tests against the exact branch head.
3. Keep the PR in draft until acceptance checks pass.
4. After MOT0.3A is stable, plan the first card-surface component as a separate small delivery. Do not start sheet, page-frame, or glass work in this batch.

## Deferred validation

Emulator / physical-device behavior, TalkBack traversal, contrast inspection, and hardware haptics remain NOT TESTED until run on real supported environments.
