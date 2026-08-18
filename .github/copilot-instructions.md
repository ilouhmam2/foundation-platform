# GitHub Copilot Instructions

Follow the shared repository instructions in:

- `AGENTS.md`
- `docs/architecture.md`
- `docs/adr`
- `docs/guidelines`
- `ai/instructions/foundation-platform.md`

Do not duplicate rules here.

If there is a conflict, follow this priority:

1. Explicit user request
2. ADRs
3. `AGENTS.md`
4. `ai/instructions/foundation-platform.md`
5. `docs/guidelines`
6. This file

Important rules:

- Do not create deployment artifacts.
- Do not add Docker Compose.
- Do not add Kubernetes manifests.
- Do not add Helm charts.
- Keep composition dependency-driven.
- Do not use properties as the primary feature activation mechanism.
- Keep archetype generation outside this repository (generated services are external consumers).
- Treat security as an optional archetype capability, not a mandatory one.
- Keep artifacts publishable to Maven repositories (Nexus/Artifactory/equivalent).
- Keep the foundation close to Spring Boot.