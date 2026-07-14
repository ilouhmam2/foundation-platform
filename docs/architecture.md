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
└── foundation-sample-service
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

consuming services
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

---

## 17. Non-goals

The foundation is not intended to provide:

- business services
- business workflows
- business entities
- business APIs
- service-specific generated clients
- deployment infrastructure

The foundation exists only to provide reusable technical capabilities.

---

## 18. Related documents

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