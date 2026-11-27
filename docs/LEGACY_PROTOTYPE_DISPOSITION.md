# Legacy prototype disposition

**Decision date:** 2026-11-26  
**Disposition:** removed from the active Motion repository; no source was carried forward.

## What was reviewed

The former `lab/` directory was not a Gradle module and was not included in the standalone build or required CI. Its seven Kotlin files were an application-integrated prototype rather than an independent Motion sample.

The reviewed source files carried GNU Lesser General Public License version 3 notices and imported application-specific packages, resources, theme classes, activities, icons, and UI components. They could not be treated as Apache-2.0 Motion source or safely moved by renaming packages.

## Decision

- Remove the unbuilt `lab/` source tree rather than preserve it as an apparent part of the reusable framework.
- Do not copy or mechanically port its implementation into `:motion-lab` or `:motion-ui`.
- Keep the independently maintained `:motion-lab` as the only sample application in the active repository.
- Reimplement any genuinely reusable behavior from independently written requirements and the public Motion API.
- If future work proposes reusing external source, perform a separate provenance, copyright, and license review before copying or adapting it.

No legacy implementation code is retained by this cleanup. The existing Motion Core and Motion Lab source remain under the repository's Apache-2.0 licensing.
