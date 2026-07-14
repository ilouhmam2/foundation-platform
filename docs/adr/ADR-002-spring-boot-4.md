# ADR-002 - Use Spring Boot 4.1.x

## Status

Accepted

## Context

`foundation-platform` is a new technical foundation.

The team considered:

- Spring Boot 3.x
- Spring Boot 4.x

The client prefers open-source and free solutions.

Starting a new foundation on a version with limited remaining OSS support is not ideal.

## Decision

Use Spring Boot 4.1.x as the target Spring Boot baseline.

## Rationale

Spring Boot 4.x is the current generation of Spring Boot.

It provides a more future-proof baseline for a new foundation.

The platform will still keep the implementation close to standard Spring Boot practices to reduce migration and maintenance risks.

## Consequences

- Starters must follow Spring Boot 4 auto-configuration conventions.
- Spring Security configuration must align with the Spring Security version used by Spring Boot 4.
- Third-party library compatibility must be validated early.
- A technical POC must validate key integrations:
  - OAuth2 Resource Server
  - Flyway
  - PostgreSQL
  - NATS
  - MapStruct
  - OpenAPI Generator
  - Apache CXF
  - Actuator / Micrometer / OpenTelemetry