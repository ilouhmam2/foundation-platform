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
- Publishable Maven artifacts (single JAR or multiple JARs, depending on module boundaries)
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
- Maven Archetype for generating hexagonal service projects

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
- Lombok
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

### Rule 4 - Publishable Artifacts and Repository Distribution

`foundation-platform` must be consumable as published Maven artifacts.

The platform may be consumed as:

- one aggregated deliverable when appropriate for the target use case
- multiple modular JARs following capability boundaries

Artifacts must be publishable to a Maven-compatible repository manager:

- Nexus
- Artifactory
- or equivalent Maven repository

The concrete target in CI is the **GitLab Package Registry** of this project, configured via `distributionManagement` in three POMs:

- `pom.xml` (root aggregator)
- `foundation-parent/pom.xml`
- `foundation-bom/pom.xml`

All three must use the same repository id `gitlab-maven` and the URL pattern:
```
${env.CI_API_V4_URL}/projects/${env.CI_PROJECT_ID}/packages/maven
```

Credentials must never be hardcoded in versioned files. CI uses `CI_JOB_TOKEN` injected automatically by GitLab. Developer machines use a Deploy Token stored in `~/.m2/settings.xml` only.

### Rule 5 - No Sample Service in the Socle

`foundation-sample-service` is **not** part of `foundation-platform`.

Do not introduce it.

Consuming services are generated using `foundation-archetype` and live in separate repositories.

The archetype must always:

- include the 6 mandatory capabilities (core, api, logging, observability, mapping, test)
- allow optional capabilities to be selected via `-Dcapabilities` at generation time, including `security`
- generate a hexagonal package structure (domain / application / infrastructure)
- generate a minimal `application.yml` containing only the blocks for the selected capabilities

### Rule 6 - No Deployment Artifacts

Do not create:

- Kubernetes manifests
- Helm charts
- GitLab CI pipelines
- Docker Compose files

Dockerfile belongs to the consuming microservice, not to the foundation runtime.

> **Archetype exception**: `foundation-archetype` may include a `Dockerfile` template and a `.gitlab-ci.yml` template in its `archetype-resources/`. These files are generated inside the consuming service project (outside this repository), not inside the foundation-platform repository itself.

> **Socle CI exception**: `foundation-platform` maintains its own `.gitlab-ci.yml` at the repository root. This pipeline builds, tests, and publishes the foundation artifacts to the GitLab Package Registry. It is the CI pipeline of the foundation itself, not a deployment artifact.

### Rule 7 - No IDP-Specific Implementation

Do not use Keycloak adapters.

Use Spring Security OAuth2 Resource Server.

The platform must support any OIDC-compliant IDP through `issuer-uri` or `jwk-set-uri`.

### Rule 8 - Generated Clients Belong to Consuming Services

Do not place service-specific generated REST or SOAP clients inside `foundation-platform`.

The foundation provides:

- Generation guidelines
- Plugin management
- Templates
- Runtime support

The consuming service owns the generated client modules.

### Rule 9 - Maven Publication

Publication to the Maven repository follows this workflow:

**Local development** — install to `~/.m2/repository`:

```bash
mvn clean install
```

**CI/CD — GitLab Package Registry** (authenticated via `CI_JOB_TOKEN`):

```bash
mvn -B clean deploy -DskipTests=true --settings ci-settings.xml
```

`ci-settings.xml` is versioned in this repository without credentials. It references `${env.CI_JOB_TOKEN}`, which is injected automatically by GitLab CI in every job. No manual variable configuration is needed.

For publication from a developer machine, configure a Deploy Token in `~/.m2/settings.xml` (never commit it).

### Rule 10 - Versioning Governance

Version changes follow a snapshot/release cycle:

1. Decide the next version number (major.minor.patch) as a team.
2. Bump version: `mvn versions:set -DnewVersion=X.Y.Z -DgenerateBackupPoms=false`
3. Commit and tag: `git commit -m "release: prepare X.Y.Z"` then `git tag vX.Y.Z`
4. Push the tag — the `publish-release` CI job triggers automatically.
5. Return to snapshot: `mvn versions:set -DnewVersion=X.Y.Z+1-SNAPSHOT -DgenerateBackupPoms=false`

Do not manually edit version numbers in POM files. Always use `mvn versions:set`.

The CI pipeline provides two publish stages:

- `publish-snapshot` — triggers on `main` or `develop` branches
- `publish-release` — triggers only on protected tags matching `vX.Y.Z`

### Rule 11 - MR Traceability

Every merge request that changes a behavior or a platform principle must also update the relevant documentation.

The following documents are source of truth and must stay consistent:

- `AGENTS.md`
- `PRD.md`
- `README.md`
- `docs/architecture.md`
- `docs/guidelines/*`
- `ai/instructions/*`
- `ai/prompts/*`
- `ROADMAP.md`

A MR that modifies code without updating documentation when documentation is impacted must be rejected.

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

**Local validation:**

```bash
mvn clean install
```

**Module-level validation:**

```bash
mvn -pl {module} -am clean verify
```

**CI publication (requires GitLab CI context):**

```bash
mvn -B clean deploy -DskipTests=true --settings ci-settings.xml
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