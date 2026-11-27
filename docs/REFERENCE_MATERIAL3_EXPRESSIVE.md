# Material 3 Expressive — engineering reference study

## Purpose

This document records an implementation-focused study of AndroidX Compose Material 3 Expressive to inform Motion Core and Motion UI. Material 3 Expressive is a reference implementation, not a visual or architectural dependency for Motion.

The source review is based on the AndroidX `androidx-main` branch as inspected on 2026-11-27. Internal implementation details can change; verify them against the Compose version selected by a consuming application before relying on them.

## Executive findings

1. Material 3 Expressive coordinates motion through a theme-level `MotionScheme`, rather than requiring every component to invent unrelated animation specifications.
2. Components combine standard Compose primitives with component-specific state machines, geometry, layout, semantics, and interaction sources. There is no separate Material animation engine.
3. Expressiveness is often produced by coordinated changes in shape, size, spacing, color, and content—not by adding a decorative animation to an otherwise static component.
4. Accessibility and layout constraints are part of the component implementation, not a later polish pass.
5. Motion should reuse the platform and Material components where appropriate, while keeping its own reusable interaction contracts independent of Material's visual language.

## 1. Motion scheme and token hierarchy

Material 3 exposes a theme-level `MotionScheme` with six specifications:

- `defaultSpatial`, `fastSpatial`, `slowSpatial`
- `defaultEffects`, `fastEffects`, `slowEffects`

The distinction is semantic:

- Spatial motion is for geometry changes such as position, size, and shape.
- Effects motion is for visual effects such as color and opacity.

The Expressive token source currently inspected defines:

| Token | Damping ratio | Stiffness |
|---|---:|---:|
| Default spatial | 0.8 | 380 |
| Fast spatial | 0.6 | 800 |
| Slow spatial | 0.8 | 200 |
| Default effects | 1.0 | 1600 |
| Fast effects | 1.0 | 3800 |
| Slow effects | 1.0 | 800 |

The standard scheme uses a different spatial damping/stiffness profile while retaining the same effects stiffness values. Components request semantic motion tokens rather than hard-coding every spring locally.

**Implication for Motion:** keep Motion's existing semantic spring families as the reusable behavior vocabulary. A future theme integration may let a consumer map Motion semantic roles to its own motion scheme, but Motion should not silently replace its agreed physics with Material's constants. Theme mapping belongs at the UI integration boundary, not in Core.

Sources:
- https://github.com/androidx/androidx/blob/androidx-main/compose/material3/material3/src/commonMain/kotlin/androidx/compose/material3/MotionScheme.kt
- https://github.com/androidx/androidx/blob/androidx-main/compose/material3/material3/src/commonMain/kotlin/androidx/compose/material3/tokens/ExpressiveMotionTokens.kt
- https://github.com/androidx/androidx/blob/androidx-main/compose/material3/material3/src/commonMain/kotlin/androidx/compose/material3/tokens/StandardMotionTokens.kt

## 2. ToggleButton: shape is a state, not decoration

The Expressive `ToggleButton` implementation:

- derives pressed state from a `MutableInteractionSource`;
- selects one of three shapes: resting, checked, or pressed;
- animates between compatible `CornerBasedShape` values through `rememberAnimatedShape`;
- animates border width with a spatial motion token and border color with an effects token;
- exposes checked state and checkbox role through semantics;
- uses `ToggleButtonSize` to coordinate height, padding, icon size, icon spacing, and typography.

A useful implementation boundary is explicit: if supplied shapes are compatible corner-based shapes, the component morphs their corner geometry; arbitrary shapes fall back to a state-based shape switch rather than pretending all shapes can be interpolated safely.

**Implication for Motion:** shape morphing needs a defined compatibility contract. Do not expose a generic “morph any Shape” promise unless the geometry representation supports it. Motion's morph primitives should own interpolation mechanics; the component should own which semantic states and shape endpoints are valid.

