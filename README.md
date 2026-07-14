# foundation-platform

`foundation-platform` is a lightweight enterprise Spring Boot foundation for building standardized microservices.

It provides a set of Maven parents, dependency management and Spring Boot starters to help teams build services with consistent conventions around:

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