# Continuous Integration

## Required workflow

The `Build` workflow runs two independent jobs in parallel for every push to `main` and every pull request:

- **Fast checks** runs Spotless, Core and Motion UI unit tests, Motion Lab unit tests, and release builds for the reusable libraries.
- **Compose UI tests** boots the API 35 Pixel 2 emulator and runs `:motion-lab:connectedDebugAndroidTest`. This task builds the debug app and instrumentation APK itself, so the fast job does not duplicate `:motion-lab:assembleDebug`.

Both jobs are required evidence for a green workflow. The split exposes formatting, unit-test, and release-build failures without making those checks wait for emulator startup, while the emulator suite runs at the same time.

The fast job has a 20-minute timeout. The emulator job has a 35-minute timeout to allow for Android image setup and emulator boot while preventing an indefinitely occupied runner. The workflow concurrency group cancels superseded runs for the same pull request or branch.

## Gradle performance settings

The root `gradle.properties` enables:

- `org.gradle.parallel=true`: independent subproject tasks can execute concurrently.
- `org.gradle.caching=true`: Gradle can reuse cacheable task outputs.
- `org.gradle.configuration-cache=true`: the build must remain Configuration Cache compatible. Gradle's default problem mode is fail; do not change it to warning mode just to make CI pass.

`gradle/actions/setup-gradle` manages the Gradle User Home cache, including downloaded dependencies and the local build cache. Keep this as the single Gradle cache owner; do not add a second overlapping cache for the same Gradle directories.

The Configuration Cache lives in the build workspace under `.gradle/configuration-cache`. GitHub-hosted jobs use separate ephemeral workspaces, so this workflow does not claim to reuse Configuration Cache entries between jobs or separate workflow runs. The setting is still valuable as a strict compatibility gate and enables Gradle's parallel task execution within a build invocation. Local development builds can reuse the configuration entry while their workspace remains available.

Configuration-on-demand is intentionally not enabled: the required CI task sets involve all three modules, so it does not avoid configuring projects in this workflow. Avoid enabling incubating configuration optimizations without measuring a real benefit.

## Test coverage boundary

Passing CI proves formatting, compilation, unit behavior, and the automated Compose semantics/interaction assertions that run in the emulator. It does not replace manual validation with TalkBack, physical keyboard hardware, RTL visual inspection, large font scaling, contrast checks, reduced-motion preference changes, or physical haptic output. Record those as deferred until actually tested.
