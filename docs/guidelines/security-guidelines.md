# Security Guidelines - foundation-platform

## 1. Purpose

This document defines security standards for services using `foundation-platform`.

The goal is to provide consistent OAuth2/JWT security without coupling the foundation to a specific Identity Provider.

## 2. Security model

The target architecture uses:

```text
Consumer
    ->
Gravitee API Gateway
    ->
Spring Boot Microservice
```

The API Gateway handles authentication for most flows.

The Spring Boot service validates the JWT token passed by the gateway.

---

## 3. Required approach

Use Spring Security OAuth2 Resource Server.

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-oauth2-resource-server</artifactId>
</dependency>
```

Configuration via issuer URI:

```yaml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: https://your-idp.example.com/realms/your-realm
```

Alternative using JWK Set URI:

```yaml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          jwk-set-uri: https://your-idp.example.com/.well-known/jwks.json
```

---

## 4. Forbidden patterns

Do not use:

- Keycloak adapters (`keycloak-spring-boot-adapter`)
- IDP-specific Spring Security integrations
- Hardcoded client credentials in code
- Local user management inside foundation modules

---

## 5. SecurityFilterChain conventions

Provide a default `SecurityFilterChain` via auto-configuration.

The default chain must:
- Protect all endpoints by default
- Allow explicit public path configuration via properties
- Allow consuming services to override the bean entirely

Example:

```java
@Bean
@ConditionalOnMissingBean(SecurityFilterChain.class)
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/actuator/health/**").permitAll()
            .anyRequest().authenticated()
        )
        .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));
    return http.build();
}
```

---

## 6. Configuration properties

```java
@ConfigurationProperties(prefix = "foundation.security")
public record SecurityProperties(
        List<String> publicPaths) {
}
```

---

## 7. Sensitive data protection

- Never log JWT tokens
- Never log passwords or credentials
- Never include Authorization headers in application logs

---

## 8. Required tests

Security tests must cover:

- Public endpoint accessible without token
- Protected endpoint returns 401 without token
- Protected endpoint returns 401 with invalid token
- Protected endpoint returns 200 with valid token
- Role and scope mapping works correctly