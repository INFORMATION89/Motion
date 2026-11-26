# Motion

An independent motion and interaction framework for Jetpack Compose apps.

Motion provides reusable motion behavior: spring-driven state, interruptible animations, gesture-to-animation handoff, elastic boundaries, morphing surfaces, and interaction primitives. It is designed to be used by different apps without depending on their screens, themes, or business logic.

Motion is organized around two layers: **Motion Core** (`:motion`) owns reusable motion and interaction behavior; **Motion UI** (`:motion-ui`) is an optional component layer for configurable page frames, cards, sheets, header actions, and message surfaces. Motion UI builds on Core, not replace Material 3 or specialized UI libraries. The initial MOT0.3A theme/token foundation is implemented; component surfaces are delivered in subsequent stages.

## What Motion covers

- **Motion behavior** — spring tokens, retargeting, interruption, and velocity continuity.
- **Gesture mechanics** — drag, catch, release projection, velocity handoff, and rubber-banding.
- **Reusable interactions** — morphing surfaces, page movement, dials, reveal choreography, and related interaction state.
- **Motion-related feedback** — semantic haptic primitives, ambient motion scheduling, and reduced-motion support.

See [the framework overview](docs/FRAMEWORK_OVERVIEW.md) for the responsibilities of Motion Core, Motion UI, Compose, other UI libraries, and app code.

## Platform support

The current library is an Android library built on Jetpack Compose. It targets Android 10 (API 29) and uses compile SDK 37 with JDK 21. iOS and desktop targets (Windows, macOS, and Linux) are not supported by the current artifact.

See [platform support and integration](docs/PLATFORM_SUPPORT.md) for the working Android setup and the requirements for future platform targets.

## Use in an Android app

Motion is not currently published to Maven. It is consumed from source through a Gradle composite build.

Add the repository as a submodule:

```bash
git submodule add https://github.com/INFORMATION89/Motion-UI.git motion
```

In the consuming project's `settings.gradle.kts`:

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

For a fresh clone, include submodules:

```bash
git clone --recurse-submodules <app-repository-url>
```

For an existing clone:

```bash
git submodule update --init --recursive
```

Pin the submodule to a tested Motion commit. Do not move a consuming app to an unverified branch tip.

## Build and test

Run from the repository root:

```bash
./gradlew spotlessCheck
./gradlew :motion:testDebugUnitTest
./gradlew :motion:assembleRelease
./gradlew :motion-ui:testDebugUnitTest
./gradlew :motion-ui:assembleRelease
./gradlew :motion-lab:assembleDebug
```

Install the sample on a connected Android device or emulator with `./gradlew :motion-lab:installDebug`.

On Windows, use `gradlew.bat` instead of `./gradlew`.

## Documentation

- [Framework scope and responsibilities](docs/FRAMEWORK_OVERVIEW.md)
- [Platform support and integration](docs/PLATFORM_SUPPORT.md)
- [AI guide for planning Motion adoption in Solstice](docs/AI_SOLSTICE_ADOPTION.md)
- [Library API and usage](motion/README.md)
- [Motion principles](docs/MOTION.md)
- [Morph design study](docs/MORPH_STUDY.md)
- [Project rules](docs/RULES.md)
- [Repository instructions for AI agents](AGENTS.md)
- [AI usage and cost policy](docs/AI_USAGE_POLICY.md)
- [MOT0.2 standalone Motion Lab plan](docs/MOT0.2_PLAN.md)
- [MOT0.3A Motion UI foundation](docs/MOT0.3A_PLAN.md)
- [Motion UI theme and token API](motion-ui/README.md)
- [Motion Lab app and build guide](motion-lab/README.md)

## Project status

The `:motion` Android library, optional `:motion-ui` Android library, and standalone `:motion-lab` sample app are Gradle modules. Motion Lab currently includes a generic gallery organized into Components and Interactions tabs, automatic system light/dark appearance, and spring/release, morph, page, dial, edge-blur, and semantic haptics demonstrations. Morph and page movement have opt-in comparisons driven by shared input. The haptics page exposes the existing semantic tokens and a temporary intensity control. CI passed on the final PR head; emulator/physical-device behavior, TalkBack, contrast, and real vibration remain unverified. The `:motion-ui` module currently provides an app-configurable Material 3 theme and semantic shape, spacing, and surface-material tokens. The old `lab/` directory remains an unbuilt Solstice-era prototype and is not part of the new app.


## License

Motion is licensed under the [Apache License 2.0](LICENSE).
