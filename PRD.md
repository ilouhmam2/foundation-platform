# PRD - foundation-platform

## 1. Context

The objective of `foundation-platform` is to provide a lightweight technical foundation for enterprise microservices developed with Spring Boot.

The platform is intended to support a migration from integration-oriented platforms such as webMethods toward a modern Spring Boot-based architecture.

The target ecosystem includes:

- Gravitee API Gateway
- Spring Boot microservices
- PostgreSQL
- Flyway
- NATS
- OAuth2 / JWT
- OpenAPI / Swagger contracts
- WSDL contracts for SOAP integrations
- Observability platforms
- Helm and GitLab CI for deployment

Deployment concerns are intentionally outside the scope of this repository.

---

## 2. Objectives

`foundation-platform` must help teams create microservices faster while enforcing consistent technical standards.

The platform should provide:

- A Maven parent for build conventions
- A BOM for dependency versions
- Spring Boot starters for reusable capabilities
- API error handling conventions
- Security conventions
- Logging and correlation ID conventions
- Observability defaults
- Data and Flyway defaults
- MapStruct configuration
- NATS messaging conventions
- REST client support for OpenAPI-generated clients
- SOAP client support for WSDL-generated clients
- Testing helpers
- Documentation and AI-agent instructions

---

## 3. Non-objectives

`foundation-platform` must not:

- Become a heavy custom framework
- Hide Spring Boot conventions behind proprietary abstractions
- Force runtime dependencies on all consuming services
- Provide service-specific business logic, entities, or APIs
- Contain service-specific generated REST or SOAP clients
- Include deployment artifacts (Docker Compose, Kubernetes, Helm, GitLab CI)
- Couple to a specific Identity Provider

---

## 4. Success criteria

`foundation-platform` is successful when:

- A new microservice can be bootstrapped in under a day using the foundation
- Services are consistent in their API conventions, security model, and observability
- Teams do not duplicate infrastructure code across services
- The foundation does not require Spring Boot expertise beyond standard skills
- Consuming services can override any default behavior without forking the foundation

---

## 5. Constraints

- Java 21 LTS baseline
- Spring Boot 4.1.x
- Maven multi-module structure
- No vendor lock-in on Identity Provider
- No deployment artifacts in this repository
- Compatible with Gravitee API Gateway conventions

---

## 6. Delivered modules

| Module | Responsibility |
|---|---|
| `foundation-parent` | Build conventions, plugin management |
| `foundation-bom` | Dependency version management |
| `foundation-common` | Shared foundation utilities |
| `foundation-core-starter` | Core auto-configuration baseline |
| `foundation-api-starter` | API conventions, error handling |
| `foundation-security-starter` | OAuth2 Resource Server |
| `foundation-logging-starter` | Correlation ID, structured logs |
| `foundation-observability-starter` | Actuator, Micrometer, OpenTelemetry |
| `foundation-mapping-starter` | MapStruct configuration |
| `foundation-data-starter` | JPA, Flyway, PostgreSQL defaults |
| `foundation-nats-starter` | NATS messaging conventions |
| `foundation-http-client-starter` | WebClient, OpenAPI client support |
| `foundation-soap-client-starter` | Apache CXF, WSDL client support |
| `foundation-test-starter` | Testing helpers and Testcontainers support |
| `foundation-sample-service` | Example consuming service |