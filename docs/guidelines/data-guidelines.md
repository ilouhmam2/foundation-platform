# Data Guidelines - foundation-platform

## 1. Purpose

This document defines data access and migration standards for services using `foundation-platform`.

The goal is to standardize:

- database access
- JPA configuration
- Flyway migrations
- transaction boundaries
- audit fields
- integration tests

## 2. Supported database

The default relational database is:

```text
PostgreSQL
```

The PostgreSQL JDBC driver must be declared explicitly by consuming services.

---

## 3. Flyway conventions

All schema changes must be managed by Flyway.

Rules:
- Migrations belong to consuming services, not to `foundation-platform`
- Migration files live in `src/main/resources/db/migration`
- Naming convention: `V{version}__{description}.sql`
- `spring.flyway.enabled=true` by default when Flyway is on the classpath

Forbidden:
- `spring.jpa.hibernate.ddl-auto=update`
- Manual schema changes outside Flyway migrations

---

## 4. JPA configuration

Default `ddl-auto` must be `validate`.

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: validate
    open-in-view: false
```

`open-in-view: false` is mandatory to prevent lazy-loading leaks in HTTP threads.

---

## 5. Entity rules

- JPA entities must never be exposed directly in REST APIs
- Use DTOs and MapStruct for all API-facing mapping
- Entity classes should stay internal to the data layer

---

## 6. Transaction boundaries

- Transactions belong to the service layer, not the repository layer
- Annotate service methods with `@Transactional`
- Avoid `@Transactional` on repository methods unless explicitly justified

---

## 7. Repository rules

- Use Spring Data JPA repositories
- Keep repositories simple and focused
- Complex queries should use JPQL or native queries with clear naming
- Repositories must not be exposed directly to REST controllers

---

## 8. Audit fields

Recommended audit fields:

```java
@CreatedDate
private Instant createdAt;

@LastModifiedDate
private Instant updatedAt;
```

Enable Spring Data JPA auditing with `@EnableJpaAuditing`.

---

## 9. Integration tests

Use Testcontainers for PostgreSQL integration tests.

```java
@Testcontainers
class MyRepositoryTest {

    @Container
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }
}
```

Integration tests must not require manually started services.