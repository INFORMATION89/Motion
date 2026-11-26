# MOT0.3 — Motion UI foundations (proposal)

**Status:** proposal only; no `:motion-ui` module or runtime code is created by this plan. Implementation begins only after the owner approves the component contracts and the first deliverable.

**Prepared:** 2026-11-26  
**Repository baseline:** `main` at `dac8df73387e87839192b867cfc2ed57b8df4561`  
**Previous milestones:** MOT0.1 and MOT0.2 are complete and merged.

## 1. Goal

Create an optional Android / Jetpack Compose UI layer, `:motion-ui`, that provides a small set of configurable building blocks for apps that want them. It must build on Motion Core (`:motion`) without changing or expanding Core's responsibility.

A consuming app may:
- use `:motion` alone;
- use `:motion` plus `:motion-ui`;
- use neither UI kit components nor Motion's visual defaults where its own design system is preferred.

Motion UI is not a replacement for Material 3, Compose, Navigation, or an app design system. It is not a complete application framework.

## 2. Agreed architectural direction

These decisions were recorded as owner-agreed direction in the Solstice project handoff on 2026-11-22 and are carried forward here for a scoped proposal:

1. **Module boundary:** `:motion-ui` is a separate Gradle Android library in this repository. It depends on `:motion`; Core must never depend on UI.
2. **Theme ownership:** the kit defines semantic token names and component contracts; consuming apps supply their actual values. Provide neutral Material-based defaults so the kit works independently.
3. **Materials:** provide a configurable glass surface and a distinct card material. The app supplies its values; final card-material values are selected in Motion Lab with the owner, not invented in this plan.
4. **First building blocks:** theme/tokens, glass and card surfaces, page frame/scaffold and header-action slots, then sheet pieces.
5. **Platform reuse:** use Material 3 for standard controls and semantics. Use Haze for backdrop blur if the selected version, licence, and Android compatibility are verified; do not implement a competing blur engine.
6. **Motion Lab:** use the standalone Lab as the first independent consumer and proof that components work without Solstice packages, resources, or branding.

If any of these decisions has changed, stop before implementation and revise this proposal.

## 3. Evidence and current baseline

- `:motion` and `:motion-lab` are the only current Gradle modules. `:motion-ui` does not exist.
- Current build baseline: Android API 29+, compile SDK / Build Tools 37, JDK 21, Kotlin 2.4.21, AGP 9.5.1, Compose BOM 2026.09.00.
- Material 3 is already used by `:motion-lab`; Motion Core does not depend on Material 3.
- Haze is not currently declared in the version catalog or modules. Do not add it until its exact version, licence, maintenance, API compatibility, and fallback behavior are reviewed.
- CI currently runs Spotless, `:motion:testDebugUnitTest`, `:motion:assembleRelease`, and `:motion-lab:assembleDebug`.
- MOT0.1 and MOT0.2 are merged. PR #9 (rules), #7 (Lab foundation), #8 (Lab sections/theme), #10 (MOT0.2 documentation closeout), and #11 (repository baseline refresh) are merged. CI #108 passed on the exact PR #8 head; CI #110 passed for PR #10; CI #112 passed on the exact PR #11 head.
- Latest repository main: `dac8df73387e87839192b867cfc2ed57b8df4561`. There are no open Motion PRs or issues at this handoff.
- Solstice currently pins Motion submodule commit `25ffa862f503b482ed98671cd5eab66040f0d75f`, eight commits behind current Motion main. This is consumer state only: this proposal does not update Solstice's submodule, URL, or runtime code. Solstice's current documentation/CI work is tracked separately in its own PR.

## 4. Scope and delivery stages

### MOT0.3A — module and theme foundation

- Add `:motion-ui` as an Android library module.
- Make its dependency direction explicit: `:motion-ui` → `:motion`; no reverse dependency.
- Add Material 3 only to `:motion-ui`, not to Core.
- Define a small theme entry point and semantic token contract for color/material, typography, shape, spacing, and motion values where required by the first components.
- Use neutral, accessible Material-based defaults. Apps must be able to inject their own values without forking the kit.
- Keep tokens semantic and minimal. Do not expose every internal dimension or create a second full design-system framework.
- Add focused unit/Compose tests for theme defaults, overrides, and module boundaries.
- Add `:motion-ui:assembleRelease` and its relevant tests to CI.

**Acceptance:** a clean sample consumer can import `:motion-ui`, use its default theme, override the supplied values, and build without depending on Solstice. Core remains buildable and unchanged.

### MOT0.3B — surfaces and page structure

Deliver in small PRs, in this order:

