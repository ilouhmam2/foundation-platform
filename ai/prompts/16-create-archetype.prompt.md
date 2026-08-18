# Prompt — foundation-archetype

## Context

Read before implementing:

- `AGENTS.md`
- `docs/architecture.md` section 17 (Archetype architecture)
- `docs/guidelines/module-guidelines.md`
- `PRD.md` section 7 (Maven Archetype)

Maven archetype generation must resolve the artifact from the local `~/.m2/repository` cache first, then from the remote repositories configured in `~/.m2/settings.xml` if it is not available locally.

---

## Objective

Implement `foundation-archetype`, a Maven Archetype that generates a new microservice project adopting:

- A ports-and-adapters (hexagonal) package structure
- 6 mandatory capabilities always included
- Up to 5 optional capabilities selected via `-Dcapabilities` at generation time
- A minimal `application.yml` with only the configuration blocks for the chosen capabilities
- An optional minimal `Dockerfile`
- An optional minimal `.gitlab-ci.yml`

---

## Generation parameters

| Parameter | Required | Default | Description |
|---|---|---|---|
| `groupId` | yes | — | Maven groupId, e.g. `fr.francetv.myteam` |
| `artifactId` | yes | — | Maven artifactId and directory name, e.g. `my-service` |
| `version` | yes | `0.0.1-SNAPSHOT` | Maven version |
| `serviceName` | yes | — | PascalCase Java class prefix, e.g. `MyService`. Produces `MyServiceApplication.java`. Must be entered by the user. Cannot be derived automatically from `artifactId` in Maven archetypes. |
| `capabilities` | no | _(none)_ | Comma-separated optional capabilities: `security`, `data`, `nats`, `http-client`, `soap-client` |
| `generateDockerfile` | no | `true` | Generate a minimal `Dockerfile` |
| `generateGitlabCi` | no | `true` | Generate a minimal `.gitlab-ci.yml` |

---

## Mandatory capabilities (always generated)

| Starter | Purpose |
|---|---|
| `foundation-core-starter` | Correlation ID filter |
| `foundation-api-starter` | REST API conventions, error handling |
| `foundation-logging-starter` | Structured JSON logging |
| `foundation-observability-starter` | Actuator, Micrometer, OpenTelemetry |
| `foundation-mapping-starter` | MapStruct |
| `foundation-test-starter` | Testing helpers |

---

## Optional capabilities

Selected via `-Dcapabilities=security,data,nats,http-client,soap-client` (comma-separated, any combination).

`security` must behave like the other optional capabilities: it is omitted by default and only added when explicitly requested.

| Key | Starter added | Packages added | YAML blocks added |
|---|---|---|---|
| `security` | `foundation-security-starter` | none | `spring.security.oauth2.resourceserver.jwt` |
| `data` | `foundation-data-starter` | `infrastructure/adapter/out/persistence` | `spring.datasource`, `spring.jpa`, `spring.flyway` |
| `nats` | `foundation-nats-starter` | `infrastructure/adapter/in/messaging` | `foundation.nats` |
| `http-client` | `foundation-http-client-starter` | `infrastructure/adapter/out/rest` | `foundation.http-client` |
| `soap-client` | `foundation-soap-client-starter` | `infrastructure/adapter/out/soap` | `foundation.soap-client` |

---

## Hexagonal package structure (always generated)

```
fr.francetv.${groupId}.${artifactId}/
├── ${ServiceName}Application.java
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
    │   │   ├── web/           present when: always (api is mandatory)
    │   │   └── messaging/     present when: nats
    │   └── out/
    │       ├── persistence/   present when: data
    │       ├── rest/          present when: http-client
    │       └── soap/          present when: soap-client
    └── config/
```

Empty packages must contain a `.gitkeep` placeholder so they are committed.

---

## Generated POM requirements

The generated `pom.xml` must:

1. Declare `<parent>` pointing to `foundation-parent`
2. Import `foundation-bom` in `<dependencyManagement>`
3. Declare all 6 mandatory starter dependencies
4. Declare only the optional starters selected via capabilities
5. Include `spring-boot-maven-plugin` in `<build>`

---

## Generated application.yml

Mandatory blocks (always present):

```yaml
spring:
  application:
    name: ${artifactId}

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      probes:
        enabled: true

foundation:
  logging:
    json-format: true
```

