# Continue

This file is the working handoff for the next development session. Keep it aligned with actual branches, pull requests, exact-head CI, and immediate next actions.

## Read first

1. [Project rules](docs/RULES.md) and [agent instructions](AGENTS.md).
2. [CI workflow and performance design](docs/CI.md).
3. [MOT0.3A execution plan](docs/MOT0.3A_PLAN.md).
4. [MOT0.3B toolbar contract](docs/MOT0.3B_TOOLBAR_PLAN.md) and [implementation spike](docs/MOT0.3B_TOOLBAR_SPIKE.md).
5. [Framework overview](docs/FRAMEWORK_OVERVIEW.md) and [platform support](docs/PLATFORM_SUPPORT.md).
6. [Motion UI theme API](motion-ui/README.md), [Core API](motion/README.md), and [consumer adoption guide](docs/AI_CONSUMER_ADOPTION.md).

## Guardrails

- Motion is an independent Android / Jetpack Compose framework.
- `:motion-ui` is optional and depends on `:motion`; Core must never depend on UI.
- Applications own their branding, domain, screens, routing, and product behavior. Motion UI provides configurable reusable building blocks only.
- Material 3 remains the source for standard controls, typography, and accessibility semantics.
- Do not copy consumer-app or third-party source, assets, resources, or branding into Motion.
- Do not add Haze until its exact version, license, maintenance, compatibility, and fallback are reviewed.
- Android API 29+, compile SDK 37, JDK 21. iOS and desktop are not supported by the current artifact.
- Report device, TalkBack, contrast, RTL, font-scale, and haptic checks as deferred unless actually run.

## Verified project state

- MOT0.1 and MOT0.2 are complete.
- MOT0.3A introduces the optional `:motion-ui` Android library and the initial configurable theme/token API.
- PR #13 (MOT0.3A) merged into `main` on 2026-11-27; merge commit `2887d8aec1e06cfe8a747d20b0f1b32ee826e6ea`.
- PR #25 (MOT0.3B toolbar contract) merged into `main` after PR #13 on 2026-11-27; merge commit `51420fdbb117ddfb8738263cf428b85e197a7719`.
- The toolbar contract uses stable endpoint IDs for host progress and explicitly separates local drag ownership from consumer-controlled selection.
- Branch `feat/mot03b-toolbar-prototype` carries the internal Motion Lab spike. It is not a public API.
- PR #26 is open, targets `main`, and is not a draft. CI #197 is a historical checkpoint, not verification of the current head.
- The prototype includes stable-ID host progress, variable-width geometry, drag preview/cancel, RTL-aware edge auto-scroll, a retracting contextual-action connector with one semantic button target, two visual profiles, and strict single-tab-stop keyboard navigation with an RTL-aware focus ring.
- The Lab has 15 pure unit tests covering host-progress validation, geometry interpolation/fallback, drag-target resolution, keyboard navigation/wrap-around, edge-scroll acceleration, and LTR/RTL coordinate conversion. Required CI includes `:motion-lab:testDebugUnitTest`.
- Motion Lab Compose UI tests now cover tab semantics, host-progress selection invariants, LTR/RTL focus navigation, one contextual-action target, and adjacent-tab interaction with attached/detached action profiles.
- CI runs fast formatting/unit/release checks and API 35 emulator tests in parallel jobs. Gradle parallel execution, build caching, and strict Configuration Cache compatibility checks are enabled in `gradle.properties`; see [CI design](docs/CI.md).
- The obsolete, unbuilt prototype directory was removed after provenance/license review; no implementation was copied from it.

## MOT0.3A acceptance

- [x] `:motion-ui` builds as an independent Android library.
- [x] Dependency direction is UI → Core only.
- [x] Theme defaults and app overrides have focused tests.
- [x] Spotless and full required CI pass on the cleaned PR #13 head (CI #176).
- [x] README, API docs, and CI reflect the delivered module.
- [x] No consumer-specific source, branding, dependency pin, Haze dependency, or unsupported platform work was introduced.
- [ ] Emulator/TalkBack, contrast, and physical-device checks remain deferred.

## Current MOT0.3B work

- [x] Contract written and synchronized with the current MOT0.3A head.
- [x] Internal Motion Lab spike added on a separate stacked branch.
- [x] Pure progress/geometry tests added and included in required CI.
- [x] Review the complete spike diff for the implemented interaction paths.
- [x] Historical checkpoint: Spotless, Core tests/build, Motion UI tests/build, Motion Lab tests, and Motion Lab debug build passed on PR #26 head `75b6c9d` (CI #197).
- [x] Implement the contextual-action connector morph, edge auto-scroll, and strict roving keyboard focus in the Motion Lab spike.
- [ ] Perform manual emulator/device, TalkBack, focus appearance, contrast, RTL, font-scale, live reduced-motion preference, contextual-action hit-testing, and haptic checks.
- [x] Add focused Compose UI tests for callback/semantics, LTR/RTL roving focus, and physical pointer hit-testing near the contextual action in both attached and detached states.
- [ ] Decide the smallest public Motion UI API only after the manual review.

## Next steps

1. Review and manually exercise the latest prototype on an emulator/device, especially TalkBack, visible focus, RTL, narrow layouts, connector hit targets, and rapid interruption.
2. Add focused Compose UI tests for contextual-action semantics, adjacent-tab hit testing, and focus traversal.
3. Keep PR #26 open against `main` until exact-head CI and the manual validation checklist pass; then review the prototype before designing a public API.
4. After manual interaction/accessibility review, design the public toolbar API in `:motion-ui`.
5. Deliver reusable cards/surfaces only after the toolbar contract and prototype are established.

## Deferred validation

Emulator/physical-device behavior, TalkBack traversal, real focus appearance, contrast, RTL rendering, large font scaling, dynamic reduced-motion preference changes, contextual-action hit testing, and physical haptics remain NOT TESTED until actually run and recorded.

Do not merge, release, publish, tag, or update a consumer's pinned Motion commit without explicit owner approval.
