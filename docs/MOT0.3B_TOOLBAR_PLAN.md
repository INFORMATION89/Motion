# MOT0.3B — Animated toolbar contract and implementation plan

## Status

Proposal only. This document defines the reusable component contract and a staged implementation. It does not commit the project to a final public Kotlin API, copy third-party implementation code, or migrate a consumer application.

**Prerequisite:** MOT0.3A theme/token foundation (the optional Motion UI module) is reviewed and accepted.

## 1. Goal

Build an optional animated toolbar in Motion UI around a reusable interaction contract while allowing each consuming application to choose its own visual identity.

The toolbar is a first-class Motion UI component, not a wrapper around a standard top app bar and not a navigation framework. Material 3 remains the preferred source for standard controls and semantics where appropriate.

The key invariant is:

> One interaction model, multiple visual profiles, consumer-owned navigation and actions.

## 2. Reusable interaction capabilities

The toolbar requirements identify several distinct capabilities worth preserving as generic, opt-in behaviors:

- Tabs have variable widths. The active tab unfolds its label while inactive tabs can remain compact.
- Selection geometry, tab widths, and label visibility follow continuous pager progress, including a swipe that reverses direction.
- The selection surface can be pressed and lifted, then dragged across tabs; release settles on the nearest destination.
- A contextual action can grow from the end of the toolbar, remain visually attached through a deforming connector, and then detach into its own action target.
- The contextual action can represent a stateful action such as pin/unpin, with a small state-change animation and optional semantic haptic feedback.
- The toolbar can adapt its available tabs and contextual action to different host configurations.

These are behavioural requirements, not a mandate to reproduce a consumer's materials, colors, domain-derived accents, iconography, dimensions, labels, or exact animation tuning.

No consumer or third-party source, assets, strings, icons, theme classes, domain state, or app navigation code may be copied into Motion UI. Implement the generic contract independently from this description. The consumer remains responsible for its own styling and product semantics.

## 3. Ownership boundaries

### Motion UI owns

- A reusable composable structure for tabs and an optional contextual-action slot.
- Stable tab identity and layout/selection presentation.
- The interaction state machine for toolbar-specific gestures and visual transitions.
- A style contract and neutral defaults.
- Semantics and accessible alternatives for every optional gesture.
- Integration points for Motion Core motion primitives.

### Motion Core owns

- Reusable spring/physics primitives, interruptible motion values, retargeting, settling, velocity projection/handoff, and reduced-motion policy primitives where already available.
- Generic gesture/motion mechanics only when they are useful outside this component.

Do not add a second animation engine or duplicate Core physics inside the toolbar. Do not move component layout, icon/label policy, navigation callbacks, or style tokens into Motion Core.

### The consumer application owns

- The selected destination and navigation/pager state.
- Tab content, stable IDs, labels, icons, content descriptions, enabled/visible policy, and callbacks.
- The mapping between pager progress and toolbar progress.
- Contextual action meaning, enabled/checked state, icon, label, and callback.
- Product-specific appearance, including consumer-specific palette, materials, iconography, and branding.

Motion UI must not import a navigation library, own a back stack, or assume a pager is present.

## 4. Conceptual component contract

The eventual API should be declarative and controlled by the consumer. Names below are conceptual, not approved public API.

### Tab model

Each tab should provide:

- A stable, unique ID (not its current list index).
- A label suitable for visible text and accessibility.
- An icon slot or consumer-provided icon content; selected/unselected icon variants may be supplied when useful.
- An explicit content description when the icon or visible label is not sufficient.
- Enabled and visible state.
- An optional selected-state description if the consumer needs one.
- A selection callback routed through the consumer.

Do not put Android string resource IDs, domain-specific types, ImageVector assumptions, or app-specific page enums in the public model. Prefer composable icon/content slots or a small stable abstraction that does not constrain consumers to one icon library.

The API must define what happens when a selected tab becomes disabled or is removed. It must not silently select an arbitrary destination; the consumer remains the source of truth.

### Controlled selection and progress

Support two clear usage modes:

1. **Discrete selection:** consumer supplies selected tab ID and receives selection requests.
2. **Continuous host progress:** consumer may additionally supply a normalized fractional position/progress source so the indicator and tab expansion can track an external pager during a swipe.

The toolbar must not own a PagerState. It should not require HorizontalPager or any particular navigation solution.

