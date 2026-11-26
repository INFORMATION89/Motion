# MOT0.2 — Standalone Motion Lab

**Status:** complete and merged into `main` through PR #7; the Components / Interactions and system-theme follow-up is merged through PR #8.  
**Baseline:** `main` at the MOT0.2 integration commit `6ea53caf31a17b70894487cd065707a800d1a35a`; final Lab follow-up merged at `130c8abb7a1ca6613d70796255f9385362bb0f41`.  
**Milestone type:** sample app, documentation, and CI; no Motion Core behavior changes

## Goal

Turn the existing Motion Lab prototypes into a standalone Android sample app in this repository. The Lab should make Motion understandable and testable without requiring a consuming application, while remaining separate from the reusable `:motion` library.

The app is a showroom and test bench for the public Motion API. It is not the planned `:motion-ui` component library and must not become a consumer-specific demo.

## Why this is next

MOT0.1 completed the reliability baseline for Motion Core. The repository roadmap identifies an independently buildable Motion Lab as the next phase, before broader reusable-interaction extraction and consumer adoption work.

The current `lab/` directory is only a source prototype. It is not included in Gradle settings or CI. Its Kotlin files still depend on legacy app-specific app classes, resources, packages, and theme tokens, so they cannot simply be moved into an independent app.

## Architecture and boundaries

- Add a separate Android application module, initially named `:motion-lab`.
- The app depends on `:motion`; `:motion` must never depend on the Lab.
- Use Jetpack Compose and Material 3 for app structure, standard controls, semantics, and accessibility.
- Use the public Motion API for the behavior being demonstrated. Do not reach into library internals or expose APIs solely to make a demo easier.
- Keep the app visually neutral and generic. Use generic sample content; no domain-specific data, a consuming application names, assets, icons, screens, theme classes, navigation policy, or app dependencies.
- Keep the current Android-only support boundary. This milestone does not add iOS, desktop, web, or Kotlin Multiplatform targets.
- Do not create `:motion-ui` as part of this milestone.

## Legacy prototype and licensing constraints

Treat `lab/` as a historical prototype and behavior reference, not as code ready to be compiled into the new app.

The existing files include legacy app-specific packages, app-specific imports, and LGPL-3.0 notices. Therefore:

- Reimplement the demonstrations against Motion's public API and standard Compose/Material 3 building blocks; do not bulk-move or mechanically rename the old files.
- Do not remove copyright or license notices from code that is genuinely reused.
- Before reusing any legacy implementation code, verify its provenance and license compatibility with the target module and repository licensing. When that is unclear, write a clean implementation instead.
- Keep a consuming application and other consumer repositories out of the Lab's dependency graph.

## Current implementation

The initial app foundation and gallery have been added on the MOT0.2 branch:

- `:motion-lab` is included in Gradle as a standalone Android application and depends on `:motion`.
- The app has its own namespace, application ID, launcher activity, and neutral Material 3 theme.
- The home gallery links to spring/release, morph, page movement, dial, and scroll edge-blur examples. Page movement and Morph now have opt-in comparison modes. Page movement sends the same destination to Motion's `springToPage` and Compose's default `animateScrollToPage` concurrently; swiping is disabled in comparison mode. Morph uses one shared open/close state to drive Motion's measured-anchor container transform and Compose's default `AnimatedVisibility` transition. The Haptics page exposes all nine existing `Haptic.all` tokens through `haptics().perform(token)` and a global intensity slider; it restores `Haptic.intensity` to `1f` when leaving the page.
- Slow motion controls `MotionTime.scale` and resets to normal when the Lab leaves composition.
- The CI workflow includes `:motion-lab:assembleDebug`.

MOT0.2 implementation is complete and merged. The follow-up Components / Interactions tabs and automatic system light/dark appearance were delivered in PR #8, separately from the original MOT0.2 acceptance scope. Workflow [#98](https://github.com/INFORMATION89/Motion-UI/actions/runs/45779868512) passed on exact PR head `22707a05abf857efdda902b9afcdb3c37d259ce5`, including Spotless, Motion Core unit tests, `:motion:assembleRelease`, and `:motion-lab:assembleDebug`. This verified head includes the Morph action-label correction, explicit button semantics on gallery cards, and synchronized handoff documentation. CI validates formatting, tests, and compilation, not visual behavior, touch interactions, screen-reader experience, or real vibration output. Emulator and physical-device interactions remain `NOT TESTED` / `DEFERRED`.

## Delivery phases

### 1. App foundation

- Add the Android application plugin and `:motion-lab` module with its own namespace, application ID, manifest, and entry activity.
- Add only dependencies needed by the sample app, using the repository's existing version catalog and Compose BOM where possible.
- Depend on `:motion` as a project dependency.
- Establish a neutral Material 3 theme and basic app navigation.
- Preserve the current library build and test tasks unchanged.

### 2. Gallery and test-bench shell

