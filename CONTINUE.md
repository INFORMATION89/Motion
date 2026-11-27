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
- PR #26 is open as a **Draft**, targets `main`, and must remain a draft until exact-head CI and manual validation are complete. Its current head is `89766a57aee43259076eb996c37c6959eb5ecf66`; CI #217 failed on that exact head.
- The prototype includes stable-ID host progress, variable-width geometry, drag preview/cancel, RTL-aware edge auto-scroll, a retracting contextual-action connector with one semantic button target, two visual profiles, and strict single-tab-stop keyboard navigation with an RTL-aware focus ring.
- The Lab has 15 pure unit tests covering host-progress validation, geometry interpolation/fallback, drag-target resolution, keyboard navigation/wrap-around, edge-scroll acceleration, and LTR/RTL coordinate conversion. Required CI includes `:motion-lab:testDebugUnitTest`.
- Motion Lab Compose UI tests now cover tab semantics, host-progress selection invariants, LTR/RTL focus navigation, one contextual-action target, and adjacent-tab interaction with attached/detached action profiles.
- CI runs fast formatting/unit/release checks and API 35 emulator tests in parallel jobs. Gradle parallel execution, build caching, and strict Configuration Cache compatibility checks are enabled in `gradle.properties`. The emulator job attempts direct read/write access to KVM and logs the reason if it must use the TCG fallback; see [CI design](docs/CI.md).
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
- [x] Add focused Compose UI tests for callback/semantics, LTR/RTL roving focus, physical pointer hit-testing near the contextual action in attached/detached states, and long-press drag completion/cancellation.
- [ ] Complete [manual validation record](docs/MOT0.3B_MANUAL_VALIDATION.md) on the designated emulator/device; every item remains NOT TESTED until recorded.
- [ ] Decide the smallest public Motion UI API only after the manual review.

## CI run-volume audit — 2026-11-27

- The latest 100 repository workflow runs contained 44 cancelled, 23 failed, and 33 successful runs. PR #26 accounted for 36 of those runs: 11 cancelled, 21 failed, and 4 successful.
- These are workflow-run records, not billing data; no claim about charged minutes or monetary cost is made from these counts.
- CI #216 on `53c551f586b04b65ae8d3f85179502df351872c4`: fast checks passed; API 35 Compose UI test compilation failed. Full workflow failed.
- CI #217 on PR head `89766a57aee43259076eb996c37c6959eb5ecf66`: fast checks passed; API 35 emulator instrumentation failed at `ToolbarPrototypeUiTest.kt:339-340` because `TouchInjectionScope.size` does not exist. Full workflow failed.
- The run volume exposed a process failure: small, separate commits on a PR branch each triggered another workflow. The policy now requires atomic multi-file batches, one intended full CI run per coherent batch, and owner approval before further full runs beyond one consolidated follow-up. Do not continue per-file writes or incremental pushes.
- A focused fix is staged on `fix/mot03b-pointer-injection-scope`: the touch-injection test uses `TouchInjectionScope.width` and `height` rather than the nonexistent `size` property. Commit `956f044928bb36bc7f0287ad0b0f4bb0e6a35f52` is isolated from PR #26, has not been compiled or run through CI, and must not be represented as verified.

## Next steps

1. Obtain owner approval before integrating the isolated pointer-scope fix into PR #26 and spending another full CI run; the normal two-run budget for this batch has already been used.
2. Run the [manual validation record](docs/MOT0.3B_MANUAL_VALIDATION.md) on the designated emulator/device and record failures with the tested commit SHA.
3. Fix further findings in one consolidated batch; obtain approval before another full workflow.
4. Keep PR #26 open until its CI and manual review criteria are satisfied; then review the prototype before designing a public API.
5. Design the smallest public toolbar API in `:motion-ui` only after the manual interaction/accessibility review.
6. Deliver reusable cards/surfaces only after the toolbar contract and prototype are established.

## Deferred validation

Emulator/physical-device behavior, TalkBack traversal, real focus appearance, contrast, RTL rendering, large font scaling, dynamic reduced-motion preference changes, contextual-action hit testing, and physical haptics remain NOT TESTED until actually run and recorded.

Do not merge, release, publish, tag, or update a consumer's pinned Motion commit without explicit owner approval.
