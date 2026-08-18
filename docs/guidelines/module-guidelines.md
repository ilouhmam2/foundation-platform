# Module Guidelines - foundation-platform

## 1. Purpose

This document defines how modules must be designed inside `foundation-platform`.

The objective is to keep the foundation:

- lightweight
- modular
- close to Spring Boot standards
- easy to understand
- easy to override
- easy to consume by microservices

`foundation-platform` must not become a heavy internal framework.

---

## 2. Module categories

The repository is organized around the following module types:

```text
foundation-parent
foundation-bom
foundation-common
foundation-*-starter
foundation-test-starter
foundation-archetype
```

---

## 3. foundation-parent

Defines build conventions only.

Must contain:
- Java 21 compiler configuration
- Maven plugin management (compiler, surefire, failsafe, resources)
- Code quality plugin configuration

Must not contain:
- Runtime Java dependencies (JPA, Flyway, NATS, SOAP, WebClient, PostgreSQL)
- Spring Boot starter dependencies

---

## 4. foundation-bom

Manages dependency versions only.

Must contain:
- Spring Boot import BOM
- Third-party library versions (MapStruct, NATS, CXF, etc.)
- foundation-platform module versions

Must not contain:
- Plugin definitions (those belong in foundation-parent)

---

## 5. foundation-common

Provides shared low-level utilities used across foundation modules.

Examples:
- Correlation ID constants
- Base exception types
- Shared interfaces

Must not contain:
- Business logic
- Spring auto-configuration
- External framework coupling

---

## 6. Starters

Each starter provides exactly one technical capability.

Every starter must include:
- Auto-configuration class annotated with `@AutoConfiguration`
- Beans annotated with `@ConditionalOnClass` and/or `@ConditionalOnMissingBean`
- Typed configuration properties with `@ConfigurationProperties` (when needed)
- `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`
- Unit and auto-configuration tests
- README with configuration reference and usage example

Must not contain:
- Business logic
- Hardcoded IDP references
- Service-specific generated clients

---

## 7. foundation-test-starter

Provides test utilities and infrastructure helpers.

Must be declared with `<scope>test</scope>` in consuming modules.

Must not add runtime dependencies to the production classpath.

---

## 8. foundation-archetype

Provides a Maven Archetype for generating new hexagonal microservice projects.

Consuming teams generate services outside this repository using:

```bash
mvn archetype:generate \
  -DarchetypeGroupId=fr.francetv.foundation \
  -DarchetypeArtifactId=foundation-archetype \
  -DarchetypeVersion=${foundation.version} \
  -Dcapabilities=data,nats
```

Must:
- Always include the 6 mandatory starters (core, api, logging, observability, mapping, test)
- Allow optional capabilities to be selected via `-Dcapabilities` at generation time, including `security`
- Generate a hexagonal (ports-and-adapters) package structure
- Generate a minimal `application.yml` with only the blocks for the selected capabilities
- Generate a POM that imports `foundation-bom`

Must not:
- Contain business logic or business entities
- Generate deployment artifacts inside this repository
- Force optional starters when the capability was not selected
- Be used as a sample or demo service inside the socle

---

## 9. Maven publication

Artifacts from this repository are published to the **GitLab Package Registry**.

`distributionManagement` must be declared in three POMs because Maven inheritance does not cross parent boundaries:

| POM | Reason |
|---|---|
| `pom.xml` | Root aggregator publish target |
| `foundation-parent/pom.xml` | Does not inherit from the root aggregator |
| `foundation-bom/pom.xml` | Has no Maven parent |

All three must use the same repository id `gitlab-maven` and the URL:
```
${env.CI_API_V4_URL}/projects/${env.CI_PROJECT_ID}/packages/maven
```

Credentials must never appear in versioned files. `ci-settings.xml` is versioned but contains only `${env.CI_JOB_TOKEN}` (injected automatically by GitLab CI).

**Local install:**

```bash
mvn clean install
```

**CI deploy:**

```bash
mvn -B clean deploy -DskipTests=true --settings ci-settings.xml
```

---

## 10. Versioning governance

Version numbers are managed exclusively via `mvn versions:set`. Direct POM edits are forbidden.

```bash
# Release
mvn versions:set -DnewVersion=X.Y.Z -DgenerateBackupPoms=false

# Return to SNAPSHOT
mvn versions:set -DnewVersion=X.Y.Z+1-SNAPSHOT -DgenerateBackupPoms=false
```

The CI pipeline automatically publishes:

- snapshots on `main` and `develop` branch merges (`publish-snapshot`)
- releases on protected tags matching `vX.Y.Z` (`publish-release`)

---

## 11. MR traceability

Every MR that changes a behavior or a platform principle must also update the relevant documentation.

Source-of-truth documents:

- `AGENTS.md`
- `PRD.md`
- `README.md`
- `docs/architecture.md`
- `docs/guidelines/*`
- `ai/instructions/*`
- `ai/prompts/*`
- `ROADMAP.md`

A MR that modifies code without updating documentation is not compliant with this rule and must be rejected.