1. **Card surface:** configurable container, shape, elevation/material, and content slot. No weather-specific content or fixed brand styling.
2. **Page frame:** scaffold/frame with content and header-action slots. The app owns routes, page state, navigation policy, labels, and actions.
3. **Sheet building blocks:** reusable handle, shape/corner treatment, and layout slots only where they add value beyond Material 3. Keep Material 3's standard modal sheet and accessibility behavior rather than cloning it.
4. **Header actions:** configurable action slots and reusable visual/interaction wiring, not a new button system. Standard buttons remain Material 3.

Each component needs previews, focused tests, semantics, touch-target/focus review, RTL and font-scale consideration, and a Motion Lab example where useful.

**Acceptance:** the same component works under two materially different app-supplied themes; no Solstice resource, package, string, domain model, or navigation dependency appears in the module.

### MOT0.3C — glass material (conditional)

- Evaluate a pinned Haze release and its licence/maintenance/API compatibility before adding it.
- Wrap Haze only behind a narrow, configurable surface contract if that materially improves reuse.
- Require a no-blur fallback for unsupported devices and settings; blur must never be required for readability or interaction.
- Keep tint, opacity, color, and material choices app-supplied.
- Do not build a blur engine or duplicate Haze.

**Acceptance:** glass can be used with app-defined values, degrades cleanly without blur, and does not impose a visual brand.

### MOT0.3D — independent consumer proof

- Use `:motion-lab` to demonstrate every shipped UI component using its own theme first.
- Add a second deliberately different sample theme to prove that values are injected rather than hardcoded.
- Keep all demonstrations independent of Solstice and weather concepts.
- Only after the module is stable should a separate Solstice adoption PR pin a tested Motion commit and evaluate a first low-risk integration.

**Acceptance:** Lab builds through required CI; components remain optional; Solstice migration is a separate reviewed consumer change.

## 5. Reduced motion and interaction contracts

- Follow Motion Core's current reduced-motion signal and behavior; do not invent a second global preference system.
- Preserve the agreed principle that reduced motion removes travel, bounce, and receding effects while essential drag tracking remains directly tied to the finger.
- Before implementing each sheet or morphing surface, specify whether its reduced-motion result is an immediate state change or a brief in-place transition. Do not promise a fade unless it is implemented and verified; current Core `Morph` resolves to target geometry and alpha follows the value, while Compose may finish the animation immediately when system animations are disabled.
- Input must remain responsive. Gestures have one clear owner, commands may interrupt or retarget movement, and velocity continuity must be preserved where the Core contract requires it.
- Haptics remain optional semantic feedback, never the only indication of state.
- Do not change existing Core behavior or add Core APIs as incidental requirements of a UI component.

## 6. Architecture and clean-room constraints

- Motion UI may depend on Compose, Material 3, Motion Core, and specifically reviewed rendering dependencies. Motion Core must not depend on Motion UI.
- No Solstice or Breezy Weather source, assets, package names, resource identifiers, strings, themes, or domain types may enter Motion.
- Solstice is LGPL-3.0; Motion is Apache-2.0. Do not copy or translate Solstice implementation code into Motion. Use the agreed behavior requirements and public API contracts to write an original implementation. Preserve provenance and licence review for every dependency.
- Do not create competing buttons, text fields, navigation, back-stack, dialog, or accessibility systems.
- Keep the module optional and keep public APIs small, documented, and slot/configuration-driven.
- No KMP, iOS, Windows, macOS, Linux, web, publishing, or release work is part of MOT0.3.

## 7. Verification and cost controls

For each coherent component batch:

1. Run Spotless and the smallest affected module tests while iterating.
2. Run `:motion-ui:assembleDebug` / `:motion-ui:assembleRelease` and relevant unit/Compose tests.
3. Run the complete required CI workflow once on the exact final PR head.
4. Keep deterministic tests; do not add redundant layers or broad matrices without a concrete risk.
5. Inspect failed logs and retry only with a technical reason. Never weaken tests to make the build green.
6. Record emulator/device, TalkBack, visual contrast, haptic, and hardware-dependent checks as `NOT TESTED` / `DEFERRED` until actually run.

Opus review is optional for a complex API, dependency, or accessibility design; it is not a blocker and does not authorize paid usage.

## 8. Explicit non-goals

- No Solstice source migration or submodule bump inside this milestone.
- No Motion Core rewrite or new generic physics engine.
- No standard-control replacements or complete design system.
- No hardcoded Solstice theme, colors, typography, strings, screens, routes, or weather content.
- No Haze dependency until its version and licence are reviewed.
- No platform expansion or release/publishing.

## 9. Owner review gates

Before implementation, review and approve:
- the exact semantic theme/token contract;
- the first component API and slots;
- whether the glass wrapper belongs in the first implementation batch after Haze review;
- the reduced-motion behavior of sheet and surface components;
- the visual card-material values in Motion Lab.

This document is a proposal and does not authorize silently changing product behavior or the public API.
