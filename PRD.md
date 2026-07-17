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
- A Maven archetype to generate ready-to-use hexagonal service projects

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
- Embed a sample or demonstration service inside the socle — consuming services live outside this repository

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
| `foundation-archetype` | Maven archetype for generating hexagonal service projects |

---

## 7. Maven Archetype — Service Generator

### Purpose

`foundation-archetype` allows teams to scaffold a new microservice in seconds by selecting only the capabilities they need.

The sample service (`foundation-sample-service`) is removed from the socle. Consuming services are generated outside this repository.

### Usage

```bash
mvn archetype:generate \
  -DarchetypeGroupId=fr.francetv.foundation \
  -DarchetypeArtifactId=foundation-archetype \
  -DarchetypeVersion=${foundation.version} \
  -DgroupId=fr.francetv.myteam \
  -DartifactId=my-service \
  -Dversion=0.0.1-SNAPSHOT \
  -DserviceName=MyService \
  -Dcapabilities=data,nats \
  -DgenerateDockerfile=true \
  -DgenerateGitlabCi=true
```

### Generation parameters

| Parameter | Required | Default | Description |
|---|---|---|---|
| `groupId` | yes | — | Maven groupId, e.g. `fr.francetv.myteam` |
| `artifactId` | yes | — | Maven artifactId and directory name, e.g. `my-service` |
| `version` | yes | `0.0.1-SNAPSHOT` | Maven version |
| `serviceName` | yes | — | PascalCase Java class prefix, e.g. `MyService` (used for `MyServiceApplication.java`) |
| `capabilities` | no | _(none)_ | Comma-separated optional capabilities: `data`, `nats`, `http-client`, `soap-client` |
| `generateDockerfile` | no | `true` | Generate a minimal `Dockerfile` for the service |
| `generateGitlabCi` | no | `true` | Generate a minimal `.gitlab-ci.yml` for the service |

### Mandatory capabilities (always included)

Every generated service automatically includes:

| Capability | Starter |
|---|---|
| Core (correlation ID) | `foundation-core-starter` |
| REST API conventions | `foundation-api-starter` |
| Security (OAuth2/JWT) | `foundation-security-starter` |
| Structured logging | `foundation-logging-starter` |
| Observability (Actuator, Micrometer) | `foundation-observability-starter` |
| MapStruct | `foundation-mapping-starter` |
| Testing helpers | `foundation-test-starter` |

### Optional capabilities (selected at generation)

| Capability key | Starter added | Description |
|---|---|---|
| `data` | `foundation-data-starter` | JPA, Flyway, PostgreSQL |
| `nats` | `foundation-nats-starter` | NATS messaging |
| `http-client` | `foundation-http-client-starter` | WebClient / OpenAPI REST clients |
| `soap-client` | `foundation-soap-client-starter` | Apache CXF / WSDL SOAP clients |

### Generated hexagonal architecture

The generated service uses a ports-and-adapters (hexagonal) package structure:

```
fr.francetv.{team}.{service}/
├── {ServiceName}Application.java
├── domain/
│   ├── model/                  ← pure domain objects
│   ├── port/
│   │   ├── in/                 ← input ports (use case interfaces)
│   │   └── out/                ← output ports (repository / client interfaces)
│   └── service/                ← domain services
├── application/
│   └── usecase/                ← use case implementations
└── infrastructure/
    ├── adapter/
    │   ├── in/
    │   │   ├── web/            ← REST controllers (if api selected)
    │   │   └── messaging/      ← NATS consumers (if nats selected)
    │   └── out/
    │       ├── persistence/    ← JPA repositories (if data selected)
    │       ├── rest/           ← HTTP client adapters (if http-client selected)
    │       └── soap/           ← SOAP client adapters (if soap-client selected)
    └── config/                 ← Spring @Configuration classes
```

### Generated application.yml

The archetype generates a minimal `application.yml` with only the properties required by the selected capabilities. No unused configuration blocks are included.

### Generated Dockerfile

When `generateDockerfile=true` (default), a minimal `Dockerfile` is generated in the service root:

```dockerfile
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY target/${artifactId}-${version}.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "-Djava.security.egd=file:/dev/./urandom", "app.jar"]
```

> The Dockerfile belongs to the generated consuming service, not to `foundation-platform`.

### Generated GitLab CI

When `generateGitlabCi=true` (default), a minimal `.gitlab-ci.yml` is generated covering three stages: `build`, `test`, `package`.

The pipeline:
- Uses `maven:3.9-eclipse-temurin-21-alpine` as the build image
- Caches the local Maven repository per branch
- Runs `mvn verify` in the `test` stage (Testcontainers auto-starts required services)
- Publishes JUnit XML reports
- Packages the JAR artifact on `main` and `develop` branches

> The `.gitlab-ci.yml` belongs to the generated consuming service, not to `foundation-platform`.