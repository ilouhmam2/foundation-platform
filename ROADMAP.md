# ROADMAP — foundation-platform

## Légende

| Symbole | Signification |
|---|---|
| 🤖 Agent | Agent Copilot à sélectionner dans le picker |
| 📄 Prompt | Fichier prompt à coller dans le chat ou slash command |
| 📖 Lire | Documents à charger en contexte |
| ✅ DoD | Definition of Done — critères d'acceptation |
| 🔴 Bloquant | Erreur fréquente à éviter |

---

## Mode d'emploi

Chaque phase suit **toujours** ce cycle :

```
1. IMPLÉMENTER  →  agent engineer  +  prompt dédié
2. TESTER       →  mvn test  +  vérifier couverture
3. REVOIR       →  agent reviewer  (ou architect pour structure)
4. CORRIGER     →  agent engineer  pour les Required changes
5. VALIDER      →  mvn clean verify  →  vert = phase terminée
6. COMMITTER    →  git commit -m "feat(module): description"
```

Ne jamais passer à la phase suivante si `mvn clean verify` échoue.

---

## Phase 0 — Vérification de l'environnement

### Objectif
Confirmer que tous les outils sont opérationnels avant d'écrire la première ligne de code.

### Commandes de vérification

```bash
java -version
# attendu : openjdk 21.x.x

mvn -version
# attendu : Apache Maven 3.9.x, Java 21

git --version
# attendu : git 2.x

# Vérifier que Spring Boot 4.1.x est accessible
# (nécessite accès Maven Central ou repo interne)
mvn dependency:get -Dartifact=org.springframework.boot:spring-boot:4.1.0
```

### Vérification infra IA

```
Ouvrir VS Code Copilot Chat
→ Picker agent : vérifier que "engineer", "architect", "reviewer", "test-engineer" apparaissent
→ Taper /  : vérifier que "implement-starter" et "review-module" apparaissent
```

### ✅ DoD Phase 0
- [ ] Java 21 confirmé
- [ ] Maven 3.9+ confirmé
- [ ] Agents Copilot visibles dans le picker
- [ ] Prompts /implement-starter et /review-module disponibles

---

## Phase 1 — Bootstrap structure Maven

### Objectif
Créer le squelette complet de tous les modules Maven sans aucun code Java.

### 🤖 Agent : `engineer`

### 📄 Prompt

Coller le contenu de `ai/prompts/00-bootstrap-repo.prompt.md` dans le chat.

```
Copilot Chat → agent "engineer" → coller contenu de ai/prompts/00-bootstrap-repo.prompt.md
```

### 📖 Documents lus automatiquement par l'agent
- `AGENTS.md`
- `docs/architecture.md`
- `docs/adr/ADR-001-java-21.md`
- `docs/adr/ADR-002-spring-boot-4.md`
- `docs/adr/ADR-003-dependency-driven-composition.md`
- `docs/adr/ADR-004-no-deployment-in-foundation.md`
- `docs/guidelines/module-guidelines.md`
- `ai/skills/maven-multimodule-design/SKILL.md`

### Livrables attendus

```
pom.xml                              ← aggregator racine
foundation-parent/pom.xml
foundation-bom/pom.xml
foundation-common/pom.xml
foundation-core-starter/pom.xml
foundation-api-starter/pom.xml
foundation-security-starter/pom.xml
foundation-logging-starter/pom.xml
foundation-observability-starter/pom.xml
foundation-mapping-starter/pom.xml
foundation-data-starter/pom.xml
foundation-nats-starter/pom.xml
foundation-http-client-starter/pom.xml
foundation-soap-client-starter/pom.xml
foundation-test-starter/pom.xml
foundation-sample-service/pom.xml
```

### Validation

```bash
mvn validate
# attendu : BUILD SUCCESS sur tous les modules
```

### 🔴 Erreurs fréquentes
- Parent POM contient des dépendances runtime → refuser, les retirer
- Module `foundation-bom` hérite de `foundation-parent` mais déclare aussi `<dependencyManagement>` incompatible → vérifier structure
- groupId incorrect : doit être `fr.francetv.foundation` partout

### 🤖 Review : agent `architect`

```
Copilot Chat → agent "architect"
→ "Vérifie la structure Maven du module root et de tous les sous-modules.
   Contrôle que le parent POM ne contient aucune dépendance runtime
   et que le BOM importe correctement spring-boot-dependencies."
```

### ✅ DoD Phase 1
- [ ] `mvn validate` → BUILD SUCCESS
- [ ] root pom liste tous les modules dans `<modules>`
- [ ] foundation-parent ne contient aucune dépendance runtime
- [ ] foundation-bom importe `spring-boot-dependencies`
- [ ] Tous les modules héritent de foundation-parent
- [ ] Review architect : Accepted

---

## Phase 2 — foundation-parent complet

### Objectif
Définir toutes les conventions de build : Java 21, plugins Maven, qualité du code.

### 🤖 Agent : `engineer`

### 📄 Prompt

```
Copilot Chat → agent "engineer" → coller contenu de ai/prompts/01-create-parent-bom.prompt.md
(section foundation-parent uniquement)
```

Ou directement :

