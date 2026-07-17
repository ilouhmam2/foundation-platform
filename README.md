# foundation-platform

`foundation-platform` is a lightweight enterprise Spring Boot foundation for building standardized microservices.

It provides a set of Maven parents, dependency management, Spring Boot starters, and a Maven archetype to help teams scaffold and build services with consistent conventions around:

- REST APIs
- OAuth2 / JWT security
- Logging and correlation ID
- Observability
- Data access and Flyway migrations
- MapStruct mappings
- NATS messaging
- REST client generation from OpenAPI contracts
- SOAP client generation from WSDL contracts
- Testing conventions

New services are generated using `foundation-archetype` — a Maven archetype that scaffolds a ready-to-use hexagonal (ports and adapters) project with mandatory capabilities pre-configured and optional capabilities selected at generation time.

## Quick start — generate a new service

```bash
mvn archetype:generate \
  -DarchetypeGroupId=fr.francetv.foundation \
  -DarchetypeArtifactId=foundation-archetype \
  -DarchetypeVersion=0.0.1-SNAPSHOT \
  -DgroupId=fr.francetv.myteam \
  -DartifactId=my-service \
  -Dversion=0.0.1-SNAPSHOT \
  -DserviceName=MyService \
  -Dcapabilities=data,nats \
  -DgenerateDockerfile=true \
  -DgenerateGitlabCi=true
```

| Parameter | Description |
|---|---|
| `serviceName` | PascalCase class prefix, e.g. `MyService` → `MyServiceApplication.java` |
| `-Dcapabilities` | Comma-separated optional capabilities |
| `-DgenerateDockerfile` | Generate a minimal `Dockerfile` (default: `true`) |
| `-DgenerateGitlabCi` | Generate a minimal `.gitlab-ci.yml` (default: `true`) |

| `-Dcapabilities` value | Added capability |
|---|---|
| `data` | JPA + Flyway + PostgreSQL |
| `nats` | NATS messaging |
| `http-client` | WebClient / OpenAPI REST clients |
| `soap-client` | Apache CXF / WSDL SOAP clients |

Omit `-Dcapabilities` to generate a service with only the 7 mandatory starters (core, api, security, logging, observability, mapping, test).

## Target stack

- Java 21
- Spring Boot 4.1.x
- Maven
- Gravitee API Gateway
- PostgreSQL
- Flyway
- NATS
- OAuth2 Resource Server / JWT
- MapStruct
- OpenAPI Generator
- Apache CXF
- Micrometer / OpenTelemetry / Actuator

## Design philosophy

This project is **not** a heavy custom framework.

It must remain close to Spring Boot and must not hide standard Spring Boot behavior behind unnecessary abstractions.

The main design principle is:

```text
Parent POM = build standards
BOM = dependency versions
Starters = capabilities
Properties = configuration