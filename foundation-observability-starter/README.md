# foundation-observability-starter

Provides observability defaults for Spring Boot microservices:

- Actuator endpoints exposed: `health`, `metrics`, `prometheus`, `info`
- Liveness and readiness probes enabled
- Common Micrometer tags: `application`, `environment`
- OpenTelemetry support when `micrometer-tracing-bridge-otel` is on the classpath

---

## Usage

Add the starter dependency:

```xml
<dependency>
    <groupId>fr.francetv.foundation</groupId>
    <artifactId>foundation-observability-starter</artifactId>
</dependency>
```

Configure your `application.yml`:

```yaml
spring:
  application:
    name: my-service

foundation:
  observability:
    application-name: ${spring.application.name}
    environment: production

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
  info:
    env:
      enabled: true

info:
  app:
    name: ${spring.application.name}
    version: @project.version@
```

---

## Properties

| Property | Default | Description |
|---|---|---|
| `foundation.observability.application-name` | `application` | Value of the `application` common tag on all metrics. Set to `${spring.application.name}`. |
| `foundation.observability.environment` | `default` | Value of the `environment` common tag on all metrics. Override per environment. |

---

## Common Micrometer tags

The starter registers a `MeterRegistryCustomizer` that attaches `application` and
`environment` tags to every metric:

```
application="my-service", environment="production"
```

To override the customizer, declare your own `MeterRegistryCustomizer` bean:

```java
@Bean
public MeterRegistryCustomizer<MeterRegistry> customTags() {
    return registry -> registry.config()
            .commonTags("application", "my-service", "team", "payments");
}
```

---

## Actuator endpoints

| Endpoint | Path |
|---|---|
| Health | `/actuator/health` |
| Liveness probe | `/actuator/health/liveness` |
| Readiness probe | `/actuator/health/readiness` |
| Prometheus metrics | `/actuator/prometheus` |
| Metrics | `/actuator/metrics` |
| Info | `/actuator/info` |

Endpoint exposure is controlled by `management.endpoints.web.exposure.include`.
Liveness and readiness probes require `management.endpoint.health.probes.enabled=true`.

---

## OpenTelemetry tracing

When `micrometer-tracing-bridge-otel` is on the classpath, Spring Boot activates
OpenTelemetry auto-configuration automatically. Configure resource attributes via:

```yaml
management:
  opentelemetry:
    resource-attributes:
      service.name: ${spring.application.name}
      deployment.environment: production
```

Do not log trace IDs manually — use the `foundation-logging-starter` which
propagates them via MDC.