```
MODULE: foundation-parent
SCOPE: Conventions de build Java 21, gestion des plugins Maven,
       configuration qualité (Checkstyle ou SpotBugs)
```

### Contenu attendu dans foundation-parent/pom.xml

```xml
<!-- Java 21 -->
<properties>
  <java.version>21</java.version>
  <maven.compiler.source>21</maven.compiler.source>
  <maven.compiler.target>21</maven.compiler.target>
</properties>

<!-- Plugin management (pas de runtime deps) -->
<build>
  <pluginManagement>
    <plugins>
      <!-- maven-compiler-plugin -->
      <!-- maven-surefire-plugin -->
      <!-- maven-failsafe-plugin -->
      <!-- spring-boot-maven-plugin (gestion de version uniquement) -->
    </plugins>
  </pluginManagement>
</build>
```

### Validation

```bash
mvn -pl foundation-parent clean verify
# attendu : BUILD SUCCESS
```

### 🤖 Review : agent `architect`

```
"Vérifie que foundation-parent ne force aucune dépendance runtime.
 Contrôle que Java 21 est configuré comme source et target.
 Vérifie la gestion des plugins."
```

### ✅ DoD Phase 2
- [ ] `mvn -pl foundation-parent clean verify` → BUILD SUCCESS
- [ ] Java 21 configuré en source + target + release
- [ ] Aucune dépendance runtime dans le parent
- [ ] Plugin management complet (compiler, surefire, failsafe)
- [ ] Review architect : Accepted

---

## Phase 3 — foundation-bom

### Objectif
Centraliser toutes les versions de dépendances : Spring Boot 4.1.x, MapStruct, NATS, CXF, etc.

### 🤖 Agent : `engineer`

### 📄 Prompt

```
Copilot Chat → agent "engineer"

MODULE: foundation-bom
SCOPE: BOM centralisant les versions de Spring Boot 4.1.x,
       MapStruct 1.6+, NATS client, Apache CXF 4.x,
       OpenAPI Generator, Testcontainers, WireMock.
       Import de spring-boot-dependencies.
GUIDELINE: docs/guidelines/module-guidelines.md section 4
```

### Contenu attendu

```xml
<dependencyManagement>
  <dependencies>
    <!-- Spring Boot BOM -->
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-dependencies</artifactId>
      <version>4.1.x</version>
      <type>pom</type>
      <scope>import</scope>
    </dependency>
    <!-- MapStruct -->
    <!-- NATS Java client -->
    <!-- Apache CXF -->
    <!-- OpenAPI Generator annotations -->
    <!-- Testcontainers BOM -->
    <!-- WireMock -->
    <!-- Tous les modules foundation-platform (version ${project.version}) -->
  </dependencies>
</dependencyManagement>
```

### Validation

```bash
mvn -pl foundation-bom clean verify
mvn -pl foundation-bom dependency:display-ancestors
```

### 🔴 Erreurs fréquentes
- BOM contient des `<plugins>` → appartient au parent, pas au BOM
- Version Spring Boot non LTS ou snapshot → utiliser uniquement releases stables

### ✅ DoD Phase 3
- [ ] `mvn -pl foundation-bom clean verify` → BUILD SUCCESS
- [ ] Toutes les versions des modules foundation gérées dans le BOM
- [ ] Spring Boot 4.1.x importé via BOM
- [ ] Aucun `<build>` ou `<plugins>` dans le BOM
- [ ] Review architect : Accepted

---

## Phase 4 — foundation-common

### Objectif
Utilitaires partagés entre tous les modules foundation : constantes, types de base. Aucune auto-configuration Spring.

### 🤖 Agent : `engineer`

### 📄 Prompt

```
Copilot Chat → agent "engineer"

MODULE: foundation-common
SCOPE: Utilitaires partagés sans dépendance Spring.
       Constantes (CORRELATION_ID_HEADER = "X-Correlation-Id"),
       interfaces partagées, types d'exceptions de base.
       Aucune auto-configuration, aucun @Bean.
GUIDELINE: docs/guidelines/coding-guidelines.md
```

### Livrables attendus

```
foundation-common/src/main/java/fr/francetv/foundation/common/
  FoundationHeaders.java        ← constantes headers HTTP
  CorrelationIdUtils.java       ← utilitaire correlation ID (si justifié)
```

### Tests à écrire

```java
// Test unitaire pur, aucun contexte Spring
class FoundationHeadersTest {
    @Test
    void shouldDefineCorrelationIdHeader() {
        assertThat(FoundationHeaders.CORRELATION_ID).isEqualTo("X-Correlation-Id");
    }
}
```

### Validation

```bash
mvn -pl foundation-common -am clean verify
```

### 🔴 Erreurs fréquentes
- Ajout d'une dépendance Spring dans foundation-common → non autorisé
- foundation-common devient un "fourre-tout" → chaque classe doit être justifiée

### ✅ DoD Phase 4
- [ ] Tests unitaires passent
- [ ] Aucune dépendance Spring dans le POM
- [ ] Moins de 5 classes (si plus, challenger la nécessité)
- [ ] `mvn -pl foundation-common -am clean verify` → BUILD SUCCESS

---

## Phase 5 — foundation-core-starter

### Objectif
Premier vrai starter : auto-configuration de base transverse, propagation du Correlation ID via MDC.

