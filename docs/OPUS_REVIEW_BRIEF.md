# Opus Review Brief — Shared AI Model Routing Policy

**Status:** Pending — no Opus review has been performed.
**Planned review:** Saturday, November 28, 2026.
**Purpose:** Independent review of the four coordinated documentation PRs, plus a cross-repository CI optimization audit and a general documentation audit across all four repositories. Do not merge or approve solely on the basis of this brief.

## Review package

| Repository | PR | Policy change commit inspected |
|---|---:|---|
| [Motion-UI](https://github.com/INFORMATION89/Motion-UI/pull/15) | #15 | `9c4b95700df910db09303082b9aa2868e412e7e5` |
| [Solstice](https://github.com/INFORMATION89/Solstice/pull/16) | #16 | `ad812b1579ea244a830cf6af6d782d61c4d9257d` |
| [Meridian-Engine](https://github.com/INFORMATION89/Meridian-Engine/pull/17) | #17 | `be85ef0ed58c2e09f8568de2540a2e7a381223f9` |
| [Smartspacer Plugins](https://github.com/INFORMATION89/infooo-Smartspacer-Plugins/pull/5) | #5 | `517aec13a213b9ed0f000196ecf81856fde8f6b3` |

The hashes above identify the policy-change commits inspected during the initial pass. The consolidated brief was subsequently added to Motion as a separate commit; inspect each PR's current head (including the brief commit) before conducting the independent review.

## Part I — AI model-routing policy review

### Intended policy

1. **Sonnet / ChatGPT are interchangeable defaults for ordinary engineering tasks.** This means either may own everyday feature work, routine fixes, scoped refactors, tests, standard documentation, and fast iteration. Choose one primary agent based on repository/tool access, task fit, and available usage; do not duplicate the same implementation.
2. **Opus is an escalation/specialist option**, not a mandatory dependency. Use it for complex architecture and trade-offs, cross-repository/long-context reasoning, difficult concurrency or system bugs, stubborn failures, complex technical specifications, and high-value independent review.
3. **Plan with Opus, implement with Sonnet/ChatGPT** when the complexity warrants it. Capture durable architectural decisions in the repository's plan or decision documentation before switching agents.
4. **Explicit review state:** use the exact status `Opus review pending` when specialist review is warranted. Never imply that Opus reviewed or approved a change until an actual Opus review has occurred.
5. **Cost and access controls:** do not incur paid API usage, buy credits, upgrade plans, or silently switch billing without explicit owner approval. Model names, IDs, pricing, and entitlements must not be treated as permanent.
6. **Correctness remains evidence-based:** a model's approval is not proof. Verify substantive findings against code, official docs, reproducible tests, CI, and owner approval for strategic/product/privacy/security decisions.

### Review questions

#### A. Cross-repository consistency
- Do all four repositories express the same default-versus-escalation boundary without contradictory wording?
- Is “interchangeable” correctly scoped to task suitability rather than claiming the models have identical capabilities?
- Are routine documentation and ordinary implementation clearly routed to Sonnet/ChatGPT, while Opus is reserved for work where deeper reasoning reduces risk or rework?
- Are the Opus triggers sufficiently concrete and not so broad that every multi-file change becomes an Opus task?

#### B. Handoff and approval semantics
- Does every repository distinguish an Opus recommendation/pending review from a completed review?
- Can normal implementation proceed without waiting for Opus?
- Is a useful handoff defined (diff, task context, acceptance criteria, tests/evidence, constraints, unresolved questions)?
- Are product-owner approvals still required for strategic, privacy, security, legal, or irreversible decisions?

#### C. Model/version and cost hygiene
- Are model names and IDs treated as time-sensitive, with current provider docs authoritative?
- Are API billing, subscription allowances, and credits correctly distinguished?
- Are there stale model-version references, contradictory model routing bullets, or dead/temporarily unavailable documentation links?
- Does the policy avoid implying that a stronger model automatically produces correct results?

#### D. Repository-specific fit
- **Motion:** Is the shared policy appropriately detailed, and do `AGENTS.md` and the policy agree?
- **Solstice:** Does the AI policy supplement, rather than override, product/design owner approval and device/CI verification requirements?
- **Meridian:** Does the cost policy preserve scientific/forecast, privacy, security, and architectural review safeguards without making Opus a gate for routine work?
- **Smartspacer:** Does the root policy fit the plugin monorepo, preserve plugin independence/privacy constraints, and link to shared guidance in a way that will resolve after the coordinated PRs land?

### Known review findings already addressed by the primary agent

- Removed a contradictory Solstice rule that still hardcoded “Claude Opus 5.5” after the new generic escalation rule was added.
- Removed the duplicate Meridian standard-coding table row and replaced stale ChatGPT model-family labels with generic current-menu wording.
- Updated Motion's old version-specific Opus documentation URL to Anthropic's current model-selection guidance.
- Clarified Motion's review handoff as explicitly pending in both the shared policy and agent instructions.

Please independently verify these corrections against the current PR heads; do not accept this list as proof.

## Part II — Cross-repository CI optimization audit

Audit the CI configuration and workflows in **all four repositories**, not only files touched by the current PRs. Inspect workflow files, build scripts, dependency setup, test configuration, caches, artifacts, and recent run history where available.

Look for measurable, low-risk opportunities such as:

- Redundant workflow runs, duplicate jobs, unnecessary matrix combinations, and repeated dependency/build setup.
- Appropriate path filters and event filters so documentation-only changes do not run expensive unrelated build/test jobs, while required validation and security checks still run.
- Concurrency groups and cancellation of superseded runs, without cancelling release, deployment, or other critical workflows incorrectly.
- Correct and effective dependency/build caches, cache keys, restore fallbacks, and avoidance of stale or unsafe cache reuse.
- Job dependency graphs, parallelizable independent jobs, fail-fast behavior, and avoidable serialization.
- Checkout depth, artifact contents/retention, unnecessary uploads/downloads, and excessive log or artifact volume.
- Repeated setup steps that can be safely shared or consolidated; action versions, pinning strategy, permissions, secrets exposure, and least-privilege defaults.
- Test selection, build variants, emulator/device setup, and platform-specific checks: reduce waste, but do not silently drop meaningful platform coverage.
- Flaky or slow steps, avoidable network downloads, and opportunities for reproducible builds.

For every proposed optimization:
1. Identify the exact workflow/job/step and the current cost or latency problem; distinguish measured evidence from a hypothesis.
2. Explain expected benefit, trade-offs, and regression/security risks.
3. Prefer minimal changes and preserve required checks, branch protection compatibility, release safety, and platform coverage.
4. Validate changes with the relevant workflow or an equivalent reproducible local check. Compare before/after timing when run history permits.
5. Do not weaken tests, remove required checks, reduce security scanning, or hide failures merely to make CI faster/greener.
6. If evidence is insufficient or a change is risky, document it as a recommendation instead of applying it.

Return a per-repository findings table with priority, expected impact, confidence/evidence, proposed change, and validation result. Also identify shared patterns worth standardizing, but do not force identical CI where repository needs differ.

## Part III — General documentation audit and repair

Perform a broad documentation review across **all documentation in all four repositories**: README files, `AGENTS.md`, rules/policies, architecture and design docs, contribution/development guides, setup instructions, API/plugin documentation, examples, changelogs, security/privacy/licensing documentation, and relevant inline documentation. Do not limit the audit to the four policy PR diffs.

Check for:

- Incorrect, stale, contradictory, duplicated, incomplete, or misleading instructions.
- Broken links, invalid paths, references to renamed files, outdated screenshots/examples, stale commands, and unsupported version assumptions.
- Mismatches between documentation and actual code, build configuration, workflows, public APIs, plugin contracts, and repository structure.
- Missing prerequisites, setup/build/test/release steps, troubleshooting guidance, and clear ownership or source-of-truth declarations.
- Inconsistent terminology, naming, formatting, information architecture, and cross-repository references.
- AI-agent instructions that conflict, repeat unnecessarily, overreach, omit safety boundaries, or encourage agents to make unapproved product/security/privacy decisions.
- Claims about platform support, compatibility, performance, privacy, data collection, or features that are not supported by implementation or evidence.

Repository-specific invariants to preserve:
- **Meridian-Engine:** It must remain an independent, reusable weather engine, not a Solstice-only library; preserve the intended multi-platform consumption direction and privacy-first design. Do not move Solstice-specific lifestyle/personalized recommendations into Meridian.
- **Solstice:** Preserve its role as a weather app and keep app-specific presentation/product experiences documented at the app layer where appropriate.
- **Motion-UI:** Keep the UI library independent of Solstice; do not reintroduce product-specific dependencies or references outside explicitly justified integration guidance. Preserve its distinctive animated toolbar capabilities and configurable styling direction.
- **Smartspacer Plugins:** Preserve plugin independence, clear plugin contracts, and applicable privacy/security boundaries.

### Repair rules
- Fix objectively incorrect, stale, broken, or internally contradictory documentation when the intended truth can be verified from code/configuration or an authoritative source.
- Keep changes focused, consistent with each repository's purpose, and easy to review. Avoid wholesale rewrites for stylistic preference.
- Do not invent product behavior, platform guarantees, APIs, roadmap commitments, or privacy claims.
- Where product/architecture intent is ambiguous, record the question and recommendation for the owner rather than guessing.
- Prefer one canonical source for shared rules and link to it where appropriate; avoid brittle cross-repo links to unmerged branches.
- Validate commands, links, paths, code examples, and generated docs where feasible. Report anything that could not be verified.
- Make documentation repairs in reviewable commits/branches or dedicated PRs; do not silently merge them.

Deliver:
1. A repository-by-repository inventory of documentation areas inspected.
2. Findings grouped by severity and exact file/line, with evidence.
3. A list of verified fixes made, with commit/PR references.
4. A list of proposed changes deliberately not made because owner input or stronger evidence is needed.
5. A short cross-repository consistency map identifying canonical sources and references.

## CI / verification snapshot from the initial pass

This snapshot is historical, not a current status. Recheck the live PR heads and checks before making any recommendation.

- Motion PR #15: Build workflow reported completed / success on the reviewed head at the time of inspection. The latest brief commit did not yet have a confirmed workflow result in the previous pass.
- Solstice PR #16: Build workflow was queued at the time of inspection; result pending.
- Meridian PR #17: no PR-triggered workflow run was returned for the reviewed head. Confirm whether CI is intentionally absent/not applicable or whether checks failed to trigger.
- Smartspacer PR #5: Build plugin suite was in progress at the time of inspection.
- These policy changes are documentation-only; no code tests were run during the initial review. Recheck live checks.

## Requested Opus output

Return a structured report containing:

1. Overall verdict on the four AI-policy PRs: approve as-is, approve with minor fixes, or request changes.
2. Findings grouped by severity (blocking, important, minor), with exact repository, file, line, evidence, and rationale.
3. Cross-repository contradictions or wording that could cause an AI coding agent to misroute work.
4. Missing safeguards around cost, privacy, owner approval, security, or review state.
5. Per-repository CI audit: measured baseline where possible, optimization opportunities, risks, changes made, and validation results.
6. Per-repository documentation audit: inspected scope, severity-classified findings, verified corrections, and unresolved owner decisions.
7. A cross-repository consistency map and minimal recommended follow-up patch list.
8. Separate merge-readiness assessment for each policy PR, accounting for current CI and branch conflicts.
9. Explicit confirmation of what was actually inspected, what was changed, and which checks were or were not run.

**Do not merge the PRs or push broad unreviewed changes to their default branches.** Keep the four existing policy PRs in draft. Make any additional repairs in focused, reviewable commits/PRs. Keep Opus review status pending until the actual review is complete and its findings have been evaluated.
