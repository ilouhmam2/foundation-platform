# ADR-004 - Keep Deployment Outside foundation-platform

## Status

Accepted

---

## Context

The target runtime is based on Kubernetes, Helm and GitLab CI.

However, `foundation-platform` is intended to be a Spring Boot technical foundation, not a deployment platform.

---

## Decision

Do not include deployment artifacts in `foundation-platform`.

The repository must not include:

- Kubernetes manifests
- Helm charts
- Docker Compose

Dockerfile belongs to the consuming microservice.

---

## Rationale

Deployment standards evolve independently from application foundation standards.

Keeping deployment outside avoids coupling the foundation to one delivery model.

It also keeps the foundation focused on Java, Spring Boot and application-level concerns.

---

## Consequences

- The foundation must expose technical endpoints required by deployment platforms.
- Actuator health, readiness and liveness endpoints must be supported.
- Prometheus metrics endpoint must be supported.
- Documentation may explain deployment expectations, but no deployment artifact should be generated here.