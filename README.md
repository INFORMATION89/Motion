# Motion

A motion framework for Jetpack Compose that makes interactions feel like iOS: springs written as
*response* and *damping*, gestures that hand their velocity to the animation, rubber-banding, UIKit's
release rule, and surfaces that morph out of the element that opened them. It depends only on
Compose (`animation`, `foundation`). Licensed under the Apache License 2.0 (see `LICENSE`).

Made for [Solstice](https://github.com/INFORMATION89/Solstice), where it started as the `:motion`
module, and meant for any Compose app.

## In this repo

| | |
|---|---|
| `motion/` | The library (Gradle module `:motion`, package `io.github.information89.motion`). What each piece does: [motion/README.md](motion/README.md). |
| `lab/` | Motion Lab, a Material You Expressive app to try every piece. Moving in from Solstice; not built yet (phase 2 of [docs/MOTION_REPO_PLAN.md](docs/MOTION_REPO_PLAN.md)). |
| `docs/` | The motion rules ([MOTION.md](docs/MOTION.md)), the morph study, the lab plan, and the plan for this repo. |

Build and test: `./gradlew :motion:testDebugUnitTest :motion:assembleRelease` (JDK 21, Android SDK 37).

## Using it in an app

It isn't published. Build it from source with a Gradle composite build, as Solstice does:

1. Add this repo as a git submodule, e.g. at `motion/`:
   `git submodule add https://github.com/INFORMATION89/Motion.git motion`
2. In `settings.gradle.kts`:
   ```kotlin
   includeBuild("motion") {
       dependencySubstitution {
           substitute(module("io.github.information89:motion")).using(project(":motion"))
       }
   }
   ```
3. In your module: `implementation("io.github.information89:motion")`.

Clone with `git clone --recurse-submodules`, or run `git submodule update --init` in an existing
clone. In CI, check out submodules (`actions/checkout` with `submodules: true`).

## How a change reaches Solstice

1. Change Motion here (code, tests, lab), push; CI goes green.
2. In Solstice, move the submodule to that commit (`git -C motion pull` or
   `git submodule update --remote motion`), build and check, and commit the new pointer:
   "Bump Motion to abc1234: what changed".
3. To try a change inside Solstice first, edit Solstice's `motion/` folder (it's a checkout of this
   repo), test it in Solstice, then commit and push from inside `motion/` and bump the pointer.

Motion never imports app code: anything an app needs from Motion lands here first.
