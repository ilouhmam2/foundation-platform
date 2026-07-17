# Architecture - foundation-platform

## 1. Purpose

`foundation-platform` provides reusable technical capabilities for Spring Boot microservices.

Its objectives are to:

- accelerate delivery of new services
- standardize technical implementation
- reduce duplicated infrastructure code
- improve consistency across teams
- remain aligned with standard Spring Boot practices

`foundation-platform` is intentionally lightweight.

It is a foundation, not a custom application framework.

---

## 2. Architectural vision

The platform provides reusable technical capabilities through Spring Boot starters.

Application teams build business services by selecting only the capabilities they need.

The architecture follows:

- Spring Boot conventions first
- dependency-driven composition
- modular design
- explicit configuration
- low coupling
- high testability

---

## 3. Core principles

### Lightweight foundation

The platform must remain understandable by any experienced Spring Boot developer.

Prefer:

- Spring Boot auto-configuration
- starter-based capabilities
- typed configuration properties
- conditional beans
- standard Spring abstractions

Avoid:

- custom runtime frameworks
- hidden magic
- excessive abstraction layers
- reflection-heavy implementations

### Dependency-driven composition

Capabilities are activated through dependencies.

Example:

```xml
<dependency>
    <groupId>fr.francetv.foundation</groupId>
    <artifactId>foundation-security-starter</artifactId>
</dependency>
```

A dependency enables a capability.

Properties customize a capability.

Properties should not be the primary activation mechanism.

### Single responsibility

Every module should have one clear purpose.

A starter should provide one technical capability.

---

## 4. Repository structure

```text
foundation-platform
│
├── foundation-parent
├── foundation-bom
│
├── foundation-common
│
├── foundation-core-starter
├── foundation-api-starter
├── foundation-security-starter
├── foundation-logging-starter
├── foundation-observability-starter
├── foundation-mapping-starter
├── foundation-data-starter
├── foundation-nats-starter
├── foundation-http-client-starter
├── foundation-soap-client-starter
│
├── foundation-test-starter
│
└── foundation-archetype
```

---

## 5. Responsibility model

The platform follows strict responsibility boundaries.

```text
foundation-parent
    Build standards

foundation-bom
    Dependency versions

foundation-common
    Shared foundation utilities

foundation-*-starter
    Reusable runtime capabilities

foundation-archetype
    Maven archetype — generates hexagonal service projects
    with mandatory and optional capabilities selected at generation time

consuming services  (generated outside this repository)
    Business logic
    Domain model
    API implementation
    Database schema
    Generated clients

deployment platform
    Docker
    Helm
    Kubernetes
    GitLab CI
```

---

## 6. Parent and BOM strategy

### foundation-parent

The parent module defines:

- Java version
- compiler configuration
- Maven plugin configuration
- code quality standards
- testing standards

The parent must not introduce runtime dependencies.

### foundation-bom

The BOM manages:

- Spring Boot dependencies
- framework versions
- shared library versions

All service modules should import the BOM.

---

## 7. Starter architecture

Each starter must provide one technical capability.

Examples:

```text
foundation-security-starter
    Security capability

foundation-observability-starter
    Metrics and tracing capability

foundation-nats-starter
    Messaging capability

foundation-data-starter
    Data access capability
```

Starters may provide:

- auto-configuration
- default beans
- configuration properties
- extension points
- documentation
- tests

Starters must not provide:

- business logic
- business entities
- business controllers
- generated clients
- deployment artifacts

---

## 8. Auto-configuration model

Auto-configuration should be Spring Boot native.

Recommended annotations:

```java
@AutoConfiguration
@EnableConfigurationProperties
@ConditionalOnClass
@ConditionalOnMissingBean
```

Rules:

- create beans only when required
- allow bean overrides
- provide sensible defaults
- avoid surprising behavior

---

## 9. Security architecture

The platform provides security capabilities without coupling to a specific Identity Provider.

Typical flow:

```text
Consumer
    ->
API Gateway
    ->
Spring Boot Service
```