### 🤖 Agent : `engineer`

### 📄 Prompt

```
Copilot Chat → agent "engineer" → /implement-starter

MODULE:    foundation-core-starter
GUIDELINE: docs/guidelines/coding-guidelines.md
SCOPE:     Filtre HTTP pour extraction et propagation du X-Correlation-Id
           dans le MDC. Génération d'un ID si absent.
           Auto-configuration conditionnelle.
```

### Livrables attendus

```
foundation-core-starter/src/main/java/fr/francetv/foundation/core/
  autoconfigure/
    CoreAutoConfiguration.java
  web/
    CorrelationIdFilter.java
  properties/
    CoreProperties.java

foundation-core-starter/src/main/resources/
  META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports

foundation-core-starter/src/test/java/fr/francetv/foundation/core/
  CoreAutoConfigurationTest.java

foundation-core-starter/README.md
```

### Structure auto-configuration

```java
@AutoConfiguration
@ConditionalOnClass(HttpFilter.class)
@EnableConfigurationProperties(CoreProperties.class)
public class CoreAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public CorrelationIdFilter correlationIdFilter(CoreProperties properties) {
        return new CorrelationIdFilter(properties);
    }
}
```

### Tests à écrire

```java
class CoreAutoConfigurationTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(CoreAutoConfiguration.class));

    @Test
    void shouldCreateCorrelationIdFilterByDefault() {
        runner.run(ctx ->
            assertThat(ctx).hasSingleBean(CorrelationIdFilter.class));
    }

    @Test
    void shouldRespectUserDefinedFilter() {
        runner
            .withUserConfiguration(CustomFilterConfig.class)
            .run(ctx ->
                assertThat(ctx).hasSingleBean(CorrelationIdFilter.class)
                    .getBean(CorrelationIdFilter.class)
                    .isInstanceOf(CustomCorrelationIdFilter.class));
    }

    @Test
    void shouldGenerateCorrelationIdWhenHeaderAbsent() {
        // Test du filtre avec MockHttpServletRequest sans header
    }
}
```

### Validation

```bash
mvn -pl foundation-core-starter -am clean verify
```

### 🤖 Review : agent `reviewer`

```
Copilot Chat → agent "reviewer"
→ /review-module  (MODULE = foundation-core-starter)
```

### ✅ DoD Phase 5
- [ ] `CorrelationIdFilter` créé par auto-config
- [ ] Bean overridable par `@ConditionalOnMissingBean`
- [ ] Correlation ID généré si absent (UUID)
- [ ] Correlation ID dans MDC pour les logs
- [ ] 3 tests minimum (default, override, génération)
- [ ] README avec exemple de configuration
- [ ] `mvn -pl foundation-core-starter -am clean verify` → BUILD SUCCESS
- [ ] Review reviewer : Accepted

---

## Phase 6 — foundation-api-starter

### Objectif
Conventions API REST : modèle d'erreur standard, exception handler global, validation.

### 🤖 Agent : `engineer`

### 📄 Prompt

```
Copilot Chat → agent "engineer" → /implement-starter

MODULE:    foundation-api-starter
GUIDELINE: docs/guidelines/api-guidelines.md
SCOPE:     @RestControllerAdvice global avec modèle d'erreur standard
           (timestamp, status, message, correlationId, path).
           Gestion @Valid, @NotNull, erreurs 4xx/5xx.
           Auto-configuration conditionnelle sur spring-webmvc.
```

### Livrables attendus

```
foundation-api-starter/src/main/java/fr/francetv/foundation/api/
  autoconfigure/
    ApiAutoConfiguration.java
  error/
    ApiErrorResponse.java       ← record immuable
    GlobalExceptionHandler.java ← @RestControllerAdvice
  properties/
    ApiProperties.java
```

### Tests à écrire

```java
// Test avec @WebMvcTest + slice
@WebMvcTest
class GlobalExceptionHandlerTest {

    @Test
    void shouldReturn400ForValidationError() { ... }

    @Test
    void shouldReturn404ForResourceNotFound() { ... }

    @Test
    void shouldIncludeCorrelationIdInErrorResponse() { ... }

    @Test
    void shouldReturn500ForUnhandledException() { ... }
}
```

### Validation

```bash
mvn -pl foundation-api-starter -am clean verify
```

### ✅ DoD Phase 6
- [ ] `ApiErrorResponse` est un record immuable avec correlationId
- [ ] `GlobalExceptionHandler` géré par `@ConditionalOnMissingBean`
- [ ] HTTP 400/404/500 couverts
- [ ] Tests de slice `@WebMvcTest` passent
- [ ] README avec exemple de réponse d'erreur JSON
- [ ] `mvn -pl foundation-api-starter -am clean verify` → BUILD SUCCESS
- [ ] Review reviewer : Accepted

---

## Phase 7 — foundation-logging-starter

### Objectif
Configuration des logs structurés, format JSON, intégration MDC + Correlation ID.

### 🤖 Agent : `engineer`

### 📄 Prompt

```
Copilot Chat → agent "engineer" → /implement-starter

MODULE:    foundation-logging-starter
GUIDELINE: docs/guidelines/coding-guidelines.md (section 7 Logging)
SCOPE:     Configuration Logback JSON (logstash-logback-encoder ou similaire),
           appender structuré, MDC automatique avec correlationId, applicationName.
           Auto-configuration conditionnelle sur présence de Logback.
```

