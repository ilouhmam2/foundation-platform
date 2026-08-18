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

Generated services are created outside this repository.

When Maven generates the archetype, it resolves the artifact from the local `~/.m2/repository` cache first. If the artifact is not available locally, Maven falls back to the remote repositories configured in `~/.m2/settings.xml` (or the effective Maven settings in use).

## Distribution model

`foundation-platform` artifacts are meant to be published and consumed through a Maven-compatible repository manager (Nexus, Artifactory, or equivalent).

Depending on capability boundaries and consumption needs, delivery can be:

- one consumable JAR
- multiple consumable JARs (multi-module starters)

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
| `security` | OAuth2 Resource Server / JWT |
| `data` | JPA + Flyway + PostgreSQL |
| `nats` | NATS messaging |
| `http-client` | WebClient / OpenAPI REST clients |
| `soap-client` | Apache CXF / WSDL SOAP clients |

Omit `-Dcapabilities` to generate a service with only the 6 mandatory starters (core, api, logging, observability, mapping, test). Add `security` when OAuth2/JWT is needed.

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

## Publishing artifacts

Foundation artifacts are published to the GitLab Package Registry of this project.

### Install locally (development)

```bash
mvn clean install
```

Installs all artifacts to `~/.m2/repository`.

### Deploy to GitLab Package Registry (CI)

```bash
mvn -B clean deploy -DskipTests=true --settings ci-settings.xml
```

`CI_JOB_TOKEN`, `CI_API_V4_URL`, and `CI_PROJECT_ID` are automatically injected by GitLab CI — no manual variable configuration needed.

### Deploy from a developer machine (optional)

Create a Deploy Token in GitLab (Settings → Repository → Deploy tokens) with scopes `read_package_registry` + `write_package_registry`, then add it to `~/.m2/settings.xml` (never commit credentials):

```xml
<servers>
  <server>
    <id>gitlab-maven</id>
    <username>MY_DEPLOY_TOKEN_USERNAME</username>
    <password>MY_DEPLOY_TOKEN_VALUE</password>
  </server>
</servers>
```

Then:

```bash
export CI_API_V4_URL=https://gitlab.example.com/api/v4
export CI_PROJECT_ID=<ID>
mvn clean deploy
```

## Versioning

Versions follow a snapshot/release cycle managed with `mvn versions:set`.

```bash
# 1. Bump to release
mvn versions:set -DnewVersion=X.Y.Z -DgenerateBackupPoms=false
git commit -am "release: prepare X.Y.Z"
git tag vX.Y.Z
git push origin vX.Y.Z   # triggers publish-release CI job

# 2. Return to SNAPSHOT
mvn versions:set -DnewVersion=X.Y.Z+1-SNAPSHOT -DgenerateBackupPoms=false
git commit -am "release: prepare next development iteration"
git push origin main
```

Never edit version numbers directly in POM files.

## Design philosophy

This project is **not** a heavy custom framework.

It must remain close to Spring Boot and must not hide standard Spring Boot behavior behind unnecessary abstractions.

The main design principle is:

```text
Parent POM = build standards
BOM = dependency versions
Starters = capabilities
Properties = configuration