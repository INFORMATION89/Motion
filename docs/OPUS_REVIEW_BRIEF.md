# Opus Review Brief — Shared AI Model Routing Policy

**Status:** Pending — no Opus review has been performed.
**Purpose:** Independent review of the four coordinated documentation PRs below. Do not merge or approve solely on the basis of this brief.

## Review package

| Repository | PR | Policy change commit inspected |
|---|---:|---|
| [Motion-UI](https://github.com/INFORMATION89/Motion-UI/pull/15) | #15 | `9c4b95700df910db09303082b9aa2868e412e7e5` |
| [Solstice](https://github.com/INFORMATION89/Solstice/pull/16) | #16 | `ad812b1579ea244a830cf6af6d782d61c4d9257d` |
| [Meridian-Engine](https://github.com/INFORMATION89/Meridian-Engine/pull/17) | #17 | `be85ef0ed58c2e09f8568de2540a2e7a381223f9` |
| [Smartspacer Plugins](https://github.com/INFORMATION89/infooo-Smartspacer-Plugins/pull/5) | #5 | `517aec13a213b9ed0f000196ecf81856fde8f6b3` |

The hashes above identify the policy-change commits inspected during this pass. The consolidated brief was subsequently added to Motion as a separate commit; inspect each PR's current head (including that brief commit) before conducting the independent review.

## Intended policy

1. **Sonnet / ChatGPT are interchangeable defaults for ordinary engineering tasks.** This means either may own everyday feature work, routine fixes, scoped refactors, tests, standard documentation, and fast iteration. Choose one primary agent based on repository/tool access, task fit, and available usage; do not duplicate the same implementation.
2. **Opus is an escalation/specialist option**, not a mandatory dependency. Use it for complex architecture and trade-offs, cross-repository/long-context reasoning, difficult concurrency or system bugs, stubborn failures, complex technical specifications, and high-value independent review.
3. **Plan with Opus, implement with Sonnet/ChatGPT** when the complexity warrants it. Capture durable architectural decisions in the repository's plan or decision documentation before switching agents.
4. **Explicit review state:** use the exact status `Opus review pending` when specialist review is warranted. Never imply that Opus reviewed or approved a change until an actual Opus review has occurred.
5. **Cost and access controls:** do not incur paid API usage, buy credits, upgrade plans, or silently switch billing without explicit owner approval. Model names, IDs, pricing, and entitlements must not be treated as permanent.
6. **Correctness remains evidence-based:** a model's approval is not proof. Verify substantive findings against code, official docs, reproducible tests, CI, and owner approval for strategic/product/privacy/security decisions.

## Review questions

### A. Cross-repository consistency
- Do all four repositories express the same default-versus-escalation boundary without contradictory wording?
- Is “interchangeable” correctly scoped to task suitability rather than claiming the models have identical capabilities?
- Are routine documentation and ordinary implementation clearly routed to Sonnet/ChatGPT, while Opus is reserved for work where deeper reasoning reduces risk or rework?
- Are the Opus triggers sufficiently concrete and not so broad that every multi-file change becomes an Opus task?

### B. Handoff and approval semantics
- Does every repository distinguish an Opus recommendation/pending review from a completed review?
- Can normal implementation proceed without waiting for Opus?
- Is a useful handoff defined (diff, task context, acceptance criteria, tests/evidence, constraints, unresolved questions)?
- Are product-owner approvals still required for strategic, privacy, security, legal, or irreversible decisions?

### C. Model/version and cost hygiene
- Are model names and IDs treated as time-sensitive, with current provider docs authoritative?
- Are API billing, subscription allowances, and credits correctly distinguished?
- Are there stale model-version references, contradictory model routing bullets, or dead/temporarily unavailable documentation links?
- Does the policy avoid implying that a stronger model automatically produces correct results?

### D. Repository-specific fit
- **Motion:** Is the shared policy appropriately detailed, and do `AGENTS.md` and the policy agree?
- **Solstice:** Does the AI policy supplement, rather than override, product/design owner approval and device/CI verification requirements?
- **Meridian:** Does the cost policy preserve scientific/forecast, privacy, security, and architectural review safeguards without making Opus a gate for routine work?
- **Smartspacer:** Does the root policy fit the plugin monorepo, preserve plugin independence/privacy constraints, and link to shared guidance in a way that will resolve after the coordinated PRs land?

## Known review findings already addressed by the primary agent

- Removed a contradictory Solstice rule that still hardcoded “Claude Opus 5.5” after the new generic escalation rule was added.
- Removed the duplicate Meridian standard-coding table row and replaced stale ChatGPT model-family labels with generic current-menu wording.
- Updated Motion's old version-specific Opus documentation URL to Anthropic's current model-selection guidance.
- Clarified Motion's review handoff as explicitly pending in both the shared policy and agent instructions.

Please independently verify these corrections against the current PR heads; do not accept this list as proof.

## CI / verification snapshot

- Motion PR #15: Build workflow reported **completed / success** on the reviewed head at the time of inspection. GitHub's mergeability field was false on the latest metadata response; determine whether this is a real conflict or transient/unknown status before merge.
- Solstice PR #16: Build workflow was **queued** at the time of inspection; result pending.
- Meridian PR #17: no PR-triggered workflow run was returned for the reviewed head. Confirm whether CI is intentionally absent/not applicable or whether checks failed to trigger.
- Smartspacer PR #5: Build plugin suite was **in progress** at the time of inspection.
- These are documentation-only changes; no code tests were run. Recheck live checks before making any merge recommendation.

## Requested Opus output

Return:
1. A concise overall verdict: approve as-is, approve with minor fixes, or request changes.
2. Findings grouped by severity (blocking, important, minor), with exact repository, file, line, and rationale.
3. Cross-repository contradictions or wording that could cause an AI coding agent to misroute work.
4. Any missing safeguards around cost, privacy, owner approval, or review state.
5. Recommended minimal patch list, if any.
6. A separate merge-readiness assessment for each PR, accounting for CI and branch conflicts.
7. Explicit confirmation of what you actually inspected and which checks you did or did not run.

**Do not merge the PRs.** The purpose of this pass is independent review and recommendations. Keep Opus review status pending until the review is complete and its findings have been evaluated.
