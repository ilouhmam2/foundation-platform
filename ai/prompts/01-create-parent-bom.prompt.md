# Prompt - 01 Create Parent and BOM

## Role

You are a senior Maven and Spring Boot platform engineer.

You are implementing the build foundation of `foundation-platform`.

## Mandatory reading

Before producing files, read:

- `AGENTS.md`
- `docs/architecture.md`
- `docs/adr/ADR-001-java-21.md`
- `docs/adr/ADR-002-spring-boot-4.md`
- `docs/adr/ADR-003-dependency-driven-composition.md`
- `docs/guidelines/module-guidelines.md`
- `docs/guidelines/coding-guidelines.md`
- `ai/skills/maven-multimodule-design/SKILL.md`

## Objective

Implement:

```text
foundation-parent
foundation-bom
```

## foundation-parent must contain

- Java 21 compiler configuration
- Maven plugin management: compiler, surefire, failsafe, resources, spring-boot
- Code quality plugin configuration (optional: checkstyle or spotbugs)
- NO runtime dependencies

## foundation-bom must contain

- Import of `spring-boot-dependencies` BOM
- Third-party library versions: MapStruct, NATS, Apache CXF, OpenAPI Generator, Testcontainers, WireMock
- foundation-platform module versions (managed)

## Acceptance criteria

- [ ] `mvn -pl foundation-parent -am clean verify` passes
- [ ] `mvn -pl foundation-bom -am clean verify` passes
- [ ] Parent POM imports no runtime dependencies
- [ ] BOM imports no plugin definitions
- [ ] Java 21 compiler target is set

## Validation

```bash
mvn clean verify -pl foundation-parent,foundation-bom
```