# Continue

This file is the working handoff for the next development session. Keep it aligned with the actual branch, pull requests, CI results, and immediate next actions.

## Read first

1. [Project rules](docs/RULES.md) and [agent instructions](AGENTS.md).
2. [MOT0.3A execution plan](docs/MOT0.3A_PLAN.md).
3. [Framework overview](docs/FRAMEWORK_OVERVIEW.md) and [platform support](docs/PLATFORM_SUPPORT.md).
4. [Motion UI theme API](motion-ui/README.md), [Core API](motion/README.md), and [consumer adoption guide](docs/AI_CONSUMER_ADOPTION.md).

## Guardrails

- Motion is an independent Android / Jetpack Compose framework.
- `:motion-ui` is optional and depends on `:motion`; Core must never depend on UI.
- Applications own their branding, domain, screens, routing, and product behavior. Motion UI provides configurable reusable building blocks only.
- Material 3 remains the source for standard controls, typography, and accessibility semantics.
- Do not copy consumer-app or third-party source, assets, resources, or branding into Motion.
- Do not add Haze until its exact version, license, maintenance, compatibility, and fallback are reviewed.
- Android API 29+, compile SDK 37, JDK 21. iOS and desktop are not supported by the current artifact.
- Use focused checks during iteration and one complete required CI run on the final PR head. Report device, TalkBack, contrast, and haptic checks as deferred unless actually run.

## Verified project state

- MOT0.1 and MOT0.2 are complete.
- MOT0.3A introduces the optional `:motion-ui` Android library and the initial theme/token API.
- Initial API: `MotionUiTheme`, `MotionUiThemeTokens.current`, `MotionUiTokens`, `MotionUiShapes`, `MotionUiSpacing`, and `MotionUiMaterials`.
- Token defaults derive from the caller's Material 3 color scheme; consumers can override the tokens.
- Focused unit tests cover default light/dark values and custom token overrides.
- Required CI includes Motion UI tests and release assembly.
- The obsolete, unbuilt prototype directory was removed because it contained app-coupled source under LGPL-3.0 notices; no implementation from it was carried into Motion.
- PR #13 contains the MOT0.3A foundation and the main repository cleanup.
- PR #19 is the toolbar-contract proposal, intentionally stacked on PR #13. Recheck its base/head relationship after any changes to PR #13.
- Both PRs remain drafts until their final diffs and exact-head CI have been reviewed. Do not merge without explicit owner approval.

## MOT0.3A acceptance

- [ ] `:motion-ui` builds as an independent Android library.
- [ ] Dependency direction is UI → Core only.
- [ ] Theme defaults and app overrides pass focused tests.
- [ ] Spotless and full required CI pass on the exact final PR head.
- [ ] README, API docs, and CI reflect the delivered module.
- [ ] No consumer-specific code, dependency pin, Haze dependency, or unsupported platform work is introduced.

## Next steps

1. Finish reviewing and cleaning the MOT0.3A diff, including documentation and license/provenance notes.
2. Confirm the final PR #13 head and its exact CI result.
3. Rebase/recreate the toolbar-contract PR on the accepted MOT0.3A head; verify its own exact-head CI.
4. Review the toolbar contract before committing to a public Kotlin API.
5. After the contract is accepted, prototype the same toolbar interaction model under at least two visual profiles in Motion Lab.
6. Deliver reusable card/surface work after the toolbar contract and prototype are established.

## Deferred validation

Emulator / physical-device behavior, TalkBack traversal, contrast inspection, and hardware haptics remain NOT TESTED until run on real supported environments.
