# :motion

Motion is the reusable motion and interaction layer for Jetpack Compose apps. It provides spring-driven state, gesture mechanics, morphing behavior, and reusable interaction primitives without taking ownership of an app's visual design, screen structure, or domain logic.

It is an independent library, not a general-purpose UI component kit. Use Compose and your preferred UI/design-system libraries for layout and components, then compose them with Motion for movement and interaction behavior.

See the [framework overview](../docs/FRAMEWORK_OVERVIEW.md) for responsibility boundaries and the [platform guide](../docs/PLATFORM_SUPPORT.md) for current support. Today the artifact targets Android only.

Licensed under the Apache License 2.0 (see `LICENSE`).

## Install

Not published: build it from source as a Git submodule and Gradle composite build (see the [repo README](../README.md#use-in-an-android-app)). Coordinates `io.github.information89:motion`, package `io.github.information89.motion`.

## The pieces

| | What it is |
|---|---|
| `MotionSpring(response, damping)` | A spring expressed using perceptual response and damping. `spec()` returns a Compose `SpringSpec`; `fromCompose(dampingRatio, stiffness)` converts back. |
| `Motion` | Reusable motion tokens. Add a token only when the behavior is generic and its feel is agreed. |
| `MotionValue` | Stateful motion for an interaction: drag, release with velocity, catch, snapping, and rest points. |
| `rememberMotionValue(...)` | Remembers a `MotionValue`; rest points can change later. |
| `Modifier.motionDrag(value, orientation, range)` | A drag modifier that drives a `MotionValue`; the configured finger travel maps to a value delta. |
| `MotionPhysics` | Projection, release-target selection, resistance, rubber-banding, and flick thresholds. |
| `MorphHost`, `Modifier.morphAnchor`, `Morph` | A surface that morphs between registered layout anchors and follows a `MotionValue`. |
| `MorphStyle`, `Modifier.morphSource(...)` | Morph source/target visual properties such as fill, border, shadow, and source content. The app still owns its visual design. |
| `rememberMorphDrawn(key)` | Reports when a morph for a source key has drawn, so opening can begin from a valid source frame. |
| `rememberAmbientClock(fps, running)`, `rememberAmbientMotionAllowed()` | Schedules ambient animation conservatively and reports whether ambient motion should run. |
| `Dial`, `DialScale`, `DialLook`, `rememberDialState` | A reusable dial interaction with drag, projection, snapping, haptics, labels, and accessibility support. |
| `Modifier.edgeBlur(...)`, `EdgeBand` | Edge blur/fade helpers for content where the effect supports the interaction or motion presentation. |
| `Haptic`, `MotionHaptics`, `LocalHaptics`, `haptics()` | Semantic haptic feedback tokens and an abstraction over platform feedback. |
| `pageSpringFling`, `springToPage`, `PageOvershoot` | Spring-driven page movement and edge overshoot mechanics. |
| `Modifier.morphFrom(anchor)` | Moves an element within a `Morph` from a source anchor to its target location. |
| `Modifier.reveal(...)`, `Modifier.handOff(...)` | Choreographs content reveal and hand-off using a motion value. |
| `rememberReducedMotion()` | Reports the system reduced-motion/animation preference supported by the current platform implementation. |
| `MotionTime.scale` | Slows Motion tokens for deliberate inspection; restore it after debugging. |

## Tokens

| Token | Response / damping | For |
|---|---|---|
| `container` | ≈0.32 s / 0.78 | Morphing containers opening: sheets, menus, pills. A touch of bounce. |
| `dismiss` | ≈0.39 s / 1 | A container going back into its source. No bounce. |
| `control` | ≈0.31 s / 1 | Controls: buttons, toggles, sliders. |
| `content` | ≈0.31 s / 0.85 | Inner content scaling and fading in. |
| `settle` | ≈0.31 s / 1 | Settling after a drag or fling. |
| `snappy` | 0.2 s / 0.9 | Small controls: switch thumbs, presses, icon changes. |
| `playful` | ≈0.31 s / 0.5 | Expressive moments only: a pop, press return, or landing bump. |
| `wobble` | ≈0.31 s / 0.32 | Liquid effects only. |
| `gentle` | ≈0.44 s / 1 | Unhurried settling, such as a page swipe. |

## Rules

1. **Prefer springs and shared tokens.** Avoid local timing/easing values for ordinary interaction motion. Existing documented timed-effect exceptions remain explicit; do not expand them casually.
2. **Velocity carries over.** A release starts its spring with the gesture's velocity where the API supports it.
3. **Motion can be caught.** A new gesture should take over a running interaction from its current state.
4. **Choose release targets from momentum.** Use the projected landing and configured flick rules, not only the current position.
5. **Use elastic boundaries intentionally.** Over-drag resistance and release behavior should remain consistent.
6. **Use the appropriate token.** Opening and dismissing a container can have different motion character.
7. **Read motion values in layout/draw where possible.** Avoid triggering broad recomposition every animation frame.
8. **Keep appearance in the app.** Motion may interpolate a supplied style as part of a morph, but it does not define the application's theme or visual identity.

## Examples

A sheet driven by a motion value:

```kotlin
val open = rememberMotionValue(initial = 0f, overMin = 0.02f, overMax = 0.1f)
LaunchedEffect(Unit) { open.animateTo(1f) }
Box(
    Modifier
        .fillMaxSize()
        .motionDrag(open, Orientation.Vertical, range = 400.dp, reverseDirection = true)
        .graphicsLayer { translationY = (1f - open.value) * size.height }
)
```

A card that morphs from a pill:

```kotlin
val value = rememberMotionValue()
MorphHost {
    Button(onClick = { value.animateTo(1f) }, Modifier.morphAnchor("pill", cornerRadius = 100.dp)) {
        Text("Open")
    }
    Spacer(Modifier.fillMaxWidth().height(200.dp).morphAnchor("card", cornerRadius = 24.dp))
    Morph(from = "pill", to = "card", value = value, surface = Modifier.background(Color.White)) {
        Text("Grown from the pill", Modifier.reveal(value, 0.35f, 0.7f))
    }
}
```

For more examples and the current public API, inspect the source and tests. Do not assume an API is available on platforms other than Android.
