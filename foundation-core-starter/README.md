# foundation-core-starter

Provides automatic extraction and propagation of `X-Correlation-Id` for servlet-based Spring Boot services.

When the starter is on the classpath and the application is a servlet web application, a `CorrelationIdFilter` is registered automatically. The filter:

- Reads `X-Correlation-Id` from the incoming request header.
- Generates a UUID if the header is absent or blank.
- Writes the correlation ID to the SLF4J MDC under the key `correlationId`.
- Propagates the correlation ID back to the caller via the `X-Correlation-Id` response header.
- Removes the MDC entry after the request completes (in a `finally` block).

---

## Activation

Add the starter to your service:

```xml
<dependency>
    <groupId>fr.francetv.foundation</groupId>
    <artifactId>foundation-core-starter</artifactId>
</dependency>
```

The filter is activated automatically when:

- `spring-web` (`OncePerRequestFilter`) is on the classpath, and
- the application is a servlet web application (`spring.main.web-application-type=servlet`).

No properties are required.

---

## Log pattern configuration

Include `%X{correlationId}` in your Logback or Log4j2 pattern to output the correlation ID in every log line.

Logback example (`logback-spring.xml`):

```xml
<pattern>%d{HH:mm:ss} [%X{correlationId}] %-5level %logger{36} - %msg%n</pattern>
```

The MDC key constant is available programmatically:

```java
String key = CorrelationIdFilter.MDC_KEY; // "correlationId"
```

---

## Overriding the filter

To replace the default filter with a custom implementation, declare a bean of type `CorrelationIdFilter` in your configuration:

```java
@Configuration
public class MyConfig {

    @Bean
    public CorrelationIdFilter correlationIdFilter() {
        return new MyCustomCorrelationIdFilter();
    }
}
```

The auto-configured bean will not be registered when a bean of that type is already present.

---

## Configuration reference

This starter has no configuration properties. Behaviour is determined by classpath presence only.

| Property | Default | Description |
|---|---|---|
| — | — | No properties |

---

## Header reference

| Header | Direction | Description |
|---|---|---|
| `X-Correlation-Id` | Request | Read and propagated. Generated if absent. |
| `X-Correlation-Id` | Response | Set to the resolved or generated correlation ID. |
