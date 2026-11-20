# Solstice motion

How Solstice moves. This is the user's reference (2026-11-19) and the rules every animation follows.
The values live in code in `app/src/main/kotlin/org/breezyweather/ui/expressive/components/SolsticeMotion.kt`.
Any new or changed motion is still proposed to the user and agreed before it's built.

## Pillars

### A. Fluid spring physics (no linear or eased curves)

Motion is interruptible and driven by velocity, not by duration. Use `spring()`, not `tween()`.

| Use | Spring | In code |
|---|---|---|
| Responsive controls (buttons, toggles, sliders) | `dampingRatio = Spring.DampingRatioNoBouncy`, `stiffness = Spring.StiffnessMediumLow` | `SolsticeMotion.control` |
| Morphing containers (menus, sheets, pill-style buttons) | `dampingRatio = 0.78f`, `stiffness = 380f` (subtle elasticity) | `SolsticeMotion.container` |
| Inner content scale-in | `dampingRatio = 0.85f`, `stiffness = 400f` | `SolsticeMotion.content` |
| Settling after a drag (no bounce) | `dampingRatio = 1f`, `stiffness = Spring.StiffnessMediumLow` | `SolsticeMotion.settle` |
| Dismissing a container back into its source | `dampingRatio = 1f`, `stiffness = 260f` | `SolsticeMotion.dismiss` |
| Small controls only (switch thumbs, toggles, icon changes; Material's fast springs) | 0.2 s / 0.9 (`stiffness ≈ 987`) | `Motion.snappy` (added 2026-11-19) |

Values live in the `:motion` module (`io.github.information89.motion.Motion`, written as response +
damping). Material components get them through `SolsticeMotionScheme` (fast → `snappy`, default →
`container`, slow → `dismiss`; effects: fast → `snappy`, default → `settle`, slow → `dismiss`).

The exception is a shared transition that's seeked. There, the parts follow the fraction
linearly, and the spring is in the `Animatable` that sets the fraction, so only one physical
curve applies.

### B. Gesture-driven elastic springs (Animatable plus velocity)

- Pair an `Animatable` with `pointerInput` or `draggable`. The drag moves it directly, and the
  release velocity becomes the spring's `initialVelocity`. Don't use `animateFloatAsState` for
  gesture-driven motion.
- Catch things mid-flight: a new drag stops the spring where it is and carries on from there.
- Pick the target from momentum: project where a fling would come to rest (`SolsticeMotion.project`).
- Over-dragging rubber-bands, like iOS, and never stops dead (`SolsticeMotion.rubberBand`, coefficient 0.55).

### C. Continuous container morphing (`:motion` `Morph`; SharedTransitionLayout before M3)

- Buttons, pills and floating buttons morph into the sheets and menus they open, and back. The
  containers don't crossfade.
- Use `SharedTransitionLayout` with `sharedBounds` and a custom `BoundsTransform`. The curve runs
  from critically damped to lightly underdamped.
- Text that travels (a button's label becoming a heading) shares bounds with scale-to-bounds.

### D. Choreographed content reveal

When a pill expands into a card, the inner content is delayed and scaled, so it doesn't look
squished while the container first stretches:

- **Container bounds:** start immediately (0 ms delay), lightly underdamped.
- **Inner content:** `fadeIn(tween(180, delayMillis = 60))` combined with
  `scaleIn(initialScale = 0.92f, animationSpec = spring(0.85f, 400f))`. Text and icons emerge as
  the glass reaches about 70 % of its target size.
- **Closing:** the content leaves first, then the container shrinks back.
- **When a gesture drives it:** the reveal is tied to the morph's position (from 35 % to 70 % of
  the way, 0.92 to 1 scale), so pulling scrubs it too.

## Also always

- **Never a plain fade-in:** things morph into other things.
- **Consistency:** the same values everywhere.

## Where it's applied

- **Moon card (2026-11-19):** the pill morphs into a full-height sheet.
  - It's driven by a gesture `Animatable` with velocity.
  - Pulling down scrubs it back into the pill, and pulling up past open rubber-bands.
  - The content is choreographed.
- **Search sheet** (2026-11-19): `SolsticeSheetState` (presence + detent), see PLAN.md log.
- **Postcard window, Places drag, toolbar** (2026-11-19): on tokens; the window closes with a swipe
  down. See PLAN.md log.
- **Everything else** (2026-11-19): on tokens. Exceptions: the sticker peel, the Forecast develop
  and the moon's drift (timed effects / a slow drift, not gestures). The moon card keeps its own
  MotionValue (a full-height sheet whose pull plays the morph backwards).
- **Motion lab** (M5, 2026-11-19): Settings → Debug → Motion lab; slow motion is `MotionTime.scale`.
- **README** (2026-11-19): `motion/README.md` (pieces, tokens, rules, examples).
- **Next:** refining rounds (CONTINUE.md section 4).

## Framework

The plan to build these rules once, as a reusable `:motion` module, is in
[PLAN.md](PLAN.md#motion-framework-proposed-2026-11-19-to-agree-before-building) (proposed 2026-11-19).
