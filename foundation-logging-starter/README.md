# foundation-logging-starter

Structured JSON logging for Spring Boot microservices.

Provides:
- Logback JSON output via `logstash-logback-encoder`
- Automatic inclusion of MDC fields (`correlationId` set by `foundation-core-starter`)
- `application` field derived from `spring.application.name` in every log entry
- Plain-text fallback for local development via `foundation.logging.json-format=false`

---

## Usage

Add the starter dependency:

```xml
<dependency>
    <groupId>fr.francetv.foundation</groupId>
    <artifactId>foundation-logging-starter</artifactId>
</dependency>
```

---

## Configuration properties

| Property | Type | Default | Description |
|---|---|---|---|
| `foundation.logging.json-format` | `boolean` | `true` | `true` for JSON output (production), `false` for plain-text (local dev) |

---

## Example output

### JSON (production)

```json
{
  "@timestamp": "2024-01-15T10:23:45.123Z",
  "@version": "1",
  "message": "Request received",
  "logger_name": "fr.example.service.OrderController",
  "level": "INFO",
  "application": "order-service",
  "correlationId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890"
}
```

### Plain text (local development)

```
10:23:45.123 INFO  [order-service] [a1b2c3d4-e5f6-7890-abcd-ef1234567890] fr.example.service.OrderController - Request received
```

---

## Switching to plain-text output

In `application.properties` (or `application-local.properties`):

```properties
foundation.logging.json-format=false
```

Or in `application.yml`:

```yaml
foundation:
  logging:
    json-format: false
```

---

## Correlation ID

The `correlationId` MDC field is automatically populated per request by `foundation-core-starter`'s `CorrelationIdFilter`, which reads the `X-Correlation-Id` HTTP header or generates a new UUID.

Add `foundation-core-starter` to your service to activate this behaviour:

```xml
<dependency>
    <groupId>fr.francetv.foundation</groupId>
    <artifactId>foundation-core-starter</artifactId>
</dependency>
```

---

## Overriding the Logback configuration

Place your own `logback-spring.xml` in `src/main/resources` of your service to fully override the default configuration. You can still reference the `foundation.logging.json-format` property via `<springProperty>`.

---

## Conditional activation

The auto-configuration activates only when Logback (`ch.qos.logback.classic.Logger`) is on the classpath. Services that exclude Logback in favour of Log4j2 will not be affected.

---

## Transitive dependencies

`logstash-logback-encoder` and `janino` are **intentionally non-optional** transitive dependencies of this starter. They are required for the JSON appender and for Logback `<if>` conditional processing respectively. Any consumer of this starter will receive them on its compile and runtime classpath.
