# Platform support and integration

## Current support

The current `:motion` artifact is an Android library built with the Android Gradle plugin and Jetpack Compose.

| Platform | Current status | What that means |
|---|---|---|
| Android | **Supported** | The current library can be consumed by Android apps using Jetpack Compose. Minimum Android API: 29. |
| Windows desktop | Not supported yet | The current Android artifact cannot be added to a Windows desktop target. |
| macOS desktop | Not supported yet | The current Android artifact cannot be added to a macOS desktop target. |
| Linux desktop | Not supported yet | The current Android artifact cannot be added to a Linux desktop target. |
| iOS | Not supported yet | The current project is not a Kotlin Multiplatform library and does not publish an iOS framework. |

The build uses compile SDK 37, Android Build Tools 37, and JDK 21. The module applies the Android library plugin and uses Android-specific APIs, so source compatibility with Compose Multiplatform must not be assumed.

## Android: supported integration

### Requirements

- Android 10 / API 29 or newer.
- Android SDK and Build Tools 37.
- JDK 21.
- A Jetpack Compose app.

### Add Motion as a submodule

From the consuming repository root:

```bash
git submodule add https://github.com/INFORMATION89/Motion-UI.git motion
```

For a fresh clone of the app:

```bash
git clone --recurse-submodules <app-repository-url>
```

For an existing clone:

```bash
git submodule update --init --recursive
```

### Include the composite build

In the consuming project's `settings.gradle.kts`:

```kotlin
includeBuild("motion") {
    dependencySubstitution {
        substitute(module("io.github.information89:motion"))
            .using(project(":motion"))
    }
}
```

In the app or library module's `build.gradle.kts`:

```kotlin
dependencies {
    implementation("io.github.information89:motion")
}
```

The Motion repository is not currently published to Maven. The Gradle composite build substitutes the dependency coordinates with the local `:motion` project.

### Pin and update the submodule deliberately

A consuming app should point to a tested Motion commit, not an unreviewed moving branch. A deterministic update looks like this:

```bash
git -C motion fetch origin
git -C motion checkout <tested-motion-commit>
git add motion
git commit -m "chore: update Motion to <short-sha>"
```

The app's CI must check out submodules. For GitHub Actions:

```yaml
- uses: actions/checkout@v4
  with:
    submodules: recursive
```

Consumer repositories can integrate Motion through Gradle composite-build substitution and a pinned source dependency. New integrations should use the canonical `Motion-UI` repository URL shown above.

### Windows development note

A Windows developer can build the current Android target using the Windows Gradle wrapper and an Android SDK:

```powershell
.\gradlew.bat spotlessCheck
.\gradlew.bat :motion:testDebugUnitTest
.\gradlew.bat :motion:assembleRelease
```

This is Android development performed on Windows. It does **not** mean Motion currently supports a Windows desktop UI target.

## Desktop: Windows, macOS, and Linux

Desktop support is a future platform target, not an installation option today. Before enabling it:

1. Decide whether to convert the project to Kotlin Multiplatform with Compose Multiplatform targets, while preserving the existing Android artifact and its public API.
2. Separate platform-independent motion contracts from Android-only implementations. Review haptics, blur, system animation settings, pointer input, and lifecycle/visibility handling individually.
3. Put platform-specific capabilities behind narrow adapters with explicit fallbacks. A desktop target must not silently pretend that unavailable haptics or Android system settings exist.
4. Add desktop source sets and focused tests for shared motion logic, followed by build/test jobs on Windows, macOS, and Linux where the platform implementation requires them.
5. Validate mouse, touch, trackpad, keyboard, window resizing, and accessibility behavior instead of assuming mobile gesture semantics transfer unchanged.

Do not add a desktop target until the source-set design, dependency compatibility, API behavior, and CI matrix are agreed.

## iOS

iOS support also requires a deliberate multiplatform design. The current Android library cannot be linked into an iOS app as-is.

A future proposal must decide:

- Whether Compose Multiplatform is the UI/rendering layer for the relevant app surfaces, or whether Motion exposes a smaller Kotlin API to a native SwiftUI/UIKit app.
- Which motion state, spring, gesture, and geometry contracts can be shared.
- How haptics, system animation preferences, lifecycle, touch/gesture ownership, and accessibility map to iOS APIs.
- Whether platform-specific adapters are needed and how they are tested on simulator and physical hardware.
- How the API is exposed and versioned for Kotlin/Swift consumers.

Do not promise native SwiftUI compatibility or add an iOS target until that integration model is selected and verified.

## Platform expansion acceptance

A platform is supported only when all of the following exist:

- A declared Gradle target and compatible dependencies.
- A documented public integration path.
- Platform implementations or explicit unsupported-capability fallbacks.
- Unit tests for shared contracts and platform-specific tests where behavior differs.
- CI that builds and tests the target.
- Real platform validation before a stable release; CI alone is not device validation.

Web is not currently a committed target. Treat it as a separate product/platform decision rather than assuming that desktop or iOS support includes it.