### Livrables attendus

```
foundation-logging-starter/src/main/java/fr/francetv/foundation/logging/
  autoconfigure/
    LoggingAutoConfiguration.java
  properties/
    LoggingProperties.java

foundation-logging-starter/src/main/resources/
  META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports
  logback-spring.xml              ← configuration Logback par défaut (JSON)
```

### Tests à écrire

```java
class LoggingAutoConfigurationTest {

    @Test
    void shouldLoadWithoutError() {
        new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(LoggingAutoConfiguration.class))
            .run(ctx -> assertThat(ctx).hasNotFailed());
    }

    @Test
    void shouldBindLoggingProperties() {
        new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(LoggingAutoConfiguration.class))
            .withPropertyValues("foundation.logging.json-format=false")
            .run(ctx ->
                assertThat(ctx.getBean(LoggingProperties.class).jsonFormat())
                    .isFalse());
    }
}
```

### Validation

```bash
mvn -pl foundation-logging-starter -am clean verify
```

### ✅ DoD Phase 7
- [ ] Logs structurés JSON par défaut
- [ ] `correlationId` dans chaque ligne de log via MDC
- [ ] Format désactivable via propriété
- [ ] Aucun token ni credential dans les logs
- [ ] Tests passent
- [ ] `mvn -pl foundation-logging-starter -am clean verify` → BUILD SUCCESS
- [ ] Review reviewer : Accepted

---

## Phase 8 — foundation-observability-starter

### Objectif
Actuator, endpoints health/liveness/readiness/metrics/prometheus, Micrometer, OpenTelemetry.

### 🤖 Agent : `engineer`

### 📄 Prompt

```
Copilot Chat → agent "engineer" → /implement-starter

MODULE:    foundation-observability-starter
GUIDELINE: docs/guidelines/observability-guidelines.md
SCOPE:     Configuration Actuator (expose health, metrics, prometheus, info).
           Probes liveness/readiness activés.
           Tags Micrometer communs (application, environment).
           Auto-configuration OpenTelemetry si présent sur le classpath.
```

### Livrables attendus

```
foundation-observability-starter/src/main/java/fr/francetv/foundation/observability/
  autoconfigure/
    ObservabilityAutoConfiguration.java
  properties/
    ObservabilityProperties.java

foundation-observability-starter/src/main/resources/
  META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports
```

### Contenu clé auto-configuration

```java
@Bean
@ConditionalOnMissingBean(MeterRegistryCustomizer.class)
public MeterRegistryCustomizer<MeterRegistry> commonTags(ObservabilityProperties props) {
    return registry -> registry.config()
        .commonTags("application", props.applicationName());
}
```

### Tests à écrire

```java
@SpringBootTest(webEnvironment = RANDOM_PORT)
class ObservabilityEndpointsTest {

    @Test
    void healthEndpointShouldBeAccessible() { ... }

    @Test
    void prometheusEndpointShouldReturnMetrics() { ... }

    @Test
    void livenessProbeShouldBeAvailable() { ... }

    @Test
    void readinessProbeShouldBeAvailable() { ... }
}
```

### Validation

```bash
mvn -pl foundation-observability-starter -am clean verify
```

### ✅ DoD Phase 8
- [ ] `/actuator/health`, `/actuator/health/liveness`, `/actuator/health/readiness` exposés
- [ ] `/actuator/prometheus` expose des métriques
- [ ] Tags communs Micrometer configurés
- [ ] Tests d'endpoints passent
- [ ] README avec la configuration YAML minimale
- [ ] `mvn -pl foundation-observability-starter -am clean verify` → BUILD SUCCESS
- [ ] Review reviewer : Accepted

---

## Phase 9 — foundation-security-starter

### Objectif
OAuth2 Resource Server, JWT validation, SecurityFilterChain par défaut, IDP-agnostique.

### 🤖 Agent : `engineer`

### 📄 Prompt

```
Copilot Chat → agent "engineer" → /implement-starter

MODULE:    foundation-security-starter
GUIDELINE: docs/guidelines/security-guidelines.md
SCOPE:     SecurityFilterChain par défaut avec OAuth2 Resource Server JWT.
           Endpoints /actuator/health/** publics par défaut.
           Tout le reste protégé.
           Configuration via issuer-uri ou jwk-set-uri.
           Aucun adaptateur Keycloak.
```

### Structure auto-configuration

```java
@AutoConfiguration
@ConditionalOnClass(SecurityFilterChain.class)
@EnableConfigurationProperties(SecurityProperties.class)
public class SecurityAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(SecurityFilterChain.class)
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            SecurityProperties properties) throws Exception {
        http
            .authorizeHttpRequests(auth -> {
                properties.publicPaths()
                    .forEach(path -> auth.requestMatchers(path).permitAll());
                auth.anyRequest().authenticated();
            })
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));
        return http.build();
    }
}
```

### Tests à écrire