Additional block for `security` capability:

```yaml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: https://your-idp/.well-known/openid-configuration
```

Additional blocks per capability:

- `data`:
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/${artifactId}
    username: ${artifactId}
    password: changeme
  jpa:
    hibernate:
      ddl-auto: validate
    open-in-view: false
  flyway:
    enabled: true
```

- `nats`:
```yaml
foundation:
  nats:
    server-url: nats://localhost:4222
```

- `http-client`:
```yaml
foundation:
  http-client:
    clients:
      example:
        base-url: https://api.example.com
        connect-timeout: 5s
        read-timeout: 10s
```

- `soap-client`:
```yaml
foundation:
  soap-client:
    clients:
      example:
        wsdl-url: https://api.example.com/service?wsdl
        connect-timeout: 5000
        receive-timeout: 30000
```

---

## Generated Dockerfile

Generated when `generateDockerfile=true` (default). File: `Dockerfile` in project root.

```dockerfile
# Generated by foundation-archetype
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY target/${artifactId}-${version}.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "-Djava.security.egd=file:/dev/./urandom", "app.jar"]
```

Rules:
- Use JRE image, not JDK
- Use Alpine variant to minimize image size
- Reference the Spring Boot fat JAR produced by `mvn package`
- The entropy flag prevents slow startup in containerized environments

---

## Generated GitLab CI

Generated when `generateGitlabCi=true` (default). File: `.gitlab-ci.yml` in project root.

```yaml
# Generated by foundation-archetype
stages:
  - build
  - test
  - package

variables:
  MAVEN_OPTS: "-Dmaven.repo.local=$CI_PROJECT_DIR/.m2/repository -Dmaven.color=false"
  MAVEN_IMAGE: "maven:3.9-eclipse-temurin-21-alpine"

cache:
  key: "$CI_COMMIT_REF_SLUG"
  paths:
    - .m2/repository/

build:
  stage: build
  image: $MAVEN_IMAGE
  script:
    - mvn -B -q compile