- Create a home gallery that explains the available patterns in plain language.
- Give each demo its own page and a consistent way to return to the gallery.
- Keep tuning/debug controls separate from the main explanation.
- Add a controlled slow-motion selector and reset `MotionTime.scale` when leaving the Lab or destroying the activity, so the global setting cannot leak into a consuming app.
- Make demos usable with touch and standard accessibility semantics.

### 3. Port the stable demonstrations

Build focused examples for capabilities already present in the public API, starting with:

- Spring tokens and interruptible release/settling.
- Morph source-to-target interaction.
- Page spring and overshoot.
- Dial interaction and snapping.
- Scroll edge blur.
- Haptic primitives where platform support exists. The Lab exposes all nine public semantic tokens and a temporary global-intensity control; hardware output remains unverified.

Every example must demonstrate a real public API contract, document its interaction, and handle unavailable platform behavior honestly. Do not invent a fake implementation for missing capabilities.

### 4. Meaningful comparisons and tuning

- Add side-by-side comparisons with reasonable Compose/Material defaults for the patterns where a comparison teaches something useful.
- **Comparisons implemented:** page movement (a shared destination drives Motion's `springToPage` and Compose's default `animateScrollToPage`) and Morph (a shared open/close state drives Motion's measured-anchor container transform and Compose's default `AnimatedVisibility`). Independent gestures are disabled in comparison mode to keep the input equivalent.
- **Deferred comparison:** a sheet-like interaction. Core provides motion state and gestures, not a dedicated sheet component; add this comparison only if Motion and the Material 3 baseline can share equivalent detents and input without a new Core API.
- Ensure both sides receive equivalent input and state so the comparison is fair.
- Expose only a small set of tuning controls that help users understand the effect of each parameter.
- Avoid copying Motion's implementation into the baseline side; the baseline should use the standard Compose/Material approach.

### 5. CI, documentation, and handoff

- Add the Lab's focused build to CI (at minimum `:motion-lab:assembleDebug`).
- Keep existing Motion Core formatting, unit tests, and release assembly gates.
- Add build/run instructions and an inventory of available demonstrations.
- Update `README.md`, `AGENTS.md`, and `CONTINUE.md` when the module is actually buildable; don't describe planned screens as implemented.
- Keep physical-device validation separate from CI.

## Acceptance criteria

MOT0.2 is complete when:

1. `:motion-lab` is a standalone Android app module and builds without a consuming application or any consumer repository.
2. A fresh checkout can build the Lab using the documented Gradle commands.
3. The gallery and initial demos use only public Motion APIs and standard Compose/Material components.
4. No legacy app-specific package, app resource, theme, icon, screen, or domain dependency remains in the new app.
5. Slow motion is restored safely and cannot leak into other app behavior.
6. CI runs the Lab build alongside the existing Motion Core checks, with all required jobs passing on the exact final PR head.
7. Documentation distinguishes what is implemented from what remains planned.
8. Device behavior is marked `NOT TESTED` / `DEFERRED` until it is actually exercised on a device or emulator.

## Verification plan

During implementation, use focused checks first:

```bash
./gradlew spotlessCheck
./gradlew :motion:testDebugUnitTest
./gradlew :motion:assembleRelease
./gradlew :motion-lab:assembleDebug
```

Run the full required GitHub Actions workflow once the coherent implementation batch is ready. Record the exact tested commit SHA and job results. Do not repeatedly rerun passing jobs or add an emulator matrix without a concrete need and approval.

Manual install/interaction checks on an emulator or physical device are separate from CI and remain `NOT TESTED` / `DEFERRED` until completed.

## Follow-up product direction (outside MOT0.2)

The next Motion Lab design iteration should consider the two-section Components / Interactions information architecture, automatic system light/dark appearance, an optional pure-black appearance, and optional predictive-back blur. These ideas are recorded in [MOTION_LAB_PLAN.md](MOTION_LAB_PLAN.md); they are not retroactive MOT0.2 acceptance criteria and must be separately scoped before implementation.

## Explicit non-goals

- Changing the public behavior or physics of existing Motion Core APIs.
- Adding new Motion Core APIs without a demonstrated reusable gap, contract, tests, and separate review.
- Building the planned `:motion-ui` component layer.
- Porting a consuming application screens, theme, assets, or product logic.
- Changing a consuming application's submodule pointer or starting an app migration.
- Publishing Motion to Maven, changing its license, or broadening platform support.
- Benchmarks, performance dashboards, or broad unrelated refactors.

## Design references

- [Motion Lab redesign](MOTION_LAB_PLAN.md) — interaction-gallery and compare-mode direction.
- [Framework overview](FRAMEWORK_OVERVIEW.md) — module responsibilities and dependency boundaries.
- [Platform support](PLATFORM_SUPPORT.md) — Android support and deferred targets.
- [Motion principles](MOTION.md) — agreed motion behavior.
- [Motion repository roadmap](MOTION_REPO_PLAN.md) — long-term phase sequence.