Source:
- https://github.com/androidx/androidx/blob/androidx-main/compose/material3/material3/src/commonMain/kotlin/androidx/compose/material3/ToggleButton.kt

## 3. ButtonGroup: interaction-driven layout negotiation

Material's `ButtonGroup` is more than a Row with animated buttons:

- each item exposes a `MutableInteractionSource`;
- a modifier node observes Press, Release, and Cancel interactions;
- an `Animatable` tracks the pressed item's expansion;
- the layout measure policy distributes the extra width and compresses neighboring items;
- compression limits protect content padding;
- items that do not fit move into an overflow menu;
- menu state is saveable, and item contracts provide both inline and overflow content.

One notable detail: the release path can wait until the expansion has crossed a threshold before animating back. This coordinates the neighboring items' compression/recovery rather than immediately reversing at the first release event.

**Implication for Motion:** this is a strong reference for separating interaction observation from layout measurement. If Motion later needs a reusable elastic group-layout primitive, it should expose the generic allocation/interaction behavior—not duplicate Material's ButtonGroup API or menu policy. Overflow content and menu semantics belong to the consuming component.

Source:
- https://github.com/androidx/androidx/blob/androidx-main/compose/material3/material3/src/commonMain/kotlin/androidx/compose/material3/ButtonGroup.kt

## 4. FloatingToolbar: a complete interaction contract

The Expressive floating toolbar coordinates:

- caller-controlled expanded state;
- optional scroll behavior and nested-scroll integration;
- animated reveal/conceal of leading and trailing content;
- animated elevation;
- a layout whose focusability follows the visible/hidden state;
- custom accessibility actions to expand and collapse;
- a saved accessibility override state;
- a policy that keeps the toolbar expanded when touch exploration is active;
- scroll behavior disabled while touch exploration is active.

This is an important lesson: collapsing a control is not merely a visual operation. It changes visibility, focus eligibility, available actions, and sometimes scroll policy. Accessibility settings can override the normal visual state machine.

**Implication for Motion:** any future toolbar contract needs explicit rules for semantic visibility, focus, keyboard traversal, touch exploration, and scroll interaction. It must not treat a visually hidden action as automatically inaccessible or assume that visual collapse is always permitted.

Source:
- https://github.com/androidx/androidx/blob/androidx-main/compose/material3/material3/src/commonMain/kotlin/androidx/compose/material3/FloatingToolbar.kt

## 5. LoadingIndicator: morph geometry with explicit bounds

Material's expressive loading indicator uses `RoundedPolygon` and `Morph` rather than attempting to interpolate arbitrary Compose `Shape` objects. It normalizes polygon endpoints, builds an explicit morph sequence, and calculates a scale factor using maximum rotational bounds so that rotating shapes do not clip.

Its indeterminate variant coordinates separate animated values for morph progress and rotation. Its determinate variant maps externally supplied progress to the shape sequence and exposes progress semantics.

**Implication for Motion:** reusable morph geometry should be represented by an interpolation-friendly model with documented endpoint constraints. Geometry normalization and bounds calculation are first-class concerns. The component—not the generic morph engine—owns the sequence and meaning of progress.

Source:
- https://github.com/androidx/androidx/blob/androidx-main/compose/material3/material3/src/commonMain/kotlin/androidx/compose/material3/LoadingIndicator.kt

## 6. Comparison with the MOT0.3B toolbar spike

The current Motion Lab spike already explores several ideas that overlap with Material's engineering concerns:

| Concern | Material 3 Expressive | Motion MOT0.3B spike |
|---|---|---|
| State ownership | Component state plus caller-controlled parameters | Host pager is source of truth; stable tab IDs |
| Progress | Component-specific state and transitions | Explicit from/to IDs and bounded host fraction |
| Geometry | Component layout and shape-specific animation | Measured variable-width tab bounds and pure geometry helpers |
| Gestures | Nested scroll and interaction sources | Long-press drag selection, edge auto-scroll, cancellation |
| Accessibility | Touch-exploration-aware expansion, custom actions, focus eligibility | Roving tab focus, single contextual-action target, keyboard/RTL coverage |
| Visual identity | Material tokens and Material shapes | Separate Expressive-oriented and app-defined profiles |
| Motion | Theme motion tokens | Motion semantic spring specs; host-driven progress is seeked/interpolated |

