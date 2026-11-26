# Motion principles

This document defines Motion's reusable interaction principles. They apply across consuming applications and do not prescribe any consumer's visual identity, screen composition, or product-specific choreography. Motion is the behavior layer, not a general-purpose UI component kit.

The reusable base tokens live in `:motion` (`io.github.information89.motion.Motion`, expressed as response and damping). A consuming application may map those tokens through its own app-side scheme; consumer-specific mappings stay in the consuming app. App-side names in examples are illustrative, not dependencies or requirements for Motion.

These principles guide implementation and review. Before changing user-visible motion behavior or adding a new public contract, record the proposal and obtain the owner's agreement.

## Pillars

### A. Fluid spring physics (no linear or eased curves)

Motion is interruptible and driven by velocity, not by duration. Use `spring()`, not `tween()`.

**Things morph, they never pop** (the user, 2026-11-21: very important). Nothing new appears out of
nowhere: a sheet, card, menu or window grows out of the element that caused it, and that element *is*
what transforms and expands (as iOS buttons do), then folds back into it. No fade-in from nothing, no
replacing one thing with another.

**Scope of Motion:** it is the reusable behavior layer beneath interactions across consuming applications: button feedback, lists, menus, cards, sheets, page changes, drags, swipes, long presses, and related motion feedback. Motion should own generic mechanics; each app still owns its components, layout, content, styling, navigation policy, and domain behavior. A consumer-specific need is not automatically a Motion feature: extract it only when the behavior is generic and independent of the app.

| Use | Spring | In code |
|---|---|---|
| Responsive controls (buttons, toggles, sliders) | `dampingRatio = Spring.DampingRatioNoBouncy`, `stiffness = Spring.StiffnessMediumLow` | `AppMotion.control` |
| Morphing containers (menus, sheets, pill-style buttons) | `dampingRatio = 0.78f`, `stiffness = 380f` (subtle elasticity) | `AppMotion.container` |
| Inner content scale-in | `dampingRatio = 0.85f`, `stiffness = 400f` | `AppMotion.content` |
| Settling after a drag (no bounce) | `dampingRatio = 1f`, `stiffness = Spring.StiffnessMediumLow` | `AppMotion.settle` |
| Dismissing a container back into its source | `dampingRatio = 1f`, `stiffness = 260f` | `AppMotion.dismiss` |
| Small controls only (switch thumbs, toggles, icon changes; Material's fast springs) | 0.2 s / 0.9 (`stiffness ≈ 987`) | `Motion.snappy` (added 2026-11-19) |

Values live in the `:motion` module (`io.github.information89.motion.Motion`, written as response +
damping). Material components get them through `AppMotionScheme` (fast → `snappy`, default →
`container`, slow → `dismiss`; effects: fast → `snappy`, default → `settle`, slow → `dismiss`).

The exception is a shared transition that's seeked. There, the parts follow the fraction
linearly, and the spring is in the `Animatable` that sets the fraction, so only one physical
curve applies.

### B. Gesture-driven elastic springs (Animatable plus velocity)

- Pair an `Animatable` with `pointerInput` or `draggable`. The drag moves it directly, and the
  release velocity becomes the spring's `initialVelocity`. Don't use `animateFloatAsState` for
  gesture-driven motion.
- Catch things mid-flight: a new drag stops the spring where it is and carries on from there.
- **Animations never block input** (added by the user 2026-11-21): nothing waits for an animation to
  finish. A tap on anything (a place card while a tab change plays, a button while a sheet settles)
  works at once: the animation is retargeted, finished instantly, or keeps running underneath, but the
  touch is never swallowed. This applies to every animation, through `:motion`. **Exception, agreed
  2026-11-21:** page changes (swipes and tab taps) are the pager really scrolling, so a tap while the
  pages still move first stops them (the user chose one springy page motion over this rule).
- Pick the target from momentum: project where a fling would come to rest (`AppMotion.project`).
- Over-dragging rubber-bands, like iOS, and never stops dead (`AppMotion.rubberBand`, coefficient 0.55).

### C. Continuous container morphing (`:motion` `Morph`; SharedTransitionLayout before M3)

- Buttons, pills and floating buttons morph into the sheets and menus they open, and back. The
  containers don't crossfade.
- Use `SharedTransitionLayout` with `sharedBounds` and a custom `BoundsTransform`. The curve runs
  from critically damped to lightly underdamped.
- Text that travels (a button's label becoming a heading) shares bounds with scale-to-bounds.
- **The button and the card it becomes share one material** (the user, 2026-11-21). A morphing
  button and its card use the same background material (the same glass, or the same fill). The
  morph changes the shape, size and content, never the material, so nothing crossfades and the card
  reads as the button itself grown. Different materials at the two ends (an outlined button opening
  into a glass card, say) are not allowed.
- **The button itself becomes the card** (agreed 2026-11-21, a style morph): `Morph` takes the
  source's and target's `MorphStyle` (fill, border, shadow, material amount) and the source's own
  content. Its first frame is exactly the button, `Modifier.morphSource` hides the real button in that
  same frame, and every property interpolates to the card's; materials never crossfade. First used
  by the moon pill.

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
- **Search sheet** (2026-11-19): `AppSheetState` (presence + detent), see PLAN.md log.
- **Postcard window, Places drag, toolbar** (2026-11-19): on tokens; the window closes with a swipe
  down. See PLAN.md log.
- **Everything else** (2026-11-19): on tokens. Exceptions: the sticker peel, the Forecast develop
  and the moon's drift (timed effects / a slow drift, not gestures). The moon card keeps its own
  MotionValue (a full-height sheet whose pull plays the morph backwards).
- **Motion lab** (M5, 2026-11-19): Settings → Debug → Motion lab; slow motion is `MotionTime.scale`.
- **Haptics** (2026-11-21, F1): `:motion` `Haptic` tokens via `haptics().perform(...)`; no direct
  vibrator or `HapticFeedbackType` calls in the app, and no new haptics without agreeing them.
- **Page changes** (2026-11-21, revised): `:motion` `PageSpring` (`pageSpringFling`, `springToPage`,
  `PageOvershoot`): swipes and tab taps spring the pages themselves on `Motion.momentum`, bouncing past
  the first and last page. `PageTransition` (the drawn picture slide) is gone.
- **README** (2026-11-19): `motion/README.md` (pieces, tokens, rules, examples).
- **Next:** refining rounds (CONTINUE.md section 4).

## Framework

The plan to build these rules once, as a reusable `:motion` module, is in
[PLAN.md](PLAN.md#motion-framework-proposed-2026-11-19-to-agree-before-building) (proposed 2026-11-19).
