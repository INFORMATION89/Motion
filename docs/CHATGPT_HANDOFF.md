# ChatGPT handoff — 2026-11-28

**From:** Claude Code session (Opus review plus a Sonnet doc sync), branch `claude/determined-goodall-rap2d4`.
**For:** ChatGPT / Codex. Pick the tool per the tool tiers in [AI usage policy](AI_USAGE_POLICY.md) §2:
- **H** (Haiku): mechanical, easily verified work. **F-eligible** (ChatGPT Free) when Haiku is not used.
- **S** (Sonnet or a balanced ChatGPT/Codex coding model): standard implementation. Interchangeable; pick one primary agent.
- **O** (Opus): keep with Opus, or mark **Opus review pending**. Never assign to H or F.
- **F** (ChatGPT Free): bounded docs, explainers, and PR text. Every session starts with [agent-guides/CHATGPT_FREE_STARTER.md](agent-guides/CHATGPT_FREE_STARTER.md).

Read [AGENTS.md](../AGENTS.md), [RULES.md](RULES.md), [CONTINUE.md](../CONTINUE.md) and [the Opus review](reviews/OPUS_REVIEW_2026-11-28.md) first. Finding IDs (A1, B4, …) refer to that review.

## Ground rules (repeat of the repository rules that matter here)

- Never name or reference consuming applications in this repository. Motion stays consumer-neutral.
- Do not merge, release, tag, or force-push. Keep implementation PRs in draft until exact-head CI is green.
- One coherent change per branch/PR. Don't stack unrelated work, which is how `fix/mot03b-pointer-injection-scope` went wrong.
- Docs-only changes need no local code tests. Report the exact SHA and CI run number for anything you claim passed. Device, TalkBack, and haptics stay **NOT TESTED**.
- Spend at most one full CI run per finished batch. If a job fails, rerun only that job.

## State on 2026-11-28

