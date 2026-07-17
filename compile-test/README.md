# compile-test

Service generated from `foundation-archetype`.


- Java 21
- Spring Boot 4.1.x
- Maven


- `core` — Correlation ID filter
- `api` — REST API conventions, global error handling
- `security` — OAuth2 Resource Server (JWT)
- `logging` — Structured JSON logging
- `observability` — Actuator, Micrometer, OpenTelemetry
- `mapping` — MapStruct
- `test` — Testing helpers
- `data` — JPA + Flyway + PostgreSQL
- `nats` — NATS messaging


```
fr.francetv.myteam/
├── CompileTestApplication.java
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
    │   │   └── messaging/
    │   └── out/
    │   │   └── persistence/
    └── config/
```


1. Update `src/main/resources/application.yml` with your environment values.
2. Run:

```bash
mvn spring-boot:run
```


```bash
mvn clean verify
```
