# Continue

This file is the working handoff for the next development session. Keep it current when the milestone, branch, CI state, or immediate next steps change. It is an execution guide, not a substitute for the design documents.

## Read first

1. [Project rules](docs/RULES.md) — how Motion work is planned, implemented, reviewed, and accepted.
2. [Agent instructions](AGENTS.md) — repository-wide constraints for AI coding assistants.
3. [Framework overview](docs/FRAMEWORK_OVERVIEW.md) — what belongs in Motion and what belongs in app/UI libraries.
4. [Platform support](docs/PLATFORM_SUPPORT.md) — current Android integration and future platform requirements.
5. [Solstice AI adoption guide](docs/AI_SOLSTICE_ADOPTION.md) — how to create the adoption plan without prematurely migrating code.
6. [MOT0.1 plan](docs/MOT0.1_PLAN.md) — code tasks, tests, and acceptance criteria.
7. [Motion principles](docs/MOTION.md), [morph study](docs/MORPH_STUDY.md), and [module API](motion/README.md).

## Project guardrails

- Motion is an independent, reusable Android/Jetpack Compose motion framework. It must not depend on Solstice or import application code.
- Motion Core is not a UI kit. Reusable interaction mechanics belong in Core; the planned optional Motion UI layer may provide configurable components, while app branding, navigation policy, screens, and product behavior remain with consuming apps.
- Compose remains the animation and interaction foundation. Do not introduce a separate universal physics engine.
- Prioritize interruptible motion, continuous velocity, predictable gesture ownership, and explicit input contracts.
- Desktop and iOS are not currently supported. A multiplatform port requires a separate design and verification plan.
- Keep milestone identifiers in the `MOT` family. MOT0.1 remains limited to core reliability, focused tests, and directly related docs.

## Consolidated work

**Integration:** PR #4 is the integration vehicle for MOT0.1. Check its live GitHub status before taking action; this handoff does not assume whether it is open, ready, or merged. The branch contains the MOT0.1 implementation work plus framework, platform, Solstice-planning, AI-agent, and cost-aware verification documentation.

- Branch: `work/consolidated-docs-platforms`
- Current implementation head before this correction: `0c5cdfa99f4ef87aaa54936ff5842c8d6c9ef03f`.
- [Run #59](https://github.com/INFORMATION89/Motion-UI/actions/runs/45764340044) passed Spotless but one source-retarget continuity assertion failed. Unit tests stopped at 63 tests with one failure; library build was skipped.
- The failure showed the test sampled before the close spring had reversed its inherited opening momentum. The corrected test waits until the closing trajectory is observably moving toward the source before retargeting, then verifies direction continuity and the final source anchor.
- The trailing blank-line violation from run #57 was corrected in head `0c5cdfa`; run #58 confirmed Spotless now passes.
- After this focused test correction, run the full required workflow once on the exact resulting head. Do not spend another full run for documentation-only changes.
- PRs #1–#3 were consolidated into PR #4. Their old branch names, SHAs, and CI runs are historical only.

### Already implemented in MOT0.1

- Validate finite `MotionValue` inputs and valid non-negative configuration.
- Copy, sort, and deduplicate `restPoints`; reject empty or non-finite values.
- Keep flick thresholds ordered and update them safely when the drag range changes.
- Validate projection and target-selection inputs in `MotionPhysics`.
- Include position as well as dimensions in `MorphMotion.progress`.
- Retarget open morphs when the target changes, including after settling; retarget closing morphs when the source changes; defer anchor changes while caught or open as appropriate.
- Keep gesture ownership visible during drag/scrub, preserve spring velocity when retargeting, and guard pending open/close placement continuations with command generations.
- Validate finite drag/scrub/release input and anchor geometry, while allowing legitimate zero-size anchors.
- Cover interruption/reversal, target/source retargeting, caught gestures, high-velocity release, scalar input contracts, rest-point updates, and same-target animation with deterministic frame-clock tests.

### Remaining MOT0.1 work

No additional feature work is planned for MOT0.1. The remaining gate is final verification on the exact branch head, review of the complete diff, and integration of PR #4.

1. Review the complete diff against `main`; verify no unrelated changes or stale documentation remain.
2. Verify the final documentation-only synchronization with the required CI workflow.
3. Confirm Spotless, `:motion:testDebugUnitTest`, and `:motion:assembleRelease` pass on the exact final PR head.
4. Update the PR description with exact evidence, mark PR #4 ready, and merge it into `main` after all required checks pass.
5. Keep physical-device checks marked `NOT TESTED` / `DEFERRED`; they are not part of this milestone's CI gate.

## Local setup and verification

The repository specifies JDK 21 and Android SDK / Build Tools 37.

On Windows PowerShell:

```powershell
git fetch origin
git switch work/consolidated-docs-platforms
git pull --ff-only
git status --short --branch
java -version
.\gradlew.bat --version
.\gradlew.bat spotlessCheck
.\gradlew.bat :motion:testDebugUnitTest
.\gradlew.bat :motion:assembleRelease
```

On macOS/Linux, use `./gradlew` instead of `gradlew.bat`.

The complete GitHub Actions workflow runs:

```bash
./gradlew spotlessCheck
./gradlew :motion:testDebugUnitTest
./gradlew :motion:assembleRelease
```

Use the exact branch-head SHA and its CI result as evidence. Physical-device checks are separate from CI and remain deferred while hardware is unavailable.
