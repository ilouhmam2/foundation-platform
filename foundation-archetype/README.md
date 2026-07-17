# foundation-archetype

Maven Archetype generating a hexagonal Spring Boot microservice.

## Usage

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
  -DgenerateGitlabCi=true \
  -DinteractiveMode=false
```

## Parameters

| Parameter            | Required | Default            | Description                                                                 |
|----------------------|----------|--------------------|-----------------------------------------------------------------------------|
| `groupId`            | yes      | —                  | Maven groupId, e.g. `fr.francetv.myteam`                                    |
| `artifactId`         | yes      | —                  | Maven artifactId and directory name, e.g. `my-service`                      |
| `version`            | yes      | `0.0.1-SNAPSHOT`   | Maven version                                                               |
| `serviceName`        | yes      | —                  | PascalCase Java class prefix, e.g. `MyService` → `MyServiceApplication.java` |
| `capabilities`       | no       | _(none)_           | Comma-separated optional capabilities (see below)                           |
| `generateDockerfile` | no       | `true`             | Generate a minimal `Dockerfile`                                             |
| `generateGitlabCi`   | no       | `true`             | Generate a minimal `.gitlab-ci.yml`                                         |
| `foundationVersion`  | no       | `0.0.1-SNAPSHOT`   | Version of `foundation-parent` and `foundation-bom` used in the generated POM |

## Mandatory capabilities (always included)

| Starter                      | Purpose                          |
|------------------------------|----------------------------------|
| `foundation-core-starter`    | Correlation ID filter            |
| `foundation-api-starter`     | REST API conventions, error handling |
| `foundation-security-starter`| OAuth2 Resource Server (JWT)     |
| `foundation-logging-starter` | Structured JSON logging          |
| `foundation-observability-starter` | Actuator, Micrometer, OpenTelemetry |
| `foundation-mapping-starter` | MapStruct                        |
| `foundation-test-starter`    | Testing helpers                  |

## Optional capabilities (`-Dcapabilities`)

| Key           | Starter added                      | Packages added                             | YAML blocks added                          |
|---------------|------------------------------------|--------------------------------------------|-------------------------------------------|
| `data`        | `foundation-data-starter`          | `infrastructure/adapter/out/persistence`   | `spring.datasource`, `spring.jpa`, `spring.flyway` |
| `nats`        | `foundation-nats-starter`          | `infrastructure/adapter/in/messaging`      | `foundation.nats`                          |
| `http-client` | `foundation-http-client-starter`   | `infrastructure/adapter/out/rest`          | `foundation.http-client`                   |
| `soap-client` | `foundation-soap-client-starter`   | `infrastructure/adapter/out/soap`          | `foundation.soap-client`                   |

## Generated project structure

```
my-service/
├── pom.xml
├── Dockerfile
├── .gitlab-ci.yml
├── README.md
└── src/
    ├── main/
    │   ├── java/
    │   │   └── fr/francetv/myteam/
    │   │       ├── MyServiceApplication.java
    │   │       ├── domain/
    │   │       │   ├── model/
    │   │       │   ├── port/
    │   │       │   │   ├── in/
    │   │       │   │   └── out/
    │   │       │   └── service/
    │   │       ├── application/
    │   │       │   └── usecase/
    │   │       └── infrastructure/
    │   │           ├── adapter/
    │   │           │   ├── in/
    │   │           │   │   ├── web/
    │   │           │   │   └── messaging/  (nats only)
    │   │           │   └── out/
    │   │           │       ├── persistence/  (data only)
    │   │           │       ├── rest/         (http-client only)
    │   │           │       └── soap/         (soap-client only)
    │   │           └── config/
    │   └── resources/
    │       └── application.yml
    └── test/
        ├── java/
        │   └── fr/francetv/myteam/
        │       └── MyServiceApplicationTests.java
        └── resources/
            └── application.yml
```

## Building the archetype

```bash
mvn -pl foundation-archetype -am clean verify
```

## Local installation (required before generating)

```bash
mvn -pl foundation-archetype -am clean install -DskipTests
```
