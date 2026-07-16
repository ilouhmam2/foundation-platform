# foundation-test-starter

Shared test utilities for consuming services.

> **Important**: always declare this starter with `<scope>test</scope>` in consuming modules.
> It must never reach the production classpath.

---

## What it provides

| Utility | Description |
|---|---|
| `JwtTestUtils` | Generates signed JWT tokens using an ephemeral RSA key pair |
| `WireMockSupport` | Abstract base class with a ready-to-use WireMock server |
| `FoundationPostgresContainer` | Singleton Testcontainers PostgreSQL container |

---

## Usage

### Declaring the dependency

```xml
<dependency>
    <groupId>fr.francetv.foundation</groupId>
    <artifactId>foundation-test-starter</artifactId>
    <scope>test</scope>
</dependency>
```

---

### JwtTestUtils — generating test tokens

`JwtTestUtils` is auto-configured as a Spring bean. Inject it in your test configuration
to generate tokens signed with the same RSA key across your test suite.

```java
@SpringBootTest
class MyControllerTest {

    @Autowired
    JwtTestUtils jwtTestUtils;

    @Test
    void shouldReturnDataForAuthenticatedUser() {
        String token = jwtTestUtils.generateToken("user@example.com");
        // use token in Authorization: Bearer <token>
    }

    @Test
    void shouldEnforceScope() {
        String token = jwtTestUtils.generateToken("svc", Map.of("scope", "read:data"));
        // ...
    }
}
```

To configure a `JwtDecoder` that validates the generated tokens, use the public RSA key:

```java
@TestConfiguration
public class TestSecurityConfig {

    @Bean
    JwtDecoder testJwtDecoder(JwtTestUtils jwtTestUtils) throws Exception {
        return NimbusJwtDecoder
            .withPublicKey(jwtTestUtils.getPublicRsaKey().toRSAPublicKey())
            .build();
    }
}
```

#### Overriding the bean

If you need a custom `JwtTestUtils` in a specific module, declare your own bean:

```java
@TestConfiguration
public class CustomJwtConfig {

    @Bean
    JwtTestUtils customJwtTestUtils() {
        return new JwtTestUtils();
    }
}
```

---

### WireMockSupport — stubbing HTTP calls

`WireMockSupport` is a JUnit 5 extension. Declare it as a `static @RegisterExtension` field
in your test class. Each test class gets its own independent server — safe for parallel execution.

```java
class ExternalApiClientTest {

    @RegisterExtension
    static WireMockSupport wireMock = WireMockSupport.create();

    @Test
    void shouldCallExternalService() {
        wireMock.server().stubFor(get("/api/resource")
            .willReturn(okJson("{\"id\": 1, \"name\": \"test\"}")));

        String result = myClient.fetch(wireMock.baseUrl() + "/api/resource");

        assertThat(result).contains("test");
    }
}
```

---

### FoundationPostgresContainer — PostgreSQL integration tests

Use the singleton container in a shared `@TestConfiguration` to start PostgreSQL once per JVM:

```java
@TestConfiguration
public class PostgresTestConfig {

    static {
        FoundationPostgresContainer.getInstance().start();
    }
}
```

Spring Boot datasource properties are set automatically on `start()`:
- `spring.datasource.url`
- `spring.datasource.username`
- `spring.datasource.password`

Then use it in your `@SpringBootTest`:

```java
@SpringBootTest
@Import(PostgresTestConfig.class)
class OrderRepositoryTest {

    @Autowired
    OrderRepository orderRepository;

    @Test
    void shouldPersistOrder() { ... }
}
```

---

## Properties

This starter has no `foundation.*` properties. `JwtTestUtils` is conditionally activated
by classpath presence of `nimbus-jose-jwt`.