```java
@SpringBootTest(webEnvironment = RANDOM_PORT)
class SecurityAutoConfigurationTest {

    @Test
    void shouldReturn401ForProtectedEndpointWithoutToken() { ... }

    @Test
    void shouldReturn401ForInvalidToken() { ... }

    @Test
    void shouldReturn200ForValidToken() { ... }

    @Test
    void shouldAllowHealthEndpointWithoutToken() { ... }

    @Test
    void shouldRespectCustomSecurityFilterChain() { ... }
}
```

### Validation

```bash
mvn -pl foundation-security-starter -am clean verify
```

### 🤖 Review spécifique : agent `reviewer`

```
Copilot Chat → agent "reviewer"
→ /review-module  MODULE=foundation-security-starter
   "Attention particulière sur : aucun Keycloak, JWT configuré via issuer-uri,
    tokens non loggés, SecurityFilterChain overridable."
```

### ✅ DoD Phase 9
- [ ] Aucun adaptateur Keycloak ou IDP-spécifique
- [ ] JWT validation via `issuer-uri` ou `jwk-set-uri` uniquement
- [ ] `SecurityFilterChain` est `@ConditionalOnMissingBean`
- [ ] `/actuator/health/**` public par défaut
- [ ] Aucun token logué
- [ ] 5 scénarios de tests (public, protégé, sans token, token invalide, token valide)
- [ ] `mvn -pl foundation-security-starter -am clean verify` → BUILD SUCCESS
- [ ] Review reviewer : Accepted (vérification sécurité incluse)

---

## Phase 10 — foundation-mapping-starter

### Objectif
Configuration MapStruct : annotation processor, conventions de mapping partagées.

### 🤖 Agent : `engineer`

### 📄 Prompt

```
Copilot Chat → agent "engineer" → /implement-starter

MODULE:    foundation-mapping-starter
GUIDELINE: docs/guidelines/mapping-guidelines.md
SCOPE:     Configuration MapStruct annotation processor.
           Convention de base shared mapper config (@MapperConfig).
           Injection Spring (componentModel = "spring").
```

### Livrables attendus

```
foundation-mapping-starter/src/main/java/fr/francetv/foundation/mapping/
  autoconfigure/
    MappingAutoConfiguration.java
  config/
    FoundationMapperConfig.java   ← @MapperConfig partagé
```

### Contenu clé

```java
@MapperConfig(
    componentModel = MappingConstants.ComponentModel.SPRING,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    unmappedTargetPolicy = ReportingPolicy.WARN
)
public interface FoundationMapperConfig {}
```

### Tests à écrire

```java
class MappingAutoConfigurationTest {

    @Test
    void shouldLoadWithoutError() {
        new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(MappingAutoConfiguration.class))
            .run(ctx -> assertThat(ctx).hasNotFailed());
    }
}
```

### Validation

```bash
mvn -pl foundation-mapping-starter -am clean verify
```

### ✅ DoD Phase 10
- [ ] `FoundationMapperConfig` disponible pour les services consommateurs
- [ ] `componentModel = "spring"` configuré par défaut
- [ ] Annotation processor MapStruct déclaré dans le POM
- [ ] Tests passent
- [ ] README avec exemple d'utilisation dans un service
- [ ] `mvn -pl foundation-mapping-starter -am clean verify` → BUILD SUCCESS

---

## Phase 11 — foundation-nats-starter

### Objectif
Connexion NATS, publisher, consumer, envelope standard, propagation Correlation ID.

### 🤖 Agent : `engineer`

### 📄 Prompt

```
Copilot Chat → agent "engineer" → /implement-starter

MODULE:    foundation-nats-starter
GUIDELINE: docs/guidelines/nats-guidelines.md
SCOPE:     Connexion NATS configurée via foundation.nats.server-url.
           NatsMessagePublisher avec envelope standard (id, correlationId, source, type, timestamp, payload).
           Sérialisation JSON (Jackson).
           @ConditionalOnClass(Connection.class).
```

### Livrables attendus

```
foundation-nats-starter/src/main/java/fr/francetv/foundation/nats/
  autoconfigure/
    NatsAutoConfiguration.java
  publisher/
    NatsMessagePublisher.java
    MessageEnvelope.java          ← record immuable
  properties/
    NatsProperties.java
```

### Structure de l'enveloppe

```java
public record MessageEnvelope(
        String id,
        String correlationId,
        String source,
        String type,
        Instant timestamp,
        Object payload
) {}
```

### Tests à écrire

```java
@Testcontainers
class NatsAutoConfigurationIntegrationTest {

    @Container
    static GenericContainer<?> natsServer =
        new GenericContainer<>("nats:2.10-alpine")
            .withExposedPorts(4222);

    @Test
    void shouldConnectToNats() { ... }

    @Test
    void shouldPublishMessageWithEnvelope() { ... }

    @Test
    void shouldIncludeCorrelationIdInEnvelope() { ... }
}

class NatsAutoConfigurationTest {

    @Test
    void shouldNotCreateBeanWhenNatsClientAbsent() { ... }

    @Test
    void shouldCreatePublisherWhenNatsPresent() { ... }
}
```

### Validation

```bash
mvn -pl foundation-nats-starter -am clean verify
```