The spike's linear interpolation of indicator bounds against host pager progress is not inherently in conflict with Motion's spring principles: it is a seeked representation of an externally controlled progress fraction. The host pager owns the physical transition. Adding a second spring on top of that fraction would risk lagging or double-easing the indicator.

The current spike also uses Motion specs for detachment and connector spacing, and snaps those properties when the Android animator setting is disabled. The system animator preference is currently read as a plain value during composition; live changes while the screen remains open still need verification.

Reference implementation:
- `motion-lab/src/main/kotlin/io/github/information89/motion/lab/ToolbarPrototype.kt`
- `motion-lab/src/main/kotlin/io/github/information89/motion/lab/ToolbarGeometry.kt`
- `docs/MOT0.3B_MANUAL_VALIDATION.md`

## 7. Recommendations for Motion

### Adopt as engineering principles

1. Use semantic motion roles and shared specifications; avoid unexplained per-component spring constants.
2. Separate spatial movement from effects such as color and opacity.
3. Make interaction sources and state transitions explicit.
4. Keep geometry calculation independently testable where practical.
5. Define morph endpoint compatibility and clipping/bounds behavior.
6. Treat accessibility, focus, keyboard behavior, and touch exploration as part of the interaction contract.
7. Treat overflow, scroll coordination, and layout compression as component-level policies built on reusable lower-level mechanics.
8. Preserve one source of truth for navigation/selection; visual progress must not accidentally commit domain state.
9. Test interruption and retargeting as normal interaction, not exceptional edge cases.
10. Keep reduced-motion behavior functionally equivalent: remove or simplify motion without removing access to actions or changing final state.

### Do not copy

- Material's visual tokens, colors, shapes, typography, or component API as Motion defaults.
- Material-specific menu and overflow policies into Motion Core.
- Material's component-specific animation constants as replacements for Motion's agreed physics.
- A duplicate animation engine or duplicate implementations of standard Compose/Material controls.
- A dependency on Material 3 from Motion Core.

### Suggested next research targets

1. `MaterialShapes`, `RoundedPolygon`, and `Morph`: endpoint compatibility, path interpolation, and shape normalization.
2. Expressive `SplitButton` and `ButtonGroup`: how action hierarchy and overflow are modeled.
3. Expressive list items: segment geometry, interactive semantics, and adaptive height.
4. Flexible top/bottom app bars: scroll state, content collapse, and large-screen behavior.
5. MotionScheme consumption in components: confirm which APIs use spatial versus effects tokens and how reduced motion is applied.

## Verification boundary

This is a source-code and documentation review only. No Motion implementation was changed as a result of this study, no Android build was run, no manual device validation was performed, and no CI workflow was triggered.


## 8. MaterialShapes and the geometry contract behind Morph

Material 3's expressive shape catalog is built on `RoundedPolygon` rather than arbitrary Compose `Path` values. The catalog lazily constructs and normalizes its predefined polygons, and exposes conversion helpers to Compose paths and shapes.

The underlying AndroidX Graphics Shapes `Morph` implementation documents why this restriction matters:

- arbitrary paths can contain multiple disconnected contours and do not have an obvious point-to-point correspondence;
- rounded polygons provide a contiguous perimeter represented by cubic curves;
- the morph algorithm matches curves using proximity, vertex distance, and concavity;
- when the curve counts or placements differ, curves are split to create corresponding pairs;
- after the correspondence is prepared, each frame interpolates the paired cubic control points.

The implementation also exposes two evaluation paths:
- `asCubics(progress)` creates a new list and cubic objects;
- `forEachCubic(progress, mutableCubic, callback)` reuses a mutable cubic and is documented as faster with less allocation.

