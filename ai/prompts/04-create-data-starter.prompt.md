# Prompt - Create foundation-data-starter

You are implementing `foundation-data-starter`.

Before coding, read:
- AGENTS.md
- docs/guidelines/data-guidelines.md
- docs/adr/ADR-003-dependency-driven-composition.md

## Scope

Implement:
- JPA default conventions
- Flyway default conventions
- transaction conventions
- audit support
- Testcontainers helper documentation

## Rules

- Do not create business entities
- Do not create sample Quote entity here
- Do not use `ddl-auto=update`
- Default to `ddl-auto=validate`
- Migrations belong to consuming services
- Consuming services must add `foundation-data-starter` explicitly

## Expected output

- Maven module files
- auto-configuration classes
- configuration properties
- tests
- README usage example
- review notes

## Validation

Run or explain:

```bash
mvn -pl foundation-data-starter -am clean verify