### ✅ DoD Phase 11
- [ ] Connexion NATS configurée et testée via Testcontainers
- [ ] Enveloppe avec `correlationId` obligatoire
- [ ] `@ConditionalOnClass(Connection.class)` — aucun bean si NATS absent
- [ ] Tests unitaires + intégration passent
- [ ] README avec exemple publish/subscribe et configuration YAML
- [ ] `mvn -pl foundation-nats-starter -am clean verify` → BUILD SUCCESS
- [ ] Review reviewer (contrôle idempotence, enveloppe) : Accepted

---

## Phase 12 — foundation-http-client-starter

### Objectif
WebClient configuré avec timeout, auth Bearer, propagation X-Correlation-Id, base URL par client.

### 🤖 Agent : `engineer`

### 📄 Prompt

```
Copilot Chat → agent "engineer" → /implement-starter

MODULE:    foundation-http-client-starter
GUIDELINE: docs/guidelines/client-generation-guidelines.md
SCOPE:     WebClient.Builder pré-configuré avec ExchangeFilterFunction pour :
           - propagation X-Correlation-Id
           - injection Bearer token depuis SecurityContext
           Timeouts configurables par client (foundation.http-client.{name}.connect-timeout).
           @ConditionalOnClass(WebClient.class).
```

### Livrables attendus

```
foundation-http-client-starter/src/main/java/fr/francetv/foundation/httpclient/
  autoconfigure/
    HttpClientAutoConfiguration.java
  filter/
    CorrelationIdExchangeFilter.java
    BearerTokenExchangeFilter.java
  properties/
    HttpClientProperties.java     ← Map<String, ClientConfig>
```

### Tests à écrire

```java
@ExtendWith(WireMockExtension.class)
class CorrelationIdFilterTest {

    @Test
    void shouldPropagateCorrelationIdHeader() {
        // WireMock vérifie que X-Correlation-Id est présent dans la requête sortante
    }

    @Test
    void shouldTimeoutAfterConfiguredDuration() { ... }

    @Test
    void shouldReturn4xxWithoutRetry() { ... }
}
```

### Validation

```bash
mvn -pl foundation-http-client-starter -am clean verify
```

### ✅ DoD Phase 12
- [ ] `X-Correlation-Id` propagé dans chaque requête sortante
- [ ] Timeout connect + read configurables par client
- [ ] `@ConditionalOnMissingBean` sur WebClient.Builder
- [ ] Tests WireMock : 2xx, 4xx, 5xx, timeout
- [ ] Skill `ai/skills/openapi-client-generation/SKILL.md` cité dans le README
- [ ] `mvn -pl foundation-http-client-starter -am clean verify` → BUILD SUCCESS
- [ ] Review reviewer (contrôle correlation, timeout, pas de retry implicite) : Accepted

---

## Phase 13 — foundation-soap-client-starter

### Objectif
Apache CXF configuré : timeout, auth, propagation Correlation ID comme header HTTP/SOAP.

### 🤖 Agent : `engineer`

### 📄 Prompt

```
Copilot Chat → agent "engineer" → /implement-starter

MODULE:    foundation-soap-client-starter
GUIDELINE: docs/guidelines/client-generation-guidelines.md
SCOPE:     Configuration Apache CXF JaxWsProxyFactoryBean avec :
           - timeout connect/receive configurables
           - intercepteur CXF pour propagation X-Correlation-Id
           - support auth Basic et Bearer
           @ConditionalOnClass(JaxWsProxyFactoryBean.class).
```

### Tests à écrire

```java
class SoapClientAutoConfigurationTest {

    @Test
    void shouldNotCreateBeanWhenCxfAbsent() { ... }

    @Test
    void shouldCreateTimeoutInterceptorWhenCxfPresent() { ... }
}

// Test d'intégration : utiliser un serveur SOAP mock (ex: WireMock en mode SOAP)
class CorrelationIdSoapInterceptorTest {

    @Test
    void shouldAddCorrelationIdHeaderToOutboundMessage() { ... }
}
```

### Validation

```bash
mvn -pl foundation-soap-client-starter -am clean verify
```

### ✅ DoD Phase 13
- [ ] Timeout configurables connect/receive
- [ ] `X-Correlation-Id` propagé dans les appels SOAP sortants
- [ ] `@ConditionalOnClass(JaxWsProxyFactoryBean.class)`
- [ ] Skill `ai/skills/wsdl-client-generation/SKILL.md` cité dans le README
- [ ] `mvn -pl foundation-soap-client-starter -am clean verify` → BUILD SUCCESS
- [ ] Review reviewer : Accepted

---

## Phase 14 — foundation-data-starter

### Objectif
Conventions JPA, Flyway, PostgreSQL, audit, transactions. Le module le plus complex du socle.

### 🤖 Agent : `engineer`

### 📄 Prompt dédié

```
Copilot Chat → agent "engineer" → coller contenu de ai/prompts/04-create-data-starter.prompt.md
```

### Livrables attendus

```
foundation-data-starter/src/main/java/fr/francetv/foundation/data/
  autoconfigure/
    DataAutoConfiguration.java
    FlywayAutoConfiguration.java    ← si configuration spécifique
  audit/
    AuditConfiguration.java         ← @EnableJpaAuditing
    AuditableEntity.java            ← classe abstraite avec createdAt/updatedAt
  properties/
    DataProperties.java
```

