# foundation-data-starter

Spring Data JPA, Flyway, and PostgreSQL conventions for foundation-platform services.

## What this starter provides

- Default `ddl-auto=validate` (prevents accidental schema changes)
- Default `open-in-view=false` (prevents lazy-loading leaks in HTTP threads)
- JPA auditing enabled via `@EnableJpaAuditing`
- `AuditableEntity` base class with `createdAt` / `updatedAt` fields

All defaults are applied at the **lowest property priority** and can be overridden
in the consuming service's `application.yml`.

## Usage

Add the dependency to your service:

```xml
<dependency>
    <groupId>fr.francetv.foundation</groupId>
    <artifactId>foundation-data-starter</artifactId>
</dependency>
```

You must also declare the PostgreSQL driver and Flyway PostgreSQL dialect explicitly:

```xml
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-database-postgresql</artifactId>
    <scope>runtime</scope>
</dependency>
```

## Audit support

Extend `AuditableEntity` to get automatic `createdAt` / `updatedAt` population:

```java
@Entity
@Table(name = "orders")
public class Order extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ...
}
```

No additional configuration is required. `@EnableJpaAuditing` is activated automatically
when `spring-boot-starter-data-jpa` is on the classpath.

To override auditing, declare your own `@EnableJpaAuditing` configuration — the starter
will back off automatically.

## Flyway migrations

Place migration scripts in `src/main/resources/db/migration` using the naming convention:

```
V{version}__{description}.sql

V1__create_orders.sql
V2__add_status_to_orders.sql
```

Flyway is enabled automatically when `flyway-core` is on the classpath.

## Configuration reference

| Property | Default | Description |
|---|---|---|
| `spring.jpa.hibernate.ddl-auto` | `validate` | Hibernate DDL mode. Never set to `update`. |
| `spring.jpa.open-in-view` | `false` | Disables the Open Session in View anti-pattern. |
| `spring.flyway.enabled` | `true` | Standard Spring Boot Flyway property. |

## Overriding defaults

```yaml
# application.yml - consuming service
spring:
  jpa:
    hibernate:
      ddl-auto: none   # override foundation default for this service
```

## Integration tests

Use Testcontainers in your service integration tests:

```java
@SpringBootTest
@Testcontainers
class OrderRepositoryTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void configure(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private OrderRepository orderRepository;

    @Test
    void shouldPersistAndRetrieveOrder() {
        // ...
    }
}
```
