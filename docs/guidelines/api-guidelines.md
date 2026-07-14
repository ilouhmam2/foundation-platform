# API Guidelines - foundation-platform

## 1. Purpose

This document defines REST API standards for microservices using `foundation-platform`.

The goal is to ensure that all APIs are:

- consistent
- secure
- predictable
- observable
- easy to consume
- easy to document through OpenAPI

## 2. API ownership

Each microservice owns its API.

`foundation-platform` provides:

- API conventions
- standard error model
- validation behavior
- OpenAPI defaults
- exception handling support

It must not provide business-specific controllers or DTOs.

## 3. Base path convention

Recommended API path format:

```text
/api/v1/{resource}
```

## 4. HTTP methods

Use standard HTTP verbs:

```text
GET    - retrieve a resource or collection
POST   - create a resource
PUT    - replace a resource (full update)
PATCH  - partial update
DELETE - delete a resource
```

## 5. Standard error model

Use a consistent error response body:

```json
{
  "timestamp": "2026-07-14T10:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed for field 'email'",
  "path": "/api/v1/users",
  "correlationId": "abc-123"
}
```

The `correlationId` field must always be populated from `X-Correlation-Id`.

## 6. Validation

Use Bean Validation (`@Valid`, `@NotNull`, `@Size`) on request DTOs.

Return HTTP 400 for validation errors with field-level details.

## 7. HTTP status codes

```text
200 - OK
201 - Created (POST)
204 - No Content (DELETE)
400 - Bad Request (validation)
401 - Unauthorized (missing token)
403 - Forbidden (insufficient scope)
404 - Not Found
409 - Conflict
500 - Internal Server Error
```

## 8. OpenAPI documentation

Expose an OpenAPI spec at `/v3/api-docs`.

Use `springdoc-openapi` for auto-generation.

Annotate controllers with `@Tag` and operations with `@Operation`.

## 9. Correlation ID

Every request must propagate `X-Correlation-Id`.

The foundation provides correlation ID extraction and propagation via `foundation-logging-starter`.

Do not log sensitive data contained in request or response bodies.
