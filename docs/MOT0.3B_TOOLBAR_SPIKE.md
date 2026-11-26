# MOT0.3B — Toolbar implementation spike

## Status

Internal Motion Lab prototype. This is an experiment to validate the contract before designing a public API for `:motion-ui`. It is not a supported reusable component and must not be consumed by another application.

## Scope

The spike exercises the same interaction model under two visual profiles without changing Motion Core or introducing a public toolbar API:

- Material 3 Expressive-oriented appearance using the active Material 3 color scheme.
- App-defined appearance reading Motion UI semantic surface and shape tokens.
- Stable tab IDs and consumer-controlled selected destination.
- Host-driven visual progress represented by `fromTabId`, `toTabId`, and a bounded fraction.
- Measured, variable-width indicator geometry and label expansion/collapse.
- Optional long-press drag selection, cancellation, and a consumer selection request.
- Optional contextual Pin/Unpin action and semantic haptic feedback.
- Horizontal overflow with selected-tab visibility management.
- Android system animator-disable preference used as the Motion Lab reduced-motion signal.

The pager and destination data belong to Motion Lab. The prototype does not make Motion UI own navigation or a pager state.

## Implementation boundaries

- `ToolbarPrototype.kt` is sample-only UI and depends on existing Motion Core and Motion UI APIs.
- `ToolbarGeometry.kt` contains sample-owned pure progress/geometry helpers. It validates finite progress, stable endpoint IDs, adjacency, and measured bounds.
- `ToolbarGeometryTest.kt` covers endpoint validation, reverse progress, variable-width interpolation, missing geometry fallback, nearest-tab selection, keyboard navigation/wrap-around, and duplicate IDs.
- No new dependency is added. The Motion Lab module reuses the repository's existing test bundle.
- CI now runs `:motion-lab:testDebugUnitTest` as well as its debug build.

## Known limitations to resolve during review

- The contextual action currently enters and exits as a separate trailing action; the final connected/detached morph is not implemented.
- Long-press drag previews the nearest measured tab. Automatic edge-scrolling during a drag still needs evaluation on a narrow viewport.
- The prototype has grouped tab semantics, roving focus via logical arrow-key navigation (mirrored for RTL), and ordinary click/Enter/Space activation. TalkBack traversal, focus indication, and physical keyboard behavior still require manual validation.
- Visual comparison uses existing Material 3 colors and Motion UI theme tokens; it is not yet a finalized style interface.
- Android's animator-enabled signal is checked by the sample during composition. Runtime preference changes while the screen is already open need a lifecycle/manual check.
- Physical haptic output, contrast, RTL behavior, large font scale, emulator/device interaction, and rapid gesture interruption remain unverified until exercised.

## Exit criteria

1. Required CI passes on the final spike commit, including the new Lab unit tests.
2. Review confirms that host progress never commits navigation state and cannot move the indicator for invalid/non-adjacent endpoints.
3. Manual review exercises forward/reverse swipes, rapid retargeting, drag release/cancel, hidden contextual action hit testing, both styles, reduced motion, RTL, and narrow widths.
4. Any missing generic behavior is classified as existing Motion Core reuse, Compose behavior, or a separately proposed Motion UI capability.
5. Only after these checks should the public toolbar API and its module-owned UI tests be designed.

Do not treat a green CI run as proof of visual, accessibility, haptic, or physical-device validation.
