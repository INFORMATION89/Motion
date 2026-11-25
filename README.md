# Motion

A motion framework for Android apps built with Jetpack Compose.

Motion provides spring-driven, gesture-aware interactions with an emphasis on continuity: animations can be interrupted, gestures hand their velocity to the next movement, and surfaces can morph between layout anchors instead of being replaced.

Motion is an independent library. It does not depend on Solstice or on any consuming application.

## Features

- **Spring motion** — reusable motion tokens expressed as response and damping.
- **Gesture-driven state** — dragging, velocity handoff, projected release targets, multiple resting points, and elastic boundaries.
- **Morphing surfaces** — animate a surface between registered layout anchors while preserving its geometry and visual style.
- **Interaction primitives** — spring-driven pager movement, dials, haptic feedback, ambient motion, and edge blur.

## Platform and status

The current library targets Android with Jetpack Compose.

| Requirement | Version |
|---|---:|
| Minimum Android version | Android 10 (API 29) |
| Compile SDK | 37 |
| JDK | 21 |

Motion is under active development and is not currently published to a Maven repository. The repository builds the `:motion` Android library; the prototype sources in `lab/` are not currently included as a Gradle module.

## Use in an Android app

Motion is consumed from source through a Gradle composite build.

Add the repository as a submodule:

```bash
git submodule add https://github.com/INFORMATION89/Motion-UI.git motion
```

In the consuming project's `settings.gradle.kts`, include the build and substitute the published coordinates with the local project:

```kotlin
includeBuild("motion") {
    dependencySubstitution {
        substitute(module("io.github.information89:motion"))
            .using(project(":motion"))
    }
}
```

Then add the dependency to the app or library module:

```kotlin
dependencies {
    implementation("io.github.information89:motion")
}
```

Clone the consuming project with its submodules:

```bash
git clone --recurse-submodules <app-repository-url>
```

For an existing clone:

```bash
git submodule update --init --recursive
```

The consuming project should pin Motion to a tested commit and update the submodule pointer when adopting a newer version.

## Build and test

Run these commands from the repository root:

```bash
./gradlew spotlessCheck
./gradlew :motion:testDebugUnitTest
./gradlew :motion:assembleRelease
```

On Windows, use `gradlew.bat` instead of `./gradlew`.

## Documentation

- [Library API and usage](motion/README.md)
- [Motion principles](docs/MOTION.md)
- [Morph design study](docs/MORPH_STUDY.md)

## License

Motion is licensed under the [Apache License 2.0](LICENSE).
