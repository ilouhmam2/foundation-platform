# foundation-api-starter

Spring Boot starter providing REST API conventions for foundation microservices.

## What it provides

- `GlobalExceptionHandler` — `@RestControllerAdvice` that maps common exceptions to a structured error body
- `ApiErrorResponse` — immutable record (timestamp, status, error, message, path, correlationId)
- `ApiProperties` — typed configuration under `foundation.api.*`

Activates automatically when `spring-webmvc` is on the classpath.

---

## Standard error model

```json
{
  "timestamp": "2026-07-15T10:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "name: must not be blank",
  "path": "/api/v1/users",
  "correlationId": "abc-123"
}
```

The `correlationId` is read from MDC (populated by `foundation-core-starter`) with a fallback
to the `X-Correlation-Id` request header.

---

## Exception mapping

| Exception                          | HTTP Status |
|------------------------------------|-------------|
| `MethodArgumentNotValidException`  | 400         |
| `ConstraintViolationException`     | 400 \*      |
| `HttpMessageNotReadableException`  | 400         |
| `FoundationBusinessException`      | 400         |
| `ResponseStatusException`          | status code |
| `NoResourceFoundException`         | 404         |
| `FoundationTechnicalException`     | 500         |
| `Exception` (catch-all)            | 500         |

\* Handled by `ValidationExceptionHandler`, a separate `@RestControllerAdvice` registered only when
`jakarta.validation.ConstraintViolationException` is on the classpath (i.e. when
`spring-boot-starter-validation` is declared as a dependency).

---

## Configuration properties

| Property                                  | Type      | Default | Description                                      |
|-------------------------------------------|-----------|---------|--------------------------------------------------|
| `foundation.api.include-exception-message`| `boolean` | `false` | Expose exception message in 5xx error responses. |

> **Security note**: keep `include-exception-message=false` in production to avoid leaking
> internal error details. Enable only in non-production environments.

---

## Usage

Add the starter to your service:

```xml
<dependency>
    <groupId>fr.francetv.foundation</groupId>
    <artifactId>foundation-api-starter</artifactId>
</dependency>
```

No further configuration is required. The `GlobalExceptionHandler` is registered automatically.

### Override the exception handler

Declare your own `@RestControllerAdvice` bean:

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
auto-configuration will back off and use your bean.

### Application properties example

```yaml
foundation:
  api:
    include-exception-message: false   # default — safe for production
```

---

## Testing

`GlobalExceptionHandlerTest` uses `MockMvcBuilders.standaloneSetup` — a pure Spring Test approach
that sets up `GlobalExceptionHandler` and a minimal test controller without a full Spring context.

`ApiAutoConfigurationTest` uses `WebApplicationContextRunner` to verify conditional bean
registration and property binding.

When `foundation-api-starter` is on the classpath, `@WebMvcTest` slices in consuming
services automatically include `GlobalExceptionHandler` via
`META-INF/spring/org.springframework.boot.test.autoconfigure.web.mvc.AutoConfigureWebMvc.imports`.
