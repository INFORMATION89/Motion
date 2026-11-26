# AI guide: planning Motion adoption in Solstice

## Purpose

Use this guide when an AI coding agent is asked to plan how Solstice should consume Motion.

The first task is an evidence-based **adoption plan in the Solstice repository**. Do not begin a migration, change Solstice runtime code, or move the Motion submodule while producing that plan unless the owner explicitly approves implementation.

The goal is to reuse Motion Core for generic interaction behavior, identify reusable component building blocks for the future Motion UI layer, keep Solstice-specific UI in Solstice, and avoid duplicating Compose/Material or specialized libraries.

## Read and verify first

Before making recommendations, the agent must:

1. Read Motion's `AGENTS.md`, `README.md`, `docs/FRAMEWORK_OVERVIEW.md`, `docs/PLATFORM_SUPPORT.md`, `docs/MOTION.md`, and `motion/README.md`.
2. Read Solstice's current `AGENTS.md` (when present), `README.md`, `docs/CONTINUE.md`, `docs/PLAN.md`, and the relevant UI/design-system documentation.
3. Inspect Solstice's actual `.gitmodules`, `settings.gradle.kts`, Gradle dependency declarations, CI workflow, current Motion submodule SHA, and working-tree status.
4. Inspect the current Motion source and public API at the exact submodule SHA. Also inspect the latest Motion branch/PR state and CI; do not assume the checked-out submodule is identical to the latest Motion branch.
5. Search the current Solstice code for animation and interaction implementations. At minimum inspect uses of `Animatable`, `animate*`, `spring`, `tween`, `pointerInput`, `draggable`, `scrollable`, morph/shared-transition APIs, page transitions, and haptics. Follow the call sites to understand ownership and state.

Do not rely on old plans or filenames without confirming they still exist. A historical candidate list is a search aid, not evidence that the code is still present or unchanged.

## Classify every candidate

For each behavior found in Solstice, assign exactly one primary recommendation:

| Classification | Meaning | Expected action |
|---|---|---|
| Direct reuse | Motion already exposes the required generic behavior | Use the existing API; no new Motion code. |
| App-side composition | The motion is generic, but styling, content, state, or component structure is Solstice-specific | Keep the component in Solstice and compose it with Motion Core. |
| Motion UI candidate | A component structure is reusable across apps, configurable, and not already better supplied by Material or a focused library | Propose it for the planned `:motion-ui` module; keep it in Solstice until that module exists and its API is reviewed. |
| Motion capability gap | A reusable behavior is missing from Motion and has value beyond Solstice | Propose a separate Motion API/design issue with a contract and tests; do not implement it during planning. |
| Keep in Solstice | The behavior is domain-specific, tied to weather UX, product rules, visual identity, or app architecture | Leave it in the app. |
| Use another library | A standard component or platform capability is already solved by Compose, Material, or a specialized library | Reuse that library instead of duplicating it. |
| Use another library | A standard component or platform capability is already solved by Compose, Material, or a specialized library | Reuse that library instead of duplicating it. |

Do not promote code to Motion merely because it contains animation calls. Motion Core is for generic motion mechanics; Motion UI is for reusable, configurable component building blocks; standard controls stay with Material/Compose; Solstice's visual design, screens, data, and business logic stay in Solstice.

## Motion capabilities to check before proposing new work

Inspect the exact current API, but begin by checking these existing areas:

- `MotionSpring` and `Motion` tokens.
- `MotionValue`, `rememberMotionValue`, and `Modifier.motionDrag`.
- `MotionPhysics` for projection, target selection, and rubber-banding.
- `MorphHost`, morph anchors, `Morph`, and `MorphStyle`.
- `reveal` and `handOff` choreography.
- Page-spring helpers and page overshoot.
- `Dial` and its state/style APIs.
- Haptic tokens and the haptics abstraction.
- Ambient-motion clock and reduced-motion support.
- Edge blur helpers where they genuinely belong to a reusable motion effect.

Do not assume a capability is production-ready merely because its name exists. Check implementation, KDoc, tests, current limitations, and platform requirements. Respect the MOT0.1 reliability gate before depending on contracts that are still being validated.

## Solstice candidates to inspect

The repository extraction plan previously identified these areas as possible migration candidates. Verify the current files and their implementation before making any recommendation:

- Shell/page navigation motion.
- Toolbar interaction and movement.
- Press morph behavior in segmented rows.
- Sheet state, detents, drag, and source-to-sheet morphing.

These are candidates for analysis, not automatic moves. A candidate may remain in Solstice if it is coupled to app state or visual identity, or may only need a thin adapter over an existing Motion primitive.

## Required adoption-plan output

Create or update `docs/MOTION_ADOPTION_PLAN.md` in Solstice, containing:

1. **Verified baseline:** Solstice commit, Motion submodule SHA, Motion branch/PR SHA considered, relevant CI runs, and platform/build constraints.
2. **Current integration:** how the submodule and Gradle composite build work; any stale URL, checkout, or CI issue.
3. **Inventory:** one row per candidate with file paths and call sites, current behavior, state owner, gesture owner, styling dependencies, and tests.
4. **Classification:** one of the six classifications above, with a short evidence-based reason.
5. **API fit/gap:** exact Motion API that can be reused, or the smallest missing generic contract. Do not invent APIs or copy source code into the plan as though it already exists.
6. **Migration sequence:** small, independently reviewable phases ordered by dependency and risk. Identify which files change in Solstice and which would require a separate Motion PR.
7. **Acceptance criteria:** observable interaction behavior, state/gesture ownership, interruption and velocity expectations, accessibility/reduced-motion requirements, and regression tests.
8. **Verification plan:** focused CI checks first, one complete CI run per coherent batch, and any device checks clearly marked `NOT TESTED` / `DEFERRED` while hardware is unavailable.
9. **Risks and non-goals:** behaviors that must stay in Solstice, unsupported platforms, and work intentionally excluded from the migration.

Include an explicit mapping table, for example:

| Solstice feature/file | Current implementation | Motion API fit | Recommendation | Missing work | Verification |
|---|---|---|---|---|---|

## Migration rules

- Do not change Solstice source code, the Motion submodule pointer, or the Motion repository while creating the adoption plan.
- Do not copy Solstice screens, themes, weather models, icons, glass/sky styling, or product logic into Motion.
- Do not move code into Motion if it imports Solstice packages or relies on app-specific types. Extract only the generic contract and leave an app-side adapter.
- Prefer existing Compose and Material components for structure, semantics, and accessibility. Use Motion for movement and interaction mechanics, not as a replacement component kit.
- Avoid new hardcoded spring values in Solstice when an appropriate Motion token or public contract already exists. If a distinct feel is genuinely needed, document whether it is app-specific or a reusable token gap.
- Keep documented exceptions to motion rules explicit. Do not introduce new timed/eased motion just to simplify a migration.
- Keep animations interruptible and input responsive according to `docs/MOTION.md`. A gesture must have a clear owner, and a new command must not be blocked by an unrelated animation.
- Do not use `git submodule update --remote` as an unreviewed release/update mechanism. Pin a specific tested Motion commit and record the reason for changing the pointer.
- Do not claim device behavior is validated by CI. Physical-device checks remain deferred unless they become an explicit milestone/release requirement.

## Suggested phase structure

1. **Audit only:** produce the inventory, ownership classification, API fit/gap, and migration risks. No code changes.
2. **Agree on architecture:** review the proposed boundaries and any missing Motion API before implementation.
3. **First low-risk integration:** choose one small feature that maps directly to an existing API; migrate it in Solstice and verify that visual identity remains unchanged.
4. **Reusable gaps:** for each genuinely generic gap, create a separate Motion proposal/PR, test it in Motion, then pin the resulting commit in Solstice.
5. **Remaining migrations:** proceed one feature at a time, with a Solstice adapter where necessary.

The adoption plan is complete when every relevant candidate has an owner, a recommendation, a target API or documented gap, and verifiable acceptance criteria. Do not start implementation just because the inventory is complete.
