# Continue

This file is the working handoff for the next development session. Keep it aligned with actual branches, pull requests, exact-head CI, and immediate next actions.

## Read first

1. [Project rules](docs/RULES.md) and [agent instructions](AGENTS.md).
2. [MOT0.3A execution plan](docs/MOT0.3A_PLAN.md).
3. [MOT0.3B toolbar contract](docs/MOT0.3B_TOOLBAR_PLAN.md) and [implementation spike](docs/MOT0.3B_TOOLBAR_SPIKE.md).
4. [Framework overview](docs/FRAMEWORK_OVERVIEW.md) and [platform support](docs/PLATFORM_SUPPORT.md).
5. [Motion UI theme API](motion-ui/README.md), [Core API](motion/README.md), and [consumer adoption guide](docs/AI_CONSUMER_ADOPTION.md).

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
- PR #13 is open and ready for review, not merged. CI #176 passed on its exact head `1c87bfd23c8b8cbd63145d38d1dd613fd01d7ec7`.
- PR #25 contains the MOT0.3B toolbar contract, remains a draft, and is stacked on the MOT0.3A branch. CI #178 passed on its exact head `98886cc2f5a21cacd9d9ce0ca66a4ef992c69afa`.
- The toolbar contract uses stable endpoint IDs for host progress and explicitly separates local drag ownership from consumer-controlled selection.
- A separate branch, `feat/mot03b-toolbar-prototype`, carries the internal Motion Lab spike. It is not a public API and remains stacked on PR #25.
- PR #26 is open as a draft. CI #190 passed on its exact head `3b2ac1d525dd80a2fb6e72e443f63a6981bed506`.
- The prototype includes stable-ID host progress, variable-width geometry, drag preview/cancel, optional contextual action/haptics, two visual profiles, and arrow-key focus transfer with LTR/RTL mapping.
- The Lab unit tests cover host progress validation, geometry interpolation/fallback, drag-target resolution, and keyboard navigation/wrap-around. Required CI includes `:motion-lab:testDebugUnitTest`.
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
- [x] Spotless, Core tests/build, Motion UI tests/build, Motion Lab tests, and Motion Lab debug build pass on the exact PR #26 head (CI #190).
- [ ] Perform manual emulator/device, TalkBack, focus indication, contrast, RTL, font-scale, reduced-motion, drag edge-scroll, contextual-action hit-testing, and haptic checks.
- [ ] Resolve the known prototype limitations before promoting the design to a public API.
- [ ] Decide the smallest public Motion UI API only after the manual review.

## Next steps

1. Review and manually exercise the prototype on an emulator/device, especially accessibility, RTL, narrow layouts, and rapid interruption.
2. Decide whether to implement the connected/detached contextual-action morph and drag edge auto-scroll in the spike.
3. Keep PR #26 stacked on PR #25 until the contract and MOT0.3A foundation are accepted in order.
4. After manual interaction/accessibility review, design the public toolbar API in `:motion-ui`.
5. Deliver reusable cards/surfaces only after the toolbar contract and prototype are established.

## Deferred validation

Emulator/physical-device behavior, TalkBack traversal, focus indication, contrast, RTL, large font scaling, contextual-action hit testing, reduced-motion preference changes, and physical haptics remain NOT TESTED until actually run and recorded.

Do not merge, release, publish, tag, or update a consumer's pinned Motion commit without explicit owner approval.