Progress must be validated/clamped and mapped against the current visible, enabled tab order. Define behavior for interrupted updates, list changes, and non-adjacent jumps. Stable IDs must remain the semantic identity even though interpolation uses measured positions.

### Contextual action slot

The toolbar may expose one optional contextual action that can:

- Be absent entirely.
- Expand from the toolbar edge and optionally detach into a separate target.
- Receive consumer-owned icon/content, label, description, enabled state, and callback.
- Represent checked/toggled state when applicable.
- Report its measured bounds when a consumer needs coordinated transitions.

Its action semantics must be available independently of its visual expansion. A consumer must be able to invoke it without first triggering a drag or waiting for an animation. Avoid baking in any product-specific meaning.

### Configuration

Configuration should distinguish capabilities from styling:

- Enable/disable long-press drag.
- Enable/disable contextual action morphing.
- Choose whether labels collapse or remain visible.
- Provide spacing/size constraints and overflow policy.
- Choose motion profile/reduced-motion behavior.
- Supply callbacks for selection, drag interruption, and contextual action.

Do not expose dozens of raw animation constants in the first public API. Start with named semantic motion profiles and a narrow expert override only if real consumer needs demonstrate it.

## 5. Interaction and animation requirements

### Selection geometry

- Measure tabs and interpolate the active selection surface across their actual bounds; do not assume equal widths.
- Interpolate both horizontal position and width continuously from fractional progress.
- Animate label expansion and collapse without shifting the icon unpredictably.
- Keep geometry stable when labels have different lengths, font scaling changes, layout direction is RTL, or the available width is constrained.
- Define deterministic behavior for one tab, zero tabs, and a tab list that changes while an animation is in flight.

### Tap and host swipe

- Tapping a tab emits a selection request; it does not mutate app navigation internally.
- Host swipe progress may drive the visual selection continuously.
- If the host changes target mid-transition, retarget smoothly without snapping or leaving stale animation jobs.
- A consumer may notify the toolbar that another transition should be interrupted; this must not become a hard dependency on a particular navigation system.

### Optional long-press-and-drag selection

- When enabled, long-pressing the selection surface may lift/pop it and allow horizontal dragging across tabs.
- Drag position should track the pointer predictably and map to the nearest valid tab.
- On release, request the nearest destination and settle the visual state.
- On cancellation, restore the controlled selection/progress without firing a false selection.
- Handle pointer cancellation, list changes, disabled tabs, RTL, and nested scroll/gesture competition.
- Provide an accessible alternative: keyboard/focus navigation and ordinary tab activation must work without this gesture.

### Contextual action morph

- The action may expand from the toolbar edge, use a configurable connector/deformation, and detach into its own hit target.
- Join, detach, wobble, and rejoin transitions must be interruptible and retargetable.
- The hit target must not intercept taps intended for an adjacent tab while the action is visually tucked away.
- When the action is disabled or absent, no invisible hit target may remain.
- The component may expose progress/bounds for coordinated consumer transitions, but must not own the resulting screen or overlay.

### Haptics

- Haptics are optional, semantic, and capability-aware.
- No haptic feedback is required for correctness.
- Respect system/user settings and avoid firing repeatedly during continuous progress.
- Keep the callback/semantic boundary testable without physical hardware.

## 6. Style architecture

The same behavioural component must support the following profiles without changing its navigation or action semantics:

### Material 3 Expressive-oriented profile

- Use Material 3 color, typography, shape, and elevation roles where they fit.
- Keep the visual treatment recognizable and appropriately expressive.
- It may disable glass, connector deformation, detached action morphing, or long-press drag by configuration, but must not remove standard tab selection or accessible activation.
- Do not rebuild standard Material controls merely to animate them.

### App-defined profile

- Allow a consumer to supply tokens or a style implementation for container, selected indicator, tab sizes, shape, typography, icon treatment, contextual action, and motion emphasis.
- Reuse the MotionUiTheme token foundation where it is a good fit; extend it only after the component demonstrates the exact missing semantic roles.
- Keep layout/interaction code independent from style selection.

### Consumer-defined appearance

A consumer may configure the app-defined profile to match its own visual language and product actions. Consumer-specific renderers, colors, domain-derived state, icons, labels, and brand decisions remain in the consumer application. Motion UI must not bundle a consumer brand preset, palette, or asset.

### Style invariants

Changing style must not:

- Change the selected tab ID or navigation callback contract.
- Change enabled/disabled state or action semantics.
- Remove accessibility descriptions or keyboard activation.
- Silently enable a gesture that the consumer disabled.
- Change the host progress mapping.

