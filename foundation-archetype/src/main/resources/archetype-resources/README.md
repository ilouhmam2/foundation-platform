# ${artifactId}

Service generated from `foundation-archetype`.

## Stack

- Java 21
- Spring Boot 4.1.x
- Maven

## Capabilities included

- `core` — Correlation ID filter
- `api` — REST API conventions, global error handling
- `security` — OAuth2 Resource Server (JWT)
- `logging` — Structured JSON logging
- `observability` — Actuator, Micrometer, OpenTelemetry
- `mapping` — MapStruct
- `test` — Testing helpers
#if($capabilities.contains("data"))
- `data` — JPA + Flyway + PostgreSQL
#end
#if($capabilities.contains("nats"))
- `nats` — NATS messaging
#end
#if($capabilities.contains("http-client"))
- `http-client` — Reactive HTTP client (WebClient)
#end
#if($capabilities.contains("soap-client"))
- `soap-client` — Apache CXF SOAP client
#end

## Package structure

```
${package}/
├── ${serviceName}Application.java
├── domain/
│   ├── model/
│   ├── port/
│   │   ├── in/
│   │   └── out/
│   └── service/
├── application/
│   └── usecase/
└── infrastructure/
    ├── adapter/
    │   ├── in/
    │   │   └── web/
#if($capabilities.contains("nats"))
    │   │   └── messaging/
#end
    │   └── out/
#if($capabilities.contains("data"))
    │   │   └── persistence/
#end
#if($capabilities.contains("http-client"))
    │   │   └── rest/
#end
#if($capabilities.contains("soap-client"))
    │   │   └── soap/
#end
    └── config/
```

## Getting started

1. Update `src/main/resources/application.yml` with your environment values.
2. Run:

```bash
mvn spring-boot:run
```

## Build

```bash
mvn clean verify
```