It also provides maximum rotational bounds, not just static bounds, for shapes that rotate inside a fixed container.

**Implication for Motion:** if Motion's morph system needs expressive shape transitions, use an explicit geometry model and establish correspondence once when endpoints are registered. Avoid rebuilding curve correspondence on every frame. Prefer reusable buffers/path objects in hot rendering paths, and include bounds/rotation in layout validation. Do not promise arbitrary `Path`-to-`Path` morphing without a defined contour and correspondence model.

Sources:
- https://github.com/androidx/androidx/blob/androidx-main/compose/material3/material3/src/commonMain/kotlin/androidx/compose/material3/MaterialShapes.kt
- https://github.com/androidx/androidx/blob/androidx-main/graphics/graphics-shapes/src/commonMain/kotlin/androidx/graphics/shapes/Morph.kt
- https://github.com/androidx/androidx/blob/androidx-main/graphics/graphics-shapes/src/commonMain/kotlin/androidx/graphics/shapes/RoundedPolygon.kt


## 9. SplitButton: two independent actions with shared geometry

The expressive split button is composed from two actual button controls, not one compound click target. The leading button owns the primary action; the trailing button owns a contextually related secondary action. Each has its own interaction source and semantics/role, while `SplitButtonLayout` supplies their shared geometry.

The custom measure policy deliberately measures the trailing action first: it reserves the trailing button's intrinsic width plus spacing, then gives the remaining width to the leading action. Both are constrained to a common visual height. The parent enforces the minimum interactive size once; the child buttons temporarily suppress their own minimum-size enforcement to avoid doubled internal padding. Relative placement preserves layout-direction behavior.

Each button derives pressed state from its interaction source. The trailing checked variant also accepts externally controlled checked state. Shape selection follows pressed > checked > resting, and compatible rounded/corner-based shapes are animated; unsupported shape types remain discrete rather than being falsely interpolated. The API intentionally leaves the menu/dropdown behavior to the consumer.

**Implication for Motion:** preserve action boundaries and independent semantics even when controls share a visual container. Shared layout should coordinate sizing and spacing without merging action ownership. Measure priority and min-touch-target enforcement should be explicit and tested under constrained widths and RTL. Do not bake a dropdown/menu policy into a generic split-control primitive.

Source:
- https://github.com/androidx/androidx/blob/androidx-main/compose/material3/material3/src/commonMain/kotlin/androidx/compose/material3/SplitButton.kt

## 10. Expressive ListItem: interaction state drives shape, color, elevation, and measurement

Interactive list items observe pressed, focused, hovered, and dragged interactions through a resolved `MutableInteractionSource`. The selected state is supplied separately by the caller. Shape selection is delegated to `ListItemShapes.shapeForInteraction`; colors for the container and each content slot are animated from a compact state tuple (enabled, selected, dragged). Shape uses a fast spatial token, while colors use a default effects token. Drag elevation is separately animated with a spatial token.

The implementation merges descendant semantics at the item boundary, applies minimum interactive size only for interactive items, and uses combined click/long-click handling with a shape-aware focus ring. Its content is not a single Row: it is a multi-content custom Layout with independent leading, trailing, overline, supporting, and main-content slots. The measure policy accounts for directional horizontal padding, vertical padding, available width, and the dimensions of leading/trailing content before placing the text column. This allows one-, two-, and three-line item variants to share a consistent layout contract without hard-coding one fixed row height.

**Implication for Motion:** a component state model should distinguish domain selection from transient interaction state. Animate only the visual properties that need transition, with semantic motion roles per property. For reusable list/surface primitives, slot-aware measurement and text-driven height are more robust than fixed-height templates. Keep semantics merged at the correct interaction boundary and preserve focus indication when shape changes.

Source:
- https://github.com/androidx/androidx/blob/androidx-main/compose/material3/material3/src/commonMain/kotlin/androidx/compose/material3/ListItem.kt

