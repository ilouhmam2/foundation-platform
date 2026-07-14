---
description: "Review a foundation-platform module for design quality, security, tests, and compliance with project standards"
agent: "agent"
argument-hint: "Specify the module name to review, e.g. foundation-security-starter"
---
# Review a Foundation Module

Fill in before running:

```
MODULE: foundation-{name}-starter
```

---

## Mandatory reading

1. `AGENTS.md`
2. `ai/instructions/review-rules.md`
3. The module's `README.md`
4. The module's auto-configuration class

---

## Review checklist

Mark each item: **OK** / **Warning** / **Blocker**

### Architecture

- [ ] Single responsibility
- [ ] No deployment artifacts
- [ ] No IDP coupling
- [ ] Generated clients outside foundation
- [ ] Dependency-driven activation

### Implementation

- [ ] `@AutoConfiguration` + conditionals
- [ ] Constructor injection
- [ ] No business logic
- [ ] Minimal dependencies

### Security (if applicable)

- [ ] OAuth2 Resource Server only
- [ ] No tokens logged

### Tests

- [ ] Default beans tested
- [ ] Bean override tested
- [ ] Starter-absent scenario tested

### Documentation

- [ ] README with usage example
- [ ] Properties documented

---

## Output

```md
## Module Review: {MODULE}
### Accepted
### Issues
### Required changes
### Decision: Accepted / Needs changes / Rejected
```
