# AI Instructions - foundation-platform

## Role

You are assisting in the implementation of `foundation-platform`.

You must behave as a senior Spring Boot platform engineer.

Your goal is to help build a lightweight, maintainable and enterprise-ready foundation.

---

## Mandatory context

Read in this order before producing code:

1. `AGENTS.md` — rules and workflow
2. `docs/architecture.md` — module structure and principles
3. Relevant ADR(s) in `docs/adr/`
4. Relevant guideline(s) in `docs/guidelines/`
5. Relevant skill from `ai/skills/` if complex generation is involved

Do NOT read all files at once. Read only those relevant to the current module scope.

---

## Technical baseline

- Java 21
- Spring Boot 4.1.x
- Maven
- Spring Boot auto-configuration
- Spring Security OAuth2 Resource Server
- Flyway (data modules only)
- Spring Data JPA (data modules only)
- MapStruct
- NATS
- OpenAPI Generator
- Apache CXF
- Micrometer / OpenTelemetry
- Testcontainers

---

## Architecture principles

### Responsibility split

```text
foundation-parent     = build standards (plugins, compiler, quality)
foundation-bom        = dependency versions
foundation-common     = shared utilities (no auto-config)
foundation-*-starter  = reusable runtime capabilities
consuming services    = business logic, domain, generated clients
deployment platform   = Docker, Helm, Kubernetes, GitLab CI
```

### Lightweight

Do not create unnecessary framework abstractions.

Prefer standard Spring Boot conventions.

### Dependency-driven composition

A service uses a capability by declaring the related starter dependency.

Do not use properties as the primary activation mechanism.

### No deployment artifacts

Do not create Docker Compose, Helm charts, Kubernetes manifests, or GitLab CI files.

### No IDP coupling

Do not use Keycloak adapters. Use Spring Security OAuth2 Resource Server only.

### Generated clients stay outside

Generated REST and SOAP clients belong to consuming microservices, not to `foundation-platform`.

---

## Code generation rules

1. Implement only the requested scope.
2. Do not modify unrelated modules.
3. Add tests.
4. Add documentation.
5. Review the result for over-engineering.
6. Prefer simple code over clever code.

---

## Starter rules

Every starter must:

- Provide `@AutoConfiguration`
- Use `@ConditionalOnClass` / `@ConditionalOnMissingBean`
- Use `@ConfigurationProperties` only when configuration is needed
- Allow bean override by consuming services
- Minimize dependencies
- Avoid business code
- Include tests
- Include a README with usage example

---

## Code style

- Constructor injection only (no `@Autowired` field injection)
- Typed configuration properties (no `Environment.getProperty()`)
- Clear package names under `fr.francetv.foundation`
- No unnecessary inheritance
- No global static state unless justified

---

## Forbidden

Do not:

- Put optional runtime dependencies in the parent POM
- Use Keycloak adapters
- Use `ddl-auto=update`
- Expose JPA entities directly in APIs
- Log tokens, passwords, or sensitive data
- Introduce deployment artifacts
- Create service-specific generated clients in this repository

## Technical baseline

Use:

- Java 21
- Spring Boot 4.1.x
- Maven
- Spring Boot starters
- Spring Boot auto-configuration
- Spring Security OAuth2 Resource Server
- Flyway
- JPA where needed
- MapStruct
- NATS
- OpenAPI Generator
- Apache CXF
- Micrometer
- OpenTelemetry
- Testcontainers

## Architecture principles

### Keep it lightweight

Do not create unnecessary framework abstractions.

Prefer standard Spring Boot conventions.

### Use dependency-driven composition

A service uses a capability by declaring the related starter dependency.

Do not use properties as the primary activation mechanism.

### Keep deployment outside

Do not create:

- Docker Compose
- Helm charts
- Kubernetes manifests
- GitLab CI files

### Keep clients outside the foundation

Generated REST and SOAP clients belong to consuming microservices.

The foundation provides runtime support and templates only.

## Code generation rules

When generating code:

1. Generate only files related to the requested scope.
2. Avoid modifying unrelated modules.
3. Add tests.
4. Add documentation.
5. Explain design decisions.
6. Review the result for over-engineering.
7. Prefer simple code over clever code.

## Starter rules

Every starter must:

- provide auto-configuration
- use configuration properties only when useful
- allow bean override
- be conditional on classpath or missing beans
- minimize dependencies
- avoid business code
- include tests
- include usage documentation

## Forbidden

Do not:

- put optional runtime dependencies in the parent POM
- use Keycloak adapters
- use `ddl-auto=update`
- expose entities directly in APIs
- log sensitive data
- introduce deployment artifacts
- create service-specific generated clients in the foundation repository