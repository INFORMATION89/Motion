# Repository instructions for AI agents

Read this file before making changes. Then read [README.md](README.md), [docs/MOTION.md](docs/MOTION.md), and the relevant implementation or design documents under `docs/`. The motion behavior rules in `docs/MOTION.md` remain authoritative.

## Project boundaries

- This repository contains the reusable Motion framework for Jetpack Compose apps. It is not a Solstice module and must remain useful without Solstice.
- Never add Solstice, weather-domain, or other consumer-app dependencies, source code, assets, or assumptions to Motion.
- Motion-specific behavior and interaction decisions must follow `docs/MOTION.md`. New or changed motion still requires the owner's agreement as stated there.
- Keep the library, tests, and (when available) Motion Lab aligned. Do not silently widen public APIs, supported platforms, dependencies, or project scope.

## Cost-aware verification

1. **Plan before running checks.** State the changed modules, acceptance criteria, smallest relevant checks, expensive checks, and any device/integration requirements. Prefer checks that can detect a regression in the changed code.
2. **Use focused checks while iterating.** Run the affected unit-test or Gradle task rather than the full workflow after every small edit. For documentation-only changes, do not run code builds or tests that the change cannot affect.
3. **Batch coherent changes.** Finish a related implementation/documentation batch before pushing or requesting the full pull-request CI workflow. The current CI workflow runs Spotless, `:motion:testDebugUnitTest`, and `:motion:assembleRelease`; run that complete workflow once when the batch is ready.
4. **Reuse valid results.** A passing result remains valid for the same commit and relevant environment. Do not rerun checks that the change cannot affect. When only one CI task/job fails, inspect its logs and rerun only the failing task/job where possible.
5. **Limit infrastructure retries.** Diagnose the first failure. For a genuine environment or infrastructure failure, make at most one retry using a materially different setup. If the same requirement fails again, stop, mark it `BLOCKED`, and report the evidence and required host/tool access. Do not turn an untested check into a pass.
6. **Get approval before expanding the budget.** Before adding a substantial new test layer, broadening the platform matrix, or spending another full CI run after a failure, explain why the additional check is necessary and obtain the owner's approval when it exceeds the agreed verification plan.
7. **Keep device evidence separate.** Current-stage acceptance may rely on passing CI while device access is unavailable. Physical-device/Pixel checks are deferred unless a task or release gate explicitly requires them; record them as `NOT TESTED` or `DEFERRED`. CI does not prove touch behavior or physical-device compatibility.
8. **Report evidence precisely.** Record the exact commands or CI run, commit SHA, results, skipped checks, and blockers. Never claim a build, test, platform, or device check passed unless its result was actually inspected.

## Git workflow

- Work on a focused branch and use a pull request; do not push development work directly to `main`.
- Keep related changes together so one coherent PR can receive one complete CI verification cycle.
- Do not merge, release, publish, create tags, or change repository protections without explicit owner approval.
