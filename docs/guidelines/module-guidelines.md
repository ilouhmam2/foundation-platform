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
foundation-sample-service
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

## 8. foundation-sample-service

Demonstrates assembly of a microservice using foundation starters.

Must:
- Use a realistic subset of foundation starters
- Compile and run with `mvn spring-boot:run`
- Serve as a reference example

Must not:
- Contain real business logic
- Contain committed generated clients
- Serve as a template to copy-paste into production