### Contenu clé

```java
// Défaut ddl-auto=validate appliqué automatiquement
@AutoConfiguration
@ConditionalOnClass(DataSource.class)
public class DataAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public DataSourceCustomizer ddlAutoValidate() {
        // Force ddl-auto=validate si non surchargé
    }
}
```

### Tests à écrire

```java
@Testcontainers
class DataAutoConfigurationIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres =
        new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void configure(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Test
    void flywayMigrationShouldRunAtStartup() { ... }

    @Test
    void ddlAutoShouldDefaultToValidate() { ... }

    @Test
    void openInViewShouldBeDisabled() { ... }

    @Test
    void auditFieldsShouldBePopulated() { ... }
}
```

### Validation

```bash
mvn -pl foundation-data-starter -am clean verify
# Testcontainers démarre automatiquement PostgreSQL
```

### 🤖 Review : agent `reviewer`

```
Copilot Chat → agent "reviewer"
→ /review-module MODULE=foundation-data-starter
   "Vérifier : ddl-auto=validate par défaut, open-in-view=false,
    aucune entité métier, migrations déléguées aux services consommateurs,
    Testcontainers pour tous les tests d'intégration."
```

### ✅ DoD Phase 14
- [ ] `ddl-auto=validate` par défaut (jamais `update`)
- [ ] `open-in-view=false` par défaut
- [ ] Flyway activé automatiquement si présent sur le classpath
- [ ] `@EnableJpaAuditing` configuré
- [ ] Aucune entité métier dans le starter
- [ ] Tests Testcontainers PostgreSQL passent
- [ ] `mvn -pl foundation-data-starter -am clean verify` → BUILD SUCCESS
- [ ] Review reviewer : Accepted

---

## Phase 15 — foundation-test-starter

### Objectif
Utilitaires de tests partagés : helpers Testcontainers, configuration WireMock, utilitaires JWT pour tests de sécurité.

### 🤖 Agent : `engineer` puis `test-engineer`

### 📄 Prompt

```
Copilot Chat → agent "engineer" → /implement-starter

MODULE:    foundation-test-starter
GUIDELINE: ai/instructions/testing-rules.md
SCOPE:     Dépendances test communes (scope=test).
           Helper pour JWT de test (génération d'un token valide pour @WithMockJwt).
           Configuration WireMock de base.
           @TestConfiguration pour Testcontainers réutilisables.
           IMPORTANT : aucune dépendance runtime en production.
```

### Livrables attendus

```
foundation-test-starter/src/main/java/fr/francetv/foundation/test/
  security/
    JwtTestUtils.java             ← génère un JWT de test signé avec clé RSA temporaire
  wiremock/
    WireMockSupport.java          ← configuration WireMock de base
  containers/
    FoundationPostgresContainer.java  ← conteneur PostgreSQL pré-configuré
```

### 🔴 Erreur critique
Le test-starter ne doit JAMAIS avoir de scope `compile`. Vérifier dans le POM :
```xml
<scope>test</scope>  ← obligatoire dans les modules consommateurs
```

### Tests à écrire

```java
class JwtTestUtilsTest {
    @Test
    void shouldGenerateValidJwtToken() { ... }

    @Test
    void shouldCreateTokenWithCustomClaims() { ... }
}
```

### Validation

```bash
mvn -pl foundation-test-starter -am clean verify
# Vérifier que le JAR produit ne contient que des classes de test
```

### 🤖 Review : agent `test-engineer`

```
Copilot Chat → agent "test-engineer"
→ "Revue du foundation-test-starter.
   Vérifier que aucune dépendance runtime ne fuite en production.
   Contrôler les helpers JWT, WireMock et Testcontainers."
```

### ✅ DoD Phase 15
- [ ] JWT helper fonctionnel (génère token RSA signé)
- [ ] WireMock helper configure automatiquement le port
- [ ] PostgreSQL container helper avec configuration pré-remplie
- [ ] Aucune dépendance runtime (tout en scope `test` ou `provided`)
- [ ] Tests du helper passent
- [ ] `mvn -pl foundation-test-starter -am clean verify` → BUILD SUCCESS
- [ ] Review test-engineer : Accepted

---

## Phase 16 — foundation-sample-service

### Objectif
Service exemple assemblant un sous-ensemble réaliste de starters. Sert de validation end-to-end du socle.

### 🤖 Agent : `engineer`

### 📄 Prompt

```
Copilot Chat → agent "engineer"

MODULE: foundation-sample-service
SCOPE:  Service Spring Boot minimal utilisant :
        - foundation-core-starter
        - foundation-api-starter
        - foundation-logging-starter
        - foundation-observability-starter
        - foundation-security-starter
        Un endpoint GET /api/v1/hello qui retourne un message.
        Pas de base de données, pas de NATS.
        Démontrer : correlation ID dans les logs,
        endpoint protégé par JWT, réponse d'erreur standard.
GUIDELINE: docs/guidelines/api-guidelines.md + security-guidelines.md
```

### Tests à écrire

```java
@SpringBootTest(webEnvironment = RANDOM_PORT)
class SampleServiceSmokeTest {

    @Test
    void applicationContextLoadsSuccessfully() { }

    @Test
    void healthEndpointIsPublic() { ... }

    @Test
    void helloEndpointRequiresAuthentication() { ... }

    @Test
    void helloEndpointReturnsCorrelationId() { ... }

    @Test
    void errorResponseFollowsStandardModel() { ... }
}
```

