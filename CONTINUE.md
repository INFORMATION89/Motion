# Continue

This file is the working handoff for the next development session. Keep it aligned with the actual branch, pull requests, CI results, and immediate next actions.

## Read first

1. [Project rules](docs/RULES.md) and [agent instructions](AGENTS.md).
2. [MOT0.3A execution plan](docs/MOT0.3A_PLAN.md).
3. [MOT0.3B toolbar contract](docs/MOT0.3B_TOOLBAR_PLAN.md).
4. [Framework overview](docs/FRAMEWORK_OVERVIEW.md) and [platform support](docs/PLATFORM_SUPPORT.md).
5. [Motion UI theme API](motion-ui/README.md), [Core API](motion/README.md), and [consumer adoption guide](docs/AI_CONSUMER_ADOPTION.md).
6. [Opus review, 2026-11-28](docs/reviews/OPUS_REVIEW_2026-11-28.md) (PR #26 prototype and the Core AmbientClock change) and the [ChatGPT handoff](docs/CHATGPT_HANDOFF.md).

## Guardrails

- Motion is an independent Android / Jetpack Compose framework.
- `:motion-ui` is optional and depends on `:motion`; Core must never depend on UI.
- Applications own their branding, domain, screens, routing, and product behavior. Motion UI provides configurable reusable building blocks only.
- Material 3 remains the source for standard controls, typography, and accessibility semantics.
- Do not copy consumer-app or third-party source, assets, resources, or branding into Motion.
- Do not add Haze until its exact version, license, maintenance, compatibility, and fallback are reviewed.
- Android API 29+, compile SDK 37, JDK 21. iOS and desktop are not supported by the current artifact.
- Use focused checks during iteration and one complete required CI run on the final PR head. Report device, TalkBack, contrast, and haptic checks as deferred unless actually run.

## Verified project state

As of 2026-11-28; `main` is at 51420fd.

- MOT0.1 and MOT0.2 are complete.
- MOT0.3A is merged into `main` (PR #13, merged 2026-11-27, head 1c87bfd). It delivers the optional `:motion-ui` Android library, which depends on `:motion` only.
- Initial API: `MotionUiTheme`, `MotionUiThemeTokens.current`, `MotionUiTokens`, `MotionUiShapes`, `MotionUiSpacing`, and `MotionUiMaterials`. Token defaults derive from the caller's Material 3 color scheme; consumers can override them.
- Focused unit tests cover default light/dark values and custom token overrides. Required CI includes Motion UI tests and release assembly.
- CI: run #176 passed on the final PR #13 head 1c87bfd; push run #202 passed on `main` at 51420fd (after the PR #25 merge). Push run #201 on the intermediate merge commit 2887d8a was cancelled by the concurrency group.
- The obsolete, unbuilt prototype directory was removed because it contained app-coupled source under LGPL-3.0 notices; no implementation from it was carried into Motion.
- The MOT0.3B toolbar contract (PR #25, merged 2026-11-27) is accepted as a design contract. No public Kotlin toolbar API is committed.
- Open draft PR #26 (`feat/mot03b-toolbar-prototype`) is an internal Motion Lab toolbar prototype. The touch-injection test fix (`956f044`) was cherry-picked alone as `f1ed520`; CI run #222 passed both jobs (fast checks and API 35 emulator) on head `6812d7d`. CI runs with animations disabled, and no device or TalkBack validation has been done. Review findings B1–B7 remain open.
- Cloud sessions for this repository should use the personal **Motion-UI** environment, whose setup script installs the Android SDK (API 37, build-tools 37.0.0) and limits Gradle to one worker to avoid proxy rate limits.
- `fix/mot03b-pointer-injection-scope` has since gained an unrelated Motion Core AmbientClock change and research docs. These must be split into separate PRs; the Core change requires Opus review and owner approval.
- PR #15 (`docs/ai-model-routing-policy`) was closed on 2026-11-28 as superseded by PR #28, which adopts tiered AI routing on current `main`. Its branch is kept for reference; the cross-repository audits in its `docs/OPUS_REVIEW_BRIEF.md` remain Opus review pending.
- Do not merge any of these without explicit owner approval.

## MOT0.3A acceptance

- [x] `:motion-ui` builds as an independent Android library (merged into `main`).
- [x] Dependency direction is UI -> Core only.
- [x] Focused tests for theme defaults and app overrides exist.
- [x] Spotless and full required CI pass on the exact final head (1c87bfd, run #176; `main` 51420fd, run #202).
- [x] README and API docs describe the delivered module; CI includes Motion UI tests and release assembly.
- [x] No consumer-specific code, dependency pin, Haze dependency, or unsupported platform work is introduced.

## Next steps

1. ~~Land commit 956f044 alone into PR #26~~ Done 2026-11-28 (run #222 green). Next: the two Sonnet sessions (S2, S5) planned in `docs/CHATGPT_HANDOFF.md`.
2. Split `fix/mot03b-pointer-injection-scope` into (a) a Core AmbientClock PR (Opus review and owner approval required) and (b) a research-docs PR.
3. Owner review of PR #28 (status sync, Opus review, tiered AI routing) and answers to Q1–Q6 in `docs/CHATGPT_HANDOFF.md`.
4. Act on the findings of the [Opus review](docs/reviews/OPUS_REVIEW_2026-11-28.md) for PR #26.
5. After prototype review, design the smallest public toolbar API that satisfies the accepted contract.
6. Reusable card/surface work follows later.

## Deferred validation

Emulator / physical-device behavior, TalkBack traversal, contrast inspection, and hardware haptics remain NOT TESTED until run on real supported environments.