Supported model:

- OAuth2 Resource Server
- JWT token validation

Allowed:

- issuer-uri configuration
- jwk-set-uri configuration

Forbidden:

- Keycloak adapters
- provider-specific integrations
- embedded user management

---

## 10. Data architecture

The default relational database is:

```text
PostgreSQL
```

Data access standards:

- Spring Data JPA
- Flyway migrations
- transactional service layer
- explicit repositories

Rules:

- Flyway manages schema changes
- ddl-auto=update is forbidden
- ddl-auto=validate is preferred
- entities must not be exposed through public APIs

Schema ownership remains with consuming services.

---

## 11. Messaging architecture

Messaging is based on NATS.

The platform provides:

- connection configuration
- publisher support
- consumer support
- serialization conventions
- correlation ID propagation

The platform standardizes:

- envelope format
- retry strategy guidance
- DLQ guidance
- idempotence guidance

Business events remain service-owned.

Example:

```text
quote.created
quote.updated
policy.issued
```

---

## 12. Integration architecture

### REST integrations

Generated REST clients belong to consuming services.

Generation sources may include:

- OpenAPI
- Swagger

The platform provides:

- dependency management
- generation templates
- WebClient support
- authentication support
- timeout support
- error mapping conventions

### SOAP integrations

Generated SOAP clients belong to consuming services.

The platform provides:

- Apache CXF support
- generation templates
- authentication support
- timeout support
- error mapping conventions

Generated code must never be committed into the foundation repository.

---

## 13. Mapping architecture

MapStruct is the standard mapping technology.

The platform may provide:

- dependency management
- annotation processor configuration
- shared mapper conventions

Business mappings remain within consuming services.

Typical mappings:

```text
DTO -> Domain
Domain -> DTO

Entity -> Domain
Domain -> Entity

Generated Client -> Domain
Domain -> Generated Client
```

---

## 14. Observability architecture

Observability includes:

- health checks
- readiness checks
- liveness checks
- metrics
- tracing
- correlation IDs

Technologies:

- Spring Boot Actuator
- Micrometer
- OpenTelemetry

Required endpoints:

```text
/actuator/health
/actuator/health/liveness
/actuator/health/readiness
/actuator/metrics
/actuator/prometheus
```

Logging is handled separately by `foundation-logging-starter`.

---

## 15. Testing strategy

The platform promotes automated testing.

Required testing levels:

```text
Unit Tests
Auto-Configuration Tests
Integration Tests
```

Recommended tools:

```text
JUnit 5
AssertJ
Testcontainers
WireMock
Spring Boot Test
```

Starters should verify:

- bean creation
- bean override behavior
- property binding
- conditional configuration behavior

---

## 16. Deployment boundary

Deployment is explicitly outside the scope of the foundation.

The repository must not contain:

- Docker Compose
- Kubernetes manifests
- Helm charts
- GitLab CI pipelines
- ArgoCD manifests

Deployment concerns belong to platform engineering teams.

> **Archetype exception**: `foundation-archetype` may generate a minimal `Dockerfile` and `.gitlab-ci.yml` inside the target consuming service project. These files belong to the generated service, not to the foundation-platform repository.

---

## 17. Archetype architecture

`foundation-archetype` is a Maven Archetype that generates a ready-to-use microservice project.

Generated services live outside this repository.

### Generation parameters

| Parameter | Required | Default | Description |
|---|---|---|---|
| `groupId` | yes | — | Maven groupId (e.g. `fr.francetv.myteam`) |
| `artifactId` | yes | — | Maven artifactId and project directory name (e.g. `my-service`) |
| `version` | yes | `0.0.1-SNAPSHOT` | Maven version |
| `serviceName` | yes | — | PascalCase Java class name prefix (e.g. `MyService` → `MyServiceApplication.java`) |
| `capabilities` | no | _(none)_ | Comma-separated optional capabilities: `data`, `nats`, `http-client`, `soap-client` |
| `generateDockerfile` | no | `true` | Generate a minimal `Dockerfile` for the service |
| `generateGitlabCi` | no | `true` | Generate a minimal `.gitlab-ci.yml` for the service |

