# foundation-security-starter

Spring Boot starter that provides a default `SecurityFilterChain` using **OAuth2 Resource Server / JWT**.

---

## What it does

- Protects all endpoints by default (any unauthenticated request returns `401`)
- Allows explicit public path configuration via `foundation.security.public-paths`
- Exposes `/actuator/health` and `/actuator/health/**` as public by default
- Validates JWTs through standard Spring Security OAuth2 Resource Server
- Does **not** couple to any specific Identity Provider (no Keycloak adapters)

---

## Quick start

### 1. Add the dependency

```xml
<dependency>
    <groupId>fr.francetv.foundation</groupId>
    <artifactId>foundation-security-starter</artifactId>
</dependency>
```

### 2. Configure the JWT issuer

```yaml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: https://your-idp.example.com/realms/your-realm
```

Alternatively, use `jwk-set-uri` when the JWKS endpoint is known directly:

```yaml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          jwk-set-uri: https://your-idp.example.com/.well-known/jwks.json
```

---

## Configuration properties

| Property | Type | Default | Description |
|---|---|---|---|
| `foundation.security.public-paths` | `List<String>` | `/actuator/health`, `/actuator/health/**` | Ant-style paths permitted without authentication |

### Example: add a custom public path

> **Note — replace semantics.** Setting `public-paths` replaces the default list entirely.
> Include `/actuator/health` and `/actuator/health/**` explicitly if the health endpoint
> must remain public.

```yaml
foundation:
  security:
    public-paths:
      - /actuator/health
      - /actuator/health/**
      - /open/api/**
```

---

## Overriding the security chain

Declare your own `SecurityFilterChain` bean to take full control. The starter's default
chain backs off automatically via `@ConditionalOnMissingBean`.

```java
@Configuration
public class MySecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/actuator/health/**").permitAll()
                .requestMatchers("/internal/**").hasAuthority("SCOPE_internal")
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));
        return http.build();
    }
}
```

---

## Security model

```text
Consumer
    →
Gravitee API Gateway  (handles AuthN for most flows)
    →
Spring Boot Microservice  (validates JWT forwarded by the gateway)
```

The service validates the JWT token. It does **not** manage user identities or sessions.

---

## Forbidden patterns

- Do not use Keycloak adapters (`keycloak-spring-boot-adapter`)
- Do not use IDP-specific Spring Security integrations
- Do not log JWT tokens or Authorization headers
- Do not hardcode client credentials
