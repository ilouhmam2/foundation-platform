# Review Rules - foundation-platform

## 1. Purpose

This document defines how AI agents and humans should review changes in `foundation-platform`.

## 2. Review mindset

Review as a platform architect.

The main risk is over-engineering.

Prefer simple, explicit Spring Boot-native solutions.

## 3. Architecture review

Check:

- Is the change aligned with PRD?
- Is it aligned with ADRs?
- Does it respect dependency-driven composition?
- Does it avoid deployment coupling?
- Does it avoid IDP coupling?
- Does it keep generated clients outside the foundation?

## 4. Module review

Check:

- Does the module have one responsibility?
- Are dependencies minimal?
- Does it introduce unrelated dependencies?
- Are defaults overridable?
- Are auto-configurations conditional?
- Is business logic absent?

## 5. Code review

Check:

- constructor injection
- no field injection
- clear naming
- no unnecessary abstraction
- no static global state unless justified
- no sensitive data logging
- exceptions are handled correctly

## 6. Test review

Check:

- tests exist
- tests cover defaults
- tests cover overrides
- tests cover error cases
- tests are deterministic
- tests do not rely on manually running services

## 7. Documentation review

Check:

- README or module docs updated
- properties documented
- usage examples present
- limitations documented
- ADR updated if decision changed

## 8. Rejection criteria

Reject a change if it:

- adds deployment files
- adds business-specific code
- adds optional runtime dependencies to parent
- uses Keycloak adapters
- exposes entities through APIs
- relies on `enabled=true` as primary feature activation
- creates generated clients in foundation repository
- hides Spring Boot behind a custom programming model

## 9. Review output format

Use this format:

```md
## Review Summary

### Accepted points

- ...

### Issues

- ...

### Required changes

- ...

### Optional improvements

- ...

### Final decision

Accepted / Needs changes / Rejected
```