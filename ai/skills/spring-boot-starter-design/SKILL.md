# Skill - Spring Boot Starter Design

## Purpose

Use this skill when creating or reviewing a `foundation-platform` Spring Boot starter.

The goal is to design starters that feel natural to Spring Boot developers and avoid unnecessary framework abstractions.

## When to use

Use this skill for tasks involving:

- any `foundation-*-starter` module
- auto-configuration design
- configuration properties design
- conditional beans
- starter dependency structure

---

## Core design principle

A starter provides one technical capability.

A service uses a capability by declaring the starter dependency.

Properties configure the capability. Properties do not activate it.

---

## Starter responsibilities

A starter may provide:

- dependencies
- auto-configuration
- default beans
- configuration properties
- extension points
- documentation
- tests

A starter must not provide:

- business logic
- service-specific clients
- service-specific entities
- service-specific controllers
- deployment artifacts

---

## Auto-configuration template

```java
@AutoConfiguration
@EnableConfigurationProperties(MyProperties.class)
@ConditionalOnClass(SomeClass.class)
public class MyAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public MyService myService(MyProperties properties) {
        return new MyService(properties);
    }
}
```

Register in:
```
src/main/resources/META-INF/spring/
  org.springframework.boot.autoconfigure.AutoConfiguration.imports
```

---

## Configuration properties template

```java
@ConfigurationProperties(prefix = "foundation.{capability}")
public record MyProperties(
        String someValue,
        Duration timeout) {
}
```

---

## Test template

Use `ApplicationContextRunner` for auto-configuration tests:

```java
private final ApplicationContextRunner contextRunner =
    new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(MyAutoConfiguration.class));

@Test
void shouldCreateDefaultBean() {
    contextRunner.run(ctx -> assertThat(ctx).hasSingleBean(MyService.class));
}

@Test
void shouldRespectBeanOverride() {
    contextRunner
        .withUserConfiguration(CustomConfig.class)
        .run(ctx -> assertThat(ctx).hasSingleBean(MyService.class)
            .getBean(MyService.class).isInstanceOf(CustomMyService.class));
}
```

---

## Checklist

- [ ] Auto-configuration is conditional (`@ConditionalOnClass`, `@ConditionalOnMissingBean`).
- [ ] Beans can be overridden by consumers.
- [ ] Configuration properties are typed and only added when needed.
- [ ] `AutoConfiguration.imports` is present.
- [ ] Tests cover default bean, override, and property binding.
- [ ] README documents all properties and a usage example.
- [ ] No business logic in the starter.
- [ ] No optional runtime deps forced on all services.

---

## Extension-friendly starters

When a starter's bean is designed to be extended by consuming services (e.g. a
`@RestControllerAdvice` that teams add handlers to), make reusable methods `protected`:

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    // constant accessible by subclasses
    protected static final String MDC_KEY = "correlationId";

    // callable by subclasses to build a consistent response
    protected ResponseEntity<ApiErrorResponse> buildResponse(
            HttpStatus status, String message, HttpServletRequest request) { ... }

    // callable by subclasses to reuse the same correlation ID resolution
    protected String resolveCorrelationId(HttpServletRequest request) { ... }
}
```

Document the extension pattern in the README:

```java
@RestControllerAdvice
public class MyExceptionHandler extends GlobalExceptionHandler {

    public MyExceptionHandler(ApiProperties properties) {
        super(properties);
    }

    @ExceptionHandler(MyDomainException.class)
    public ResponseEntity<ApiErrorResponse> handleDomain(
            MyDomainException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage(), request);
    }
}
```

Because `GlobalExceptionHandler` is registered with `@ConditionalOnMissingBean`, the
auto-configuration backs off when the consuming service declares its own bean.

---

## @WebMvcTest integration

If your starter provides a `@ControllerAdvice` or other MVC infrastructure, register the
auto-configuration in the web MVC test slice so consuming services get it automatically
in `@WebMvcTest` contexts:

```
src/main/resources/META-INF/spring/
  org.springframework.boot.test.autoconfigure.web.mvc.AutoConfigureWebMvc.imports
```

Content (same class as `AutoConfiguration.imports`):

```
fr.francetv.foundation.{capability}.autoconfigure.CapabilityAutoConfiguration
```

This makes `@WebMvcTest` in consuming services include the starter's MVC beans without
any extra `@Import` annotation on the test class.

---

## Testing MVC starters with standalone MockMvc

When `spring-boot-test-autoconfigure` is limited or `@WebMvcTest` is not available, use
`MockMvcBuilders.standaloneSetup` — it is equivalent for testing a `@RestControllerAdvice`:

```java
@BeforeEach
void setUp() {
    GlobalExceptionHandler handler = new GlobalExceptionHandler(new MyProperties(false));
    TestController controller = new TestController();

    mockMvc = MockMvcBuilders.standaloneSetup(controller)
            .setControllerAdvice(handler)
            .build();
}
```

**Note on date/time types in response records**: prefer `String` (ISO-8601 formatted via
`DateTimeFormatter.ISO_INSTANT`) over `Instant` for response records in library starters.
`Instant` requires `JavaTimeModule` to be registered in the consuming service's `ObjectMapper`.
Using `String` avoids a hidden runtime requirement and keeps serialization predictable
without additional configuration:

```java
// in buildResponse():
DateTimeFormatter.ISO_INSTANT.format(Instant.now())  // → "2026-07-15T10:00:00Z"
```