test:
  stage: test
  image: $MAVEN_IMAGE
  script:
    - mvn -B verify
  artifacts:
    when: always
    reports:
      junit:
        - target/surefire-reports/*.xml
        - target/failsafe-reports/*.xml

package:
  stage: package
  image: $MAVEN_IMAGE
  script:
    - mvn -B package -DskipTests
  artifacts:
    expire_in: 1 hour
    paths:
      - target/*.jar
  rules:
    - if: $CI_COMMIT_BRANCH == "main"
    - if: $CI_COMMIT_BRANCH == "develop"
```

Rules:
- No Docker build step in the generated pipeline (Docker build belongs to the delivery pipeline, not the foundation)
- Testcontainers work automatically with the Docker-in-Docker service provided by the GitLab runner
- No `variables.DOCKER_HOST` needed when using Testcontainers with Ryuk disabled

---

## Module structure

```
foundation-archetype/
├── pom.xml
└── src/
    └── main/
        └── resources/
            ├── META-INF/
            │   └── maven/
            │       └── archetype-metadata.xml
            └── archetype-resources/
                ├── pom.xml
                └── src/
                    └── main/
                        ├── java/
                        │   └── __packageInPathFormat__/
                        │       ├── __ServiceName__Application.java
                        │       ├── domain/
                        │       │   ├── model/.gitkeep
                        │       │   ├── port/in/.gitkeep
                        │       │   ├── port/out/.gitkeep
                        │       │   └── service/.gitkeep
                        │       ├── application/
                        │       │   └── usecase/.gitkeep
                        │       └── infrastructure/
                        │           ├── adapter/in/web/.gitkeep
                        │           ├── adapter/in/messaging/.gitkeep  (nats only)
                        │           ├── adapter/out/persistence/.gitkeep (data only)
                        │           ├── adapter/out/rest/.gitkeep       (http-client only)
                        │           ├── adapter/out/soap/.gitkeep       (soap-client only)
                        │           └── config/.gitkeep
                        └── resources/
                            └── application.yml
                ├── Dockerfile                                          (generateDockerfile only)
                └── .gitlab-ci.yml                                      (generateGitlabCi only)
```

---

## Tests to write

```java
class ArchetypeGenerationIT {

    @Test
    void shouldGenerateWithMandatoryCapabilitiesOnly() {
        // Generate with no -Dcapabilities
      // Assert pom.xml contains all 6 mandatory starters
      // Assert foundation-security-starter is absent
        // Assert hexagonal package structure exists
        // Assert application.yml contains only mandatory blocks
        // Assert Dockerfile generated (default generateDockerfile=true)
        // Assert .gitlab-ci.yml generated (default generateGitlabCi=true)
        // Assert mvn compile succeeds on generated project
    }

    @Test
    void shouldGenerateWithDataCapability() {
        // Generate with -Dcapabilities=data
        // Assert foundation-data-starter in pom.xml
        // Assert infrastructure/adapter/out/persistence exists
        // Assert datasource block in application.yml
    }

    @Test
    void shouldGenerateWithAllCapabilities() {
      // Generate with -Dcapabilities=security,data,nats,http-client,soap-client
      // Assert all 5 optional starters in pom.xml
        // Assert all optional packages exist
        // Assert mvn compile succeeds
    }

    @Test
    void shouldNotIncludeUnselectedCapabilities() {
        // Generate with -Dcapabilities=nats
        // Assert foundation-data-starter NOT in pom.xml
        // Assert infrastructure/adapter/out/persistence does NOT exist
        // Assert no datasource block in application.yml
    }

    @Test
    void shouldGenerateDockerfileByDefault() {
        // Generate without -DgenerateDockerfile flag
        // Assert Dockerfile exists in project root
        // Assert Dockerfile uses eclipse-temurin:21-jre-alpine
        // Assert Dockerfile references ${artifactId}-${version}.jar
    }

    @Test
    void shouldSkipDockerfileWhenDisabled() {
        // Generate with -DgenerateDockerfile=false
        // Assert Dockerfile does NOT exist
    }

    @Test
    void shouldGenerateGitlabCiByDefault() {
        // Generate without -DgenerateGitlabCi flag
        // Assert .gitlab-ci.yml exists in project root
        // Assert stages: build, test, package present
        // Assert package stage restricted to main/develop branches
    }

    @Test
    void shouldSkipGitlabCiWhenDisabled() {
        // Generate with -DgenerateGitlabCi=false
        // Assert .gitlab-ci.yml does NOT exist
    }

    @Test
    void shouldUseServiceNameAsJavaClassPrefix() {
        // Generate with -DserviceName=InvoiceService
        // Assert InvoiceServiceApplication.java exists
    }
}
```

---

## Validation

```bash
# Build the archetype
mvn -pl foundation-archetype -am clean verify

# Generate a test project with all capabilities and deployment files
# Maven should resolve the archetype locally first, then from the configured remote repositories if needed
mvn archetype:generate \
  -DarchetypeGroupId=fr.francetv.foundation \
  -DarchetypeArtifactId=foundation-archetype \
  -DarchetypeVersion=0.0.1-SNAPSHOT \
  -DgroupId=fr.francetv.test \
  -DartifactId=generated-test \
  -DserviceName=GeneratedTest \
  -Dcapabilities=data,nats \
  -DgenerateDockerfile=true \
  -DgenerateGitlabCi=true \
  -DinteractiveMode=false

# Compile generated project
cd generated-test && mvn compile

# Check Dockerfile is present
Test-Path Dockerfile  # Windows: expected True
Get-Content Dockerfile

# Check GitLab CI is present
Test-Path .gitlab-ci.yml  # expected True
Get-Content .gitlab-ci.yml
```

---

## Definition of Done

- [ ] `mvn -pl foundation-archetype -am clean verify` → BUILD SUCCESS
- [ ] Generation without `-Dcapabilities` includes all 6 mandatory starters
- [ ] `security` capability adds foundation-security-starter and its JWT block only when requested
- [ ] Each optional capability adds exactly the right starter + packages + YAML blocks
- [ ] Unselected optional capabilities are entirely absent from pom.xml and application.yml
- [ ] Hexagonal package structure is always present (domain, application, infrastructure)
- [ ] `serviceName` parameter controls the Java class prefix correctly
- [ ] `Dockerfile` generated by default; absent when `generateDockerfile=false`
- [ ] `.gitlab-ci.yml` generated by default; absent when `generateGitlabCi=false`
- [ ] Generated project compiles with `mvn compile`
- [ ] Integration tests cover all combinations including Dockerfile/CI flags
- [ ] Review architect: Accepted
