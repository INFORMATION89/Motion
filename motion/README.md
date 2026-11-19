# :motion

A small motion framework for Jetpack Compose, written for Solstice but usable anywhere (it depends
only on Compose). Licensed under the Apache License 2.0 (see `LICENSE`), unlike the rest of Solstice
(LGPL-3.0).

- **Springs written the iOS way:** `MotionSpring(response, damping)`; tokens in `Motion`.
- **Physics:** rubber-banding, fling projection and target choice (`MotionPhysics`).
- **`MotionValue`:** one number per interaction (0 = closed, 1 = open) that a finger drags, a
  release flings with its velocity, and a new touch catches mid-flight.

Rules and the full design: `docs/MOTION.md` and "Motion framework" in `docs/PLAN.md`.
