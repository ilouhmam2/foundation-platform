# Observability Guidelines - foundation-platform

## 1. Purpose

This document defines observability standards for services using `foundation-platform`.

Observability includes:

- health checks
- metrics
- traces
- technical endpoint exposure
- service metadata

Logging is handled separately by the logging foundation.

## 2. Required capabilities

Every standard service should expose:

```text
/actuator/health
/actuator/health/liveness
/actuator/health/readiness
/actuator/metrics
/actuator/prometheus
```

---

## 3. Actuator configuration

Minimum recommended configuration:

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,metrics,prometheus,info
  endpoint:
    health:
      probes:
        enabled: true
      show-details: when-authorized
```

---

## 4. Micrometer metrics

Use Micrometer for application metrics.

Common tags should include:
- `application` - service name
- `environment` - deployment environment

Custom metrics should use meaningful names and follow the `noun.verb` pattern:

```java
counter = Counter.builder("quote.created")
        .description("Number of quotes created")
        .register(meterRegistry);
```

---

## 5. OpenTelemetry tracing

Use OpenTelemetry for distributed tracing.

The foundation provides default trace configuration.

Services should propagate trace context via HTTP headers.

Do not log trace IDs manually — the logging foundation handles this via MDC.

---

## 6. Service metadata

Expose service information via Actuator info endpoint:

```yaml
management:
  info:
    env:
      enabled: true

info:
  app:
    name: ${spring.application.name}
    version: @project.version@