# Prompt - 00 Bootstrap Repository

## Role

You are a senior Spring Boot platform engineer.

You are helping implement `foundation-platform`, a lightweight enterprise Spring Boot foundation.

## Mandatory reading

Before producing files, read:

- `AGENTS.md`
- `docs/architecture.md`
- `docs/adr/ADR-001-java-21.md`
- `docs/adr/ADR-002-spring-boot-4.md`
- `docs/adr/ADR-003-dependency-driven-composition.md`
- `docs/adr/ADR-004-no-deployment-in-foundation.md`
- `docs/guidelines/module-guidelines.md`
- `docs/guidelines/coding-guidelines.md`
- `ai/skills/maven-multimodule-design/SKILL.md`

## Objective

Create the initial Maven multi-module structure for `foundation-platform`.

This task must only bootstrap the repository structure.

Do not implement business logic.

Do not implement starter Java code yet.

## Required modules

Create the following Maven modules:

```text
foundation-parent
foundation-bom
foundation-common
foundation-core-starter
foundation-api-starter
foundation-security-starter
foundation-logging-starter
foundation-observability-starter
foundation-mapping-starter
foundation-data-starter
foundation-nats-starter
foundation-http-client-starter
foundation-soap-client-starter
foundation-test-starter
foundation-demo-service
```