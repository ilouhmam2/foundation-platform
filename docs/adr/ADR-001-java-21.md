# ADR-001 - Use Java 21 as Baseline

## Status

Accepted

---

## Context

`foundation-platform` is intended to be used by enterprise microservices.

The platform must provide a stable and widely supported Java baseline.

The team considered:

- Java 21
- Java 25

---

## Decision

Use Java 21 as the baseline version for `foundation-platform`.

---

## Rationale

Java 21 is a Long-Term Support (LTS) release and is mature in enterprise environments.

It provides modern Java capabilities such as virtual threads while maintaining strong compatibility with the broader Java ecosystem.

Java 25 may be evaluated later as a compatibility target, but it will not be the initial baseline.

---

## Consequences

- All modules must compile with Java 21.
- Maven compiler configuration must target Java 21.
- Docker images used by consuming services should use Java 21 runtime images unless the project decides otherwise.
- Java preview features are not enabled by default.
- Preview features may be evaluated in a future ADR if a specific use case justifies adoption.preview features must not be used without explicit ADR approval.
- Maven compiler plugin must target Java 21 (`source` and `target` set to `21`).
- Java 25 or later may be evaluated as a future migration target through a dedicated ADR.