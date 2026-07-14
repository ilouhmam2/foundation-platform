---
description: "Implement a foundation-platform Spring Boot starter module from scratch"
agent: "agent"
argument-hint: "Specify: MODULE=foundation-{name}-starter, SCOPE={what it provides}"
---
# Implement a Foundation Starter

Fill in before running:

```
MODULE:   foundation-{capability}-starter
SCOPE:    {one sentence: what capability this starter provides}
GUIDELINE: docs/guidelines/{capability}-guidelines.md
```

---

## Mandatory reading

Read in this order:

1. `AGENTS.md`
2. `docs/architecture.md` sections 7-8
3. `docs/adr/ADR-003-dependency-driven-composition.md`
4. `docs/guidelines/{capability}-guidelines.md`
5. `ai/skills/spring-boot-starter-design/SKILL.md`

## Required deliverables

1. `{MODULE}/pom.xml` — minimal dependencies only
2. Auto-configuration class with `@AutoConfiguration` + `@ConditionalOnClass` + `@ConditionalOnMissingBean`
3. `@ConfigurationProperties` class (only if configuration is needed)
4. `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`
5. Auto-configuration tests using `ApplicationContextRunner`
6. `README.md` with property reference and usage example

## Acceptance criteria

- [ ] Default beans created when starter is on classpath
- [ ] Consumer can override beans (`@ConditionalOnMissingBean`)
- [ ] Properties bind correctly
- [ ] No business logic
- [ ] No optional runtime dependencies added to parent POM
- [ ] `mvn -pl {MODULE} -am clean verify` passes

## Validation

```bash
mvn -pl {MODULE} -am clean verify
```
