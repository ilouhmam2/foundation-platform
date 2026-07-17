# CLAUDE.md - foundation-platform

This repository uses shared AI-agent instructions.

Before implementing or reviewing code, read:

- `AGENTS.md`
- `PRD.md`
- `docs/architecture.md`
- `docs/adr`
- `docs/guidelines`
- `ai/instructions/foundation-platform.md`

## Project summary

`foundation-platform` is a lightweight Spring Boot foundation for enterprise microservices.

It provides:

- Maven parent
- BOM
- Spring Boot starters
- API conventions
- security conventions
- logging conventions
- observability conventions
- data conventions
- mapping conventions
- integration conventions
- testing helpers
- Maven Archetype for generating hexagonal service projects

## Claude-specific workflow

When asked to implement a task:

1. Read the relevant prompt from `ai/prompts`.
2. Read the relevant skill from `ai/skills`.
3. Propose a short plan.
4. Implement incrementally.
5. Add tests.
6. Add documentation.
7. Review for over-engineering.

## Critical constraints

Do not create:

- Docker Compose
- Kubernetes manifests
- Helm charts
- GitLab CI pipelines
- Keycloak configuration
- service-specific generated clients inside foundation-platform
- a sample service inside foundation-platform (use foundation-archetype instead)

Keep feature composition dependency-driven.