## 7. Accessibility, input, and resilience

- Expose tab semantics with role, selected state, enabled state, and meaningful labels/descriptions.
- Ensure each tab and contextual action has an accessible focus target and a practical touch target (target at least 48 dp where platform guidance applies).
- Support TalkBack/semantics traversal, keyboard activation, focus indication, and directional navigation.
- Ensure labels remain available to assistive technology when visually collapsed.
- Do not use animation alone to communicate selection or checked state.
- Respect font scaling, RTL, contrast, and dynamic layout constraints.
- Provide a reduced-motion mode that preserves the same final states and actions while removing or simplifying elastic deformation, overshoot, wobble, and nonessential scaling.
- Verify that reduced motion does not disable navigation, hide actions, or make state ambiguous.
- Define behavior when animations are interrupted, the host recomposes, the window resizes, or tabs change.

## 8. Testing and verification strategy

Keep tests focused on distinct failure modes; avoid duplicating exhaustive permutations.

### Pure/unit tests

- Stable ID and tab-list reconciliation.
- Fractional progress clamping and mapping.
- Variable-width selection geometry and interpolation.
- Retargeting and interruption.
- Release/cancel target resolution, including RTL and disabled tabs.
- Contextual action state transitions and hit-target visibility rules.
- Reduced-motion state equivalence.

### Compose UI tests

- Tab click emits the correct callback and does not mutate controlled selection.
- Selected semantics update from consumer state.
- Collapsed labels remain accessible.
- Contextual action invokes its callback and does not steal adjacent-tab taps while tucked away.
- Keyboard/focus activation and disabled-state behavior.
- At least one long-press-drag success and cancellation path.

### Visual/manual checks

- Motion Lab shows the same data and interaction model under at least two styles: M3 Expressive-oriented and a deliberately custom profile.
- Variable label lengths, narrow widths, RTL, light/dark themes, and larger font scale.
- Rapid direction reversals and repeated retargeting.
- Reduced-motion mode.
- TalkBack and physical touch target checks on an emulator/device.

Do not claim TalkBack, device, contrast, or haptic validation unless it has actually been run and recorded.

## 9. Delivery sequence

1. **Contract review:** accept this behavioural/style/accessibility contract before committing to a public Kotlin API.
2. **API spike:** build a minimal internal prototype with controlled tab IDs, fractional host progress, variable-width geometry, and no consumer-specific styling.
3. **Motion Lab comparison:** apply two style profiles to the exact same tabs, state, and callbacks. Include a simple contextual action and demonstrate that it can be disabled.
4. **Core integration:** reuse existing Motion Core primitives where appropriate; add a Core API only if the prototype identifies a generic missing capability.
5. **Accessibility and resilience:** add semantics, keyboard/focus support, reduced-motion behavior, and focused UI tests.
6. **Public API hardening:** document lifecycle, state ownership, list mutation, progress mapping, and compatibility before declaring the API stable.
7. **Consumer adoption:** only in a separate consumer-repository change, with the consumer supplying its appearance and product actions. Do not update a consumer repository's dependency pin or source in this workstream.

A reusable card/surface may follow this toolbar milestone; it must not delay or replace the toolbar work.

## 10. Explicit non-goals and guardrails

- No changes to consumer source, its submodule pointer, or its open pull requests in this workstream.
- No copied consumer or third-party code, assets, icons, resources, strings, theme classes, or domain logic.
- No navigation/back-stack ownership and no required pager dependency.
- No Haze/blur dependency in the first toolbar milestone.
- No requirement that every style use glass, gooey deformation, haptics, or long-press drag.
- No premature public API commitment before the Motion Lab prototype.
- No platform expansion beyond the repository's currently documented Android/Compose support.
- No claim of runtime, accessibility, or device verification based only on compilation.

## Acceptance criteria for the first implementation

- A consumer can render the toolbar with controlled stable tab IDs and callbacks without adopting a navigation library.
- A host can drive continuous selection progress without passing a pager object.
- Variable-width tabs interpolate smoothly and can be retargeted.
- Two distinct visual profiles use the same interaction state and callbacks.
- The contextual action is optional, independently accessible, and cannot intercept neighbouring tabs while hidden.
- Reduced motion preserves all functionality.
- Focused unit/UI tests cover the critical state, geometry, semantics, and cancellation paths.
- No consumer-specific source, styling, branding, or dependency enters Motion UI.