- `main` @ `51420fd`: MOT0.3A and the MOT0.3B toolbar contract are merged, and CI passed (run #202). Status docs were refreshed on `claude/determined-goodall-rap2d4`, which is not merged yet.
- PR #26 (toolbar prototype, draft): red. The one-line test fix is `956f044`.
- PR #15 (AI routing policy, draft): based on an old `main`; needs a rebase. Its Opus review is still pending.
- `fix/mot03b-pointer-injection-scope`: mixes the #26 test fix, a Motion Core `AmbientClock` change, and five research docs. Needs splitting.

## Tier H tasks (Haiku; F-eligible when Haiku is not used)

| ID | Task | Done when |
|---|---|---|
| H1 | After S1 lands, update the PR #26 description's "Verification status" with the new head SHA and CI run number/result. Remove the sentence saying the fix is "not merged into this PR". | The PR body matches the latest run, and no claim is unverified. |
| H2 | Update PR #15's description: add "Needs rebase onto `main` @ `51420fd` (or later); branch predates the consumer-neutral cleanup" and keep **Opus review pending**. | The description is updated; no code or doc changes on the branch. |
| H3 | After each PR in this list merges, refresh `CONTINUE.md` "Verified project state" and "Next steps" (SHA, PR number, CI run). Edit only facts you checked. | `CONTINUE.md` matches GitHub. |
| H4 | **Not F-eligible** (Kotlin file; needs Spotless). In the new AmbientClock PR (S2), add one KDoc sentence to `rememberAmbientClock`: "`running` must read Compose snapshot state; it is observed, not polled." (finding A3) | The KDoc is present and Spotless is clean. |
| H5 | In the new AmbientClock PR (S2), update the status line of `docs/MOTION_RUNTIME_DESIGN_NOTES.md` from "not yet built or run" to the actual verified state (A4). | Status is accurate, with a SHA and run number. |
| H6 | Once the owner answers Q2, align the README line 7 component list with `docs/FRAMEWORK_OVERVIEW.md` (currently "page frames, cards, sheets…" vs. toolbar-first). | One-line edit matching the owner's answer. |

## Tier S tasks (Sonnet or balanced ChatGPT/Codex model)

| ID | Task | Notes |
|---|---|---|
| S1 | Cherry-pick **only** `956f044` onto `feat/mot03b-toolbar-prototype` and push. Run CI once. | Finding B0. Don't merge the whole fix branch. |
| S2 | Recreate the AmbientClock change as its own PR from `main`. Include only `AmbientClock.kt`, the design notes, and the tests. Fix the cadence (A1). Move the tests to `motion/src/test` with JUnit 5 and add the 60 Hz/120 Hz cadence tests (A1, A2). Drop the new `:motion` androidTest dependencies. | Core behavior change: needs **Opus re-review** and **owner approval** before merge. |
| S3 | Put the five research/audit docs from the fix branch in a separate docs-only PR: `MOTION_ARCHITECTURE_REVIEW.md`, `MOTION_RUNTIME_AND_TEST_AUDIT.md`, `REFERENCE_MATERIAL3_EXPRESSIVE.md`, plus their `CONTINUE.md`/spike-doc edits. Scrub any consumer names. | Docs only, no code CI needed. |
| S4 | Rebase PR #15 onto current `main`. Re-apply only its policy edits, against the consumer-neutral `AGENTS.md` and `AI_USAGE_POLICY.md`. Don't bring back the old adoption-guide link, the old consumer references, or the "planned `:motion-ui`" wording. | The owner decides whether `docs/OPUS_REVIEW_BRIEF.md` stays in this repo (Q1). |
| S5 | In PR #26, apply B4: commit selection from `settledPage`. Also apply the small B6 fixes: toggleable semantics on the context action, reduced motion via Core's `rememberReducedMotion()` passed in as a parameter, a disabled tab in the geometry tests, and no throwing on zero-size bounds. | Keep within the Lab module. No public API. |
| S6 | After owner approval (Q3), add a path filter so `**/*.md`-only changes skip the emulator job. Check branch protection first. | Finding B5. |
| S7 | After S1 and S5, resolve doc conflicts between PR #26 and the refreshed `main` docs. Keep only toolbar-specific doc edits in #26. | Finding B7. |

## Tier O tasks (Opus): do not start without Opus or explicit owner go-ahead

| ID | Task | Notes |
|---|---|---|
| O1 | PR #15 policy review, plus the cross-repository CI and documentation audits in its brief. Still **Opus review pending**. | Likely partly superseded by the AI usage policy port on this branch. The owner decides whether to close #15. |
| O2 | Re-review S2 after the cadence fix. | Core behavior change. |
| O3 | Public toolbar API design, including B1 (settle hand-off without snap-back) and B3 (same-pass measure and indicator placement), after S1, S5 and the owner decisions below. | |

## Tier F tasks (ChatGPT Free, starter guide required)

| ID | Task | Done when |
|---|---|---|
| F1 | Write a plain-language explainer for the owner of `docs/reviews/OPUS_REVIEW_2026-11-28.md`. Check every claim against the review; add nothing it does not say. | The explainer is accurate and every statement traces to the review. |
| F2 | Draft PR descriptions and handoffs for the PRs in this list in the `.github/pull_request_template.md` format. | Each section is filled in with verified facts only. |
| F3 | Clarity review of the AI usage policy changes (`docs/AI_USAGE_POLICY.md`, `docs/agent-guides/`). Wording only. | Suggested wording fixes; no change of meaning. |

## Owner questions (don't guess)

1. **Q1.** Should `docs/OPUS_REVIEW_BRIEF.md`, which names other repositories, live in this consumer-neutral repository, or somewhere shared or private?
2. **Q2.** Is Motion UI's lead component story "toolbar first" (`FRAMEWORK_OVERVIEW.md`) or "page frames, cards, sheets…" (README)?
3. **Q3.** Approve the API 35 emulator job as a required check, and a docs-only path filter for it?
4. **Q4** (B2). Must the dragged selection surface follow the pointer continuously, with lift and press, or is the tab-to-tab snapping acceptable? (If acceptable, the contract text needs amending.)
5. **Q5** (B6). On a programmatic jump to a far tab, should the indicator sweep through the intermediate tabs or travel directly?
6. **Q6.** Should a PR-hygiene CI check be added (required template sections, no `[skip ci]` on code commits)? A CI change needs owner approval.