### Validation

```bash
mvn -pl foundation-sample-service -am clean verify
mvn -pl foundation-sample-service spring-boot:run
# Vérifier manuellement :
curl http://localhost:8080/actuator/health
curl http://localhost:8080/api/v1/hello          # doit retourner 401
curl -H "Authorization: Bearer <token>" http://localhost:8080/api/v1/hello
```

### 🤖 Review finale : agent `architect`

```
Copilot Chat → agent "architect"
→ "Revue finale de foundation-sample-service.
   Vérifier qu'il n'embarque que des starters foundation,
   aucune logique métier réelle, sert uniquement de démonstration.
   Vérifier que tous les starters utilisés s'assemblent sans conflit."
```

### ✅ DoD Phase 16
- [ ] Application démarre sans erreur
- [ ] Endpoint GET /api/v1/hello fonctionne avec JWT valide
- [ ] X-Correlation-Id présent dans les logs et la réponse
- [ ] /actuator/health accessible sans token
- [ ] Smoke tests passent
- [ ] `mvn -pl foundation-sample-service -am clean verify` → BUILD SUCCESS
- [ ] Review architect : Accepted

---

## Phase 17 — Validation globale

### Objectif
Vérification complète de tout le socle en une seule commande.

### Commande

```bash
# Build complet
mvn clean verify

# Build sans les tests d'intégration (rapide)
mvn clean verify -DskipITs

# Avec rapport de couverture
mvn clean verify jacoco:report
```

### Vérifications manuelles

```bash
# Vérifier que le BOM gère bien toutes les versions
mvn -pl foundation-bom dependency:display-ancestors

# Vérifier les dépendances transitives indésirables
mvn dependency:tree -Dincludes=runtime

# Vérifier qu'aucun starter n'a de dépendance cyclique
mvn -pl foundation-core-starter dependency:tree
```

### 🤖 Review finale globale : agent `architect`

```
Copilot Chat → agent "architect"
→ "Revue globale de foundation-platform.
   Vérifier la cohérence des modules, l'absence de dépendances cycliques,
   et que la composition par dépendances fonctionne correctement.
   Comparer avec docs/architecture.md section 4."
```

### ✅ DoD Phase 17
- [ ] `mvn clean verify` → BUILD SUCCESS sur TOUS les modules
- [ ] Aucun test en échec
- [ ] Aucune dépendance cyclique
- [ ] `foundation-sample-service` démarre et répond correctement
- [ ] Review architect globale : Accepted
- [ ] `git tag v0.0.1-SNAPSHOT`

---

## Récapitulatif — Tableau de bord

| Phase | Module | Agent principal | Prompt | Statut |
|---|---|---|---|---|
| 0 | Environnement | - | - | ⬜ |
| 1 | Bootstrap Maven | engineer | 00-bootstrap-repo | ⬜ |
| 2 | foundation-parent | engineer | 01-create-parent-bom | ⬜ |
| 3 | foundation-bom | engineer | 01-create-parent-bom | ⬜ |
| 4 | foundation-common | engineer | /implement-starter | ⬜ |
| 5 | foundation-core-starter | engineer | /implement-starter | ⬜ |
| 6 | foundation-api-starter | engineer | /implement-starter | ⬜ |
| 7 | foundation-logging-starter | engineer | /implement-starter | ⬜ |
| 8 | foundation-observability-starter | engineer | /implement-starter | ⬜ |
| 9 | foundation-security-starter | engineer | /implement-starter | ⬜ |
| 10 | foundation-mapping-starter | engineer | /implement-starter | ⬜ |
| 11 | foundation-nats-starter | engineer | /implement-starter | ⬜ |
| 12 | foundation-http-client-starter | engineer | /implement-starter | ⬜ |
| 13 | foundation-soap-client-starter | engineer | /implement-starter | ⬜ |
| 14 | foundation-data-starter | engineer | 04-create-data-starter | ⬜ |
| 15 | foundation-test-starter | engineer+test-engineer | /implement-starter | ⬜ |
| 16 | foundation-sample-service | engineer | manuel | ⬜ |
| 17 | Validation globale | architect | - | ⬜ |

Remplacer ⬜ par ✅ au fur et à mesure.

---

## Règles absolues à respecter tout au long du développement

1. **Ne jamais passer à la phase suivante** si `mvn clean verify` échoue.
2. **Chaque starter** doit avoir ses tests avant d'être déclaré terminé.
3. **Chaque phase** se termine par une review avec l'agent approprié.
4. **Jamais de `ddl-auto=update`** dans aucun fichier de configuration.
5. **Jamais de dépendance runtime** dans `foundation-parent`.
6. **Jamais de clients générés** (REST/SOAP) dans les modules foundation.
7. **Jamais de code métier** dans les starters.
8. **Jamais d'adaptateur Keycloak** ou IDP-spécifique.
9. **Tout bean auto-configuré** doit être `@ConditionalOnMissingBean`.
10. **Toute propriété** doit être typée via `@ConfigurationProperties`.
