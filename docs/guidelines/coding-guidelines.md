# Coding Guidelines - foundation-platform

## 1. Purpose

This document defines coding standards for `foundation-platform`.

The goal is to keep the codebase:

- simple
- explicit
- maintainable
- testable
- close to Spring Boot conventions
- easy for consuming teams to understand

---

## 2. Java baseline

Use Java 21.

All modules must compile with Java 21.

Do not use preview features unless explicitly approved by an ADR.

---

## 3. General coding principles

Prefer:

- simple code over clever code
- explicit configuration over hidden behavior
- composition over inheritance
- constructor injection over field injection
- small classes with clear responsibilities
- standard Spring Boot mechanisms

Avoid:

- unnecessary abstractions
- deep inheritance trees
- global static helpers
- reflection unless justified
- business logic inside foundation modules
- framework code that is difficult to debug

---

## 4. Package naming

Use the base package:

```text
fr.francetv.foundation
```

Packages should be organized by feature rather than by technical layer whenever possible.

Example:

```text
fr.francetv.foundation.nats
fr.francetv.foundation.observability
fr.francetv.foundation.security
```

Avoid generic package names such as:

```text
util
common
helpers
misc
```

---

## 5. Dependency injection

Always use constructor injection.

Example:

```java
@Service
public class EventPublisher {

    private final NatsConnection natsConnection;

    public EventPublisher(NatsConnection natsConnection) {
        this.natsConnection = natsConnection;
    }
}
```

Do not use field injection:

```java
@Autowired
private NatsConnection natsConnection;
```

Constructor injection:

- makes dependencies explicit
- improves testability
- supports immutable fields

---

## 6. Configuration

Use Spring Boot configuration properties for external configuration.

Example:

```java
@ConfigurationProperties(prefix = "foundation.nats")
public record NatsProperties(
        String subject,
        Duration timeout) {
}
```

Prefer typed configuration over direct access to environment properties.

Avoid:

```java
environment.getProperty("foundation.nats.subject");
```

Configuration should be validated where appropriate.

---

## 7. Logging

Use SLF4J with structured and meaningful messages.

Example:

```java
log.info("Publishing message to subject {}", subject);
```

Avoid:

```java
log.info("Here");
log.info("Process started...");
```

Rules:

- log business-relevant events
- use `INFO` sparingly
- use `DEBUG` for diagnostic information
- never log secrets
- never log passwords, tokens, or credentials

---

## 8. Exceptions

Use explicit exception types.

Example:

```java
throw new ConfigurationException(
        "Missing NATS subject configuration");
```

Avoid:

```java
throw new RuntimeException("Error");
```

Exception messages should:

- be actionable
- explain the cause
- help operators diagnose issues

---

## 9. Testing

Every new feature must include automated tests.

Prefer:

- unit tests for business behavior
- slice tests when Spring functionality is involved
- integration tests only when necessary

Test names should describe behavior.

Example:

```java
shouldPublishMessageWhenConfigurationIsValid()
```

Avoid:

```java
testPublish()
```

---

## 10. API design

Public APIs must be:

- stable
- documented
- backward compatible whenever possible

Prefer immutable objects.

Use records for simple value objects:

```java
public record EventMetadata(
        String source,
        Instant timestamp) {
}
```

Avoid mutable DTOs unless required by a framework.

---

## 11. Auto-configuration

Follow Spring Boot auto-configuration conventions.

Rules:

- create beans only when necessary
- use conditional annotations
- provide sensible defaults
- allow consumers to override beans

Example:

```java
@Bean
@ConditionalOnMissingBean
MessagePublisher messagePublisher() {
    return new NatsMessagePublisher();
}
```

---

## 12. Documentation

All modules must contain:

- a README
- usage examples
- configuration reference
- migration notes when applicable

Complex decisions must be captured using ADRs.

Documentation should explain:

- why a decision was made
- alternatives considered
- consequences

---

## 13. Dependency management

Before adding a new dependency:

- verify that Spring Boot does not already provide the functionality
- evaluate maintenance and security implications
- document the reason for introducing it

Prefer:

- Spring Boot managed dependencies
- widely adopted libraries
- dependencies with active maintenance

Avoid duplicate libraries that solve the same problem.

---

## 14. Definition of done

A contribution is considered complete when:

- code compiles with Java 21
- automated tests pass
- documentation is updated
- configuration is documented
- code follows these guidelines
- reviewers can easily understand and maintain the implementation