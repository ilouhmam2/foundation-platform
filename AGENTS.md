# AGENTS.md - foundation-platform

## Purpose

This file provides instructions for AI coding agents working on `foundation-platform`.

It is intended to be tool-agnostic and should be followed by GitHub Copilot, Claude Code, Cursor, Codex, Aider, Windsurf or any other coding agent.

---

## Project Summary

`foundation-platform` is a lightweight Spring Boot foundation for enterprise microservices.

It provides:

- Maven parent
- Dependency BOM
- Spring Boot starters
- API standards
- Security standards
- Logging standards
- Observability standards
- Data and Flyway standards
- MapStruct standards
- NATS standards
- HTTP client standards
- SOAP client standards
- Testing helpers

---

## Target Stack

- Java 21
- Spring Boot 4.1.x
- Maven
- Gravitee API Gateway
- PostgreSQL
- Flyway
- NATS
- OAuth2 Resource Server / JWT
- MapStruct
- OpenAPI Generator
- Apache CXF
- Actuator
- Micrometer
- OpenTelemetry

---

## Core Rules

### Rule 1 - Keep the Framework Lightweight

Do not create a heavy custom framework.

Prefer Spring Boot-native mechanisms.

Do not hide standard Spring Boot behavior behind unnecessary abstractions.

### Rule 2 - Dependency-Driven Composition

Capabilities are added through explicit dependencies.

Do not create a giant configuration model with many `enabled=true/false` flags.

Properties are for configuration, not for primary feature activation.

### Rule 3 - Parent POM Must Not Force Runtime Features

`foundation-parent` must define:

- Java version
- Plugin management
- Dependency management
- Compiler configuration
- Testing configuration
- Code quality configuration

It must not force every service to use:

- JPA
- Flyway
- NATS
- SOAP
- WebClient
- PostgreSQL

### Rule 4 - No Deployment Artifacts

Do not create:

- Kubernetes manifests
- Helm charts
- GitLab CI pipelines
- Docker Compose files

Dockerfile belongs to the consuming microservice, not to the foundation runtime.

### Rule 5 - No IDP-Specific Implementation

Do not use Keycloak adapters.

Use Spring Security OAuth2 Resource Server.

The platform must support any OIDC-compliant IDP through `issuer-uri` or `jwk-set-uri`.

### Rule 6 - Generated Clients Belong to Consuming Services

Do not place service-specific generated REST or SOAP clients inside `foundation-platform`.

The foundation provides:

- Generation guidelines
- Plugin management
- Templates
- Runtime support

The consuming service owns the generated client modules.

---

## Module Implementation Rules

Every starter must:

- Provide auto-configuration
- Use conditional configuration
- Allow bean override by consuming services
- Keep dependencies minimal
- Include tests
- Include a README or usage section
- Avoid business-specific code

---

## Coding Rules

- Use Java 21
- Use constructor injection
- Avoid field injection
- Keep code explicit
- Avoid unnecessary inheritance
- Avoid static helpers unless justified
- Prefer immutable DTOs where practical
- Never expose JPA entities directly in REST APIs
- Use DTOs and MapStruct
- Use Flyway for schema migrations
- Never use `ddl-auto=update`
- Propagate `X-Correlation-Id`
- Do not log sensitive data
- Use OAuth2 Resource Server for JWT validation

---

## Before Implementing a Task

The agent must:

1. Read `AGENTS.md` (this file)
2. Read `docs/architecture.md`
3. Read the relevant ADR(s) in `docs/adr/`
4. Read the relevant guideline in `docs/guidelines/`
5. Read `ai/instructions/foundation-platform.md` for execution protocol
6. Propose a short implementation plan (3-5 bullet points)
7. Implement only the requested scope
8. Add or update tests
9. Add or update documentation
10. Review the implementation for over-engineering

**Do not read all files at once.** Read only the guideline and ADR that are relevant to the current module.

---

## Validation

When possible, run:

```bash
mvn clean verify
```

---

## AI Tooling Structure

This repository provides AI assistance resources that work with any tool.

```
AGENTS.md                          ← universal entry point (any AI tool)
CLAUDE.md                          ← Claude Code entry point
.github/copilot-instructions.md    ← VS Code Copilot auto-loaded context

.github/agents/                    ← VS Code Copilot agent modes
  engineer.agent.md                ← implement modules
  architect.agent.md               ← review architecture
  reviewer.agent.md                ← full code/module review
  test-engineer.agent.md           ← write and review tests

.github/prompts/                   ← VS Code Copilot reusable prompts (/ commands)
  implement-starter.prompt.md      ← generic starter implementation template
  review-module.prompt.md          ← module review checklist

ai/instructions/                   ← stable context (any tool)
  foundation-platform.md           ← main execution protocol
  review-rules.md                  ← review standards
  testing-rules.md                 ← test standards

ai/prompts/                        ← module-specific task files
  00-bootstrap-repo.prompt.md      ← bootstrap Maven structure
  01-create-parent-bom.prompt.md   ← implement parent + BOM
  04-create-data-starter.prompt.md ← implement data starter

ai/skills/                         ← reference knowledge (any tool)
  maven-multimodule-design/SKILL.md
  spring-boot-starter-design/SKILL.md
  openapi-client-generation/SKILL.md
  wsdl-client-generation/SKILL.md

docs/examples/                     ← concrete usage examples for consuming services
  consuming-service-pom.xml        ← starter kit POM
  service-with-data.md
  service-with-nats.md
  service-with-openapi-client.md
  service-with-wsdl-client.md
```

**For VS Code Copilot**: use agent modes via the agent picker and prompts via `/` commands.

**For other tools** (Claude Code, Cursor, Aider, etc.): reference `AGENTS.md` as the primary instruction file and paste relevant `ai/` files into context as needed.