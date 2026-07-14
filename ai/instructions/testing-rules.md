# Testing Rules - foundation-platform

## 1. Purpose

This document defines how tests should be created for `foundation-platform`.

## 2. Test levels

Use:

- unit tests for isolated logic
- auto-configuration tests for starters
- integration tests for external systems
- contract-oriented tests for generated clients where appropriate

## 3. Auto-configuration tests

Starter modules should test auto-configuration using lightweight Spring context tests.

Tests should verify:

- expected beans are created
- user-defined beans override defaults
- properties bind correctly
- optional sub-features behave correctly

## 4. Integration tests

Use integration tests when working with:

- PostgreSQL
- Flyway
- NATS
- HTTP clients
- SOAP clients
- security configuration

## 5. Testcontainers

Use Testcontainers for PostgreSQL integration tests.

Use equivalent container-based or embedded approaches for other infrastructure when appropriate.

## 6. HTTP clients

For HTTP integrations, use WireMock or equivalent.

Test:

- success case
- 4xx error
- 5xx error
- timeout
- auth header
- correlation ID propagation

## 7. Security tests

Security tests must cover:

- public endpoint access
- protected endpoint without token
- protected endpoint with invalid token
- protected endpoint with valid token
- role/scope mapping

## 8. Data tests

Data tests must cover:

- Flyway migration execution
- repository behavior
- constraints
- transaction behavior
- idempotence table if used

## 9. Naming

Use clear test names.

Example:

```text
shouldReturnValidationErrorWhenRequiredFieldIsMissing
shouldCreateDefaultCorrelationIdWhenHeaderIsAbsent
shouldNotCreateDataSourceWhenDataStarterIsNotPresent
```