### Mandatory capabilities

Every generated project automatically includes:

| Starter | Purpose |
|---|---|
| `foundation-core-starter` | Correlation ID propagation |
| `foundation-api-starter` | REST API conventions, error handling |
| `foundation-security-starter` | OAuth2 Resource Server JWT |
| `foundation-logging-starter` | Structured JSON logging |
| `foundation-observability-starter` | Actuator, Micrometer, OpenTelemetry |
| `foundation-mapping-starter` | MapStruct configuration |
| `foundation-test-starter` | Testing helpers |

### Optional capabilities

Teams select optional capabilities using `-Dcapabilities` at generation time:

| Key | Starter | Adds |
|---|---|---|
| `data` | `foundation-data-starter` | JPA, Flyway, PostgreSQL |
| `nats` | `foundation-nats-starter` | NATS messaging |
| `http-client` | `foundation-http-client-starter` | WebClient, OpenAPI REST clients |
| `soap-client` | `foundation-soap-client-starter` | Apache CXF, WSDL SOAP clients |

### Hexagonal architecture

Every generated service follows a ports-and-adapters (hexagonal) package structure.

```text
fr.francetv.{team}.{service}/
├── {ServiceName}Application.java
│
├── domain/
│   ├── model/               pure domain objects, no framework dependency
│   ├── port/
│   │   ├── in/              input ports — use case interfaces
│   │   └── out/             output ports — repository and client interfaces
│   └── service/             domain services implementing input ports
│
├── application/
│   └── usecase/             use case orchestrators, calls domain services
│
└── infrastructure/
    ├── adapter/
    │   ├── in/
    │   │   ├── web/         REST controllers (present when api capability selected)
    │   │   └── messaging/   NATS consumers (present when nats selected)
    │   └── out/
    │       ├── persistence/ JPA repositories (present when data selected)
    │       ├── rest/        HTTP client adapters (present when http-client selected)
    │       └── soap/        SOAP client adapters (present when soap-client selected)
    └── config/              Spring @Configuration classes
```

This structure enforces:

- domain isolation — domain package has zero framework dependency
- dependency inversion — infrastructure implements domain output ports
- testability — domain and application layers are testable without Spring context

### Minimal application.yml

The archetype generates an `application.yml` containing only the configuration blocks required by the selected capabilities.

No unused configuration blocks are generated.

### Generated Dockerfile

When `generateDockerfile=true` (default), a minimal `Dockerfile` targeting the Eclipse Temurin JRE 21 Alpine image is generated in the service root.

The Dockerfile:
- Uses a minimal JRE image (not JDK)
- Copies the Spring Boot fat JAR
- Exposes port 8080
- Sets the JVM entropy source flag for faster startup in containers

The Dockerfile belongs to the generated consuming service, not to `foundation-platform`.

### Generated GitLab CI

When `generateGitlabCi=true` (default), a minimal `.gitlab-ci.yml` is generated with three stages:

```text
build   → mvn compile
test    → mvn verify  (Testcontainers auto-starts required services)
package → mvn package -DskipTests  (on main / develop branches only)
```

The pipeline:
- Caches the local Maven repository per branch slug
- Publishes JUnit XML test reports
- Produces the JAR as a job artifact

The `.gitlab-ci.yml` belongs to the generated consuming service, not to `foundation-platform`.

---

## 18. Non-goals

The foundation is not intended to provide:

- business services
- business workflows
- business entities
- business APIs
- service-specific generated clients
- deployment infrastructure
- a sample or demonstration service inside the socle

The foundation exists only to provide reusable technical capabilities and the tooling to scaffold conforming services.

---

## 19. Related documents

Architecture decisions are documented through ADRs.

Implementation standards are documented in:

```text
docs/adr/*
docs/guidelines/*
ai/instructions/*
ai/skills/*
ai/prompts/*
```

When guidelines and implementation differ, ADRs take precedence.