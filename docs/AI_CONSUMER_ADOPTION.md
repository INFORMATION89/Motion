# AI guide: planning Motion adoption in a consumer application

## Purpose

Use this guide when an AI coding agent is asked to assess how an application should adopt Motion.

The first deliverable is an evidence-based **adoption plan in the consumer repository**. Do not begin a migration, change application runtime code, or update a Motion dependency pin while producing that plan unless the owner explicitly approves implementation.

The goal is to reuse Motion Core for generic interaction behavior, identify reusable component building blocks for Motion UI, keep product-specific UI in the application, and avoid duplicating Compose, Material, or specialized libraries.

## Read and verify first

Before making recommendations, the agent must:

1. Read Motion's `AGENTS.md`, `README.md`, `docs/FRAMEWORK_OVERVIEW.md`, `docs/PLATFORM_SUPPORT.md`, `docs/MOTION.md`, and `motion/README.md`.
2. Read the consumer repository's current `AGENTS.md` (when present), `README.md`, handoff notes, plans, and relevant UI/design-system documentation.
3. Inspect the consumer repository's actual `.gitmodules`, `settings.gradle.kts`, Gradle dependency declarations, CI workflow, current Motion dependency SHA, and working-tree status.
4. Inspect the current Motion source and public API at the exact pinned SHA. Also inspect the latest Motion branch/PR state and CI; do not assume the pinned source is identical to the latest Motion branch.
5. Search the application for animation and interaction implementations. At minimum inspect uses of `Animatable`, `animate*`, `spring`, `tween`, `pointerInput`, `draggable`, `scrollable`, morph/shared-transition APIs, page transitions, and haptics. Follow call sites to understand state and gesture ownership.

Do not rely on old plans or filenames without confirming they still exist. Historical candidate lists are search aids, not evidence that code is still present or unchanged.

## Classify every candidate

For each behavior found in the application, assign exactly one primary recommendation:

| Classification | Meaning | Expected action |
|---|---|---|
| Direct reuse | Motion already exposes the required generic behavior | Use the existing API; no new Motion code. |
| App-side composition | The motion is generic, but styling, content, state, or component structure is application-specific | Keep the component in the application and compose it with Motion Core. |
| Motion UI candidate | A component structure is reusable across apps, configurable, and not better supplied by Material or a focused library | Propose it for `:motion-ui`; keep it app-side until the module API is reviewed and available. |
| Motion capability gap | A reusable behavior is missing and has value beyond one application | Propose a separate Motion API/design issue with a contract and tests; do not implement it during planning. |
| Keep app-side | The behavior is domain-specific or tied to product rules, visual identity, or application architecture | Leave it in the application. |
| Use another library | Compose, Material, or a focused library already solves the requirement | Reuse that solution instead of duplicating it. |

Do not promote code to Motion merely because it contains animation calls. Motion Core is for generic motion mechanics; Motion UI is for reusable, configurable component building blocks; standard controls stay with Material/Compose; the application's visual design, screens, data, and business logic stay in the application.

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
- Edge-blur helpers where they genuinely belong to a reusable motion effect.

Do not assume a capability is production-ready merely because its name exists. Check implementation, KDoc, tests, current limitations, and platform requirements. Respect the relevant reliability gates before depending on contracts that are still being validated.

## Application areas to inspect

The original extraction inventory identified these generic areas as possible candidates. Verify current files, call sites, and tests before making recommendations:

- Shell and page-transition motion.
- Toolbar interaction and movement.
- Press/morph behavior in segmented controls.
- Sheet state, detents, drag, and source-to-sheet morphing.

These are candidates for analysis, not automatic moves. A candidate may remain app-side if it is coupled to application state or visual identity, or may need only a thin adapter over an existing Motion primitive.

## Required adoption-plan output

Create or update `docs/MOTION_ADOPTION_PLAN.md` in the consumer repository, containing:

1. **Verified baseline:** application commit, Motion dependency SHA, Motion branch/PR SHA considered, relevant CI runs, and platform/build constraints.
2. **Current integration:** how the dependency and Gradle composite build work; any stale URL, checkout, or CI issue.
3. **Inventory:** one row per candidate with file paths and call sites, current behavior, state owner, gesture owner, styling dependencies, and tests.
4. **Classification:** one of the six classifications above, with a short evidence-based reason.
5. **API fit/gap:** exact Motion API that can be reused, or the smallest missing generic contract. Do not invent APIs or describe proposed APIs as if they already exist.
6. **Migration sequence:** small, independently reviewable phases ordered by dependency and risk. Identify application files that would change and work requiring a separate Motion PR.
7. **Acceptance criteria:** observable interaction behavior, state/gesture ownership, interruption and velocity expectations, accessibility/reduced-motion requirements, and regression tests.
8. **Verification plan:** focused CI checks first, one complete CI run per coherent batch, and device checks clearly marked `NOT TESTED` / `DEFERRED` while hardware is unavailable.
9. **Risks and non-goals:** behaviors that must stay app-side, unsupported platforms, and work intentionally excluded from migration.

Include an explicit mapping table:

| Application feature/file | Current implementation | Motion API fit | Recommendation | Missing work | Verification |
|---|---|---|---|---|---|

## Migration rules

- Do not change application source, the Motion dependency pin, or the Motion repository while creating the adoption plan.
- Do not copy application screens, themes, domain models, icons, visual assets, or product logic into Motion.
- Do not move code into Motion if it imports application packages or relies on app-specific types. Extract only the generic contract and leave an app-side adapter.
- Prefer existing Compose and Material components for structure, semantics, and accessibility. Use Motion for movement and interaction mechanics, not as a replacement component kit.
- Avoid new hardcoded spring values when an appropriate Motion token or public contract already exists. If a distinct feel is genuinely needed, document whether it is app-specific or a reusable token gap.
- Keep documented exceptions to motion rules explicit. Do not introduce new timed/eased motion merely to simplify a migration.
- Keep animations interruptible and input responsive according to `docs/MOTION.md`. A gesture must have a clear owner, and a new command must not be blocked by an unrelated animation.
- Do not use `git submodule update --remote` as an unreviewed release/update mechanism. Pin a specific tested Motion commit and record the reason for changing the pointer.
- Do not claim device behavior is validated by CI. Physical-device checks remain deferred unless they become an explicit milestone/release requirement.

## Suggested phase structure

1. **Audit only:** produce the inventory, ownership classification, API fit/gap, and migration risks. No code changes.
2. **Agree on architecture:** review the proposed boundaries and any missing Motion API before implementation.
3. **First low-risk integration:** choose one small feature that maps directly to an existing API; migrate it in the application and verify that its visual identity remains unchanged.
4. **Reusable gaps:** for each genuinely generic gap, create a separate Motion proposal/PR, test it in Motion, then pin the resulting commit in the application.
5. **Remaining migrations:** proceed one feature at a time, with an app-side adapter where necessary.

The adoption plan is complete when every relevant candidate has an owner, a recommendation, a target API or documented gap, and verifiable acceptance criteria. Do not start implementation merely because the inventory is complete.