## 11. Flexible App Bars: scroll is a continuous external input, not a second animation

The flexible medium/large app bars are implemented as a two-row layout. The compact row owns navigation/actions and the collapsed title; the lower row owns the expanded title/subtitle. Both are driven by the same `TopAppBarState` through a `TopAppBarScrollBehavior` and nested-scroll connection. The expanded/collapsed heights are validated, and the lower row's measured size updates the scroll state's height-offset limit.

A single collapsed fraction coordinates several outputs: container color interpolation, compact-title alpha, expanded-title alpha, and which title's semantics are exposed. The title semantic handoff occurs around the midpoint so assistive technology does not encounter duplicate title nodes during the visual crossfade. The app bar also supports direct vertical dragging and settling/fling behavior. The exit-until-collapsed behavior consumes vertical scroll in pre/post scroll phases, respects `canScroll`, and can snap or decay based on configured animation specs.

This architecture keeps the scrollable content as the source of continuous progress. The app bar maps that progress into height, color, and title presentation; it does not independently spring-animate a second progress value. The layout also clips each row and applies window insets at the appropriate level to avoid expanded content drawing into system-bar regions.

**Implication for Motion:** when a component follows externally owned scroll/navigation progress, derive all visual properties from that one normalized progress value. Reserve springs for independently initiated transitions such as settling after a drag. For collapsing content, accessibility semantics must track the currently meaningful representation, not merely opacity. Treat insets, clipping, dynamic measured bounds, and scroll-consumption policy as part of the component contract.

Source:
- https://github.com/androidx/androidx/blob/androidx-main/compose/material3/material3/src/commonMain/kotlin/androidx/compose/material3/AppBar.kt

## 12. Refined decisions for Motion Core and Motion UI

The additional source review reinforces these boundaries:

1. **Core owns mechanics, UI owns policy.** Core may provide interpolation, interaction observation helpers, progress mapping, and reusable geometry. Components own action meaning, overflow/menu behavior, selection policy, and accessibility labels.
2. **Keep transient and durable state separate.** Pressed/focused/hovered/dragged are interaction signals; selected/checked and navigation destination belong to caller/domain state.
3. **Preserve independent actions inside compound layouts.** A shared visual surface must not collapse two buttons into one accessibility or input target.
4. **Prefer constraint-aware layouts.** Reserve critical controls first, distribute remaining space deliberately, and define behavior for narrow widths, font scaling, and RTL.
5. **Use one authoritative progress source.** Scroll- or pager-driven components should map host progress to geometry/color/alpha without layering a second animation over it. Spring/decay is appropriate for the component's own settle phase.
6. **Coordinate semantics with visual transitions.** Crossfades between duplicate representations need a clear semantic handoff; invisible/transparent content must not remain ambiguously exposed.
7. **Keep shape interpolation capability explicit.** Morph only supported geometry families; otherwise use a deterministic state change or crossfade.
8. **Accessibility settings can constrain motion policy.** Touch exploration, focus, and reduced motion must be inputs to component behavior where relevant, not post-processing flags.

These are reference-derived engineering principles, not a request to reproduce Material components or their appearance. The Motion toolbar remains its own specialized component; this study informs its engineering quality without replacing its distinctive behavior.

## 13. Sources reviewed in this pass

- `SplitButton.kt` — independent primary/secondary actions, intrinsic-width-first measurement, shape states, checked semantics.
- `ListItem.kt` — interaction state collection, state-driven shape/color/elevation, merged semantics, multi-slot measurement.
- `AppBar.kt` — flexible two-row app bars, shared collapsed fraction, nested-scroll/drag settling, semantic title handoff.

## Verification boundary (updated)

This remains a source-code and documentation review only. No Motion production implementation was changed, no Android build or device validation was run, no CI workflow was triggered, and no branch was merged. Source observations refer to AndroidX `androidx-main` as inspected on 2026-11-27 and should be rechecked against the consuming Compose version.
