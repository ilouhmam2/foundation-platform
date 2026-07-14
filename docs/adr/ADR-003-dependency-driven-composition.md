# ADR-003 - Use dependency-driven composition

## Status

Accepted

## Context

The foundation must allow services to use only the capabilities they need.

Possible approaches:

1. Put all dependencies in the parent POM and enable or disable features.
2. Provide dedicated starters and let services declare the capabilities they need.

The first approach creates heavy classpaths and many configuration flags.

## Decision

Use dependency-driven composition.

A service gets a capability by declaring the related starter dependency.

Example:

```xml
<dependency>
    <groupId>fr.francetv.foundation</groupId>
    <artifactId>foundation-nats-starter</artifactId>
</dependency>
```

## Rationale

The first approach (all dependencies in parent, enabled/disabled via properties) creates unnecessary complexity:

- Increases classpath size for all services regardless of need
- Requires configuration flags for every optional capability
- Makes auto-configuration harder to reason about
- Couples unrelated capabilities in a single activation model

The second approach is simpler and more maintainable:

- Services declare only the capabilities they use
- Startup is faster due to reduced classpath
- Auto-configurations are activated naturally by classpath presence
- Each starter can evolve independently

## Consequences

- Services must declare starter dependencies explicitly in their pom.xml
- Properties configure capabilities but do not primarily activate them
- The parent POM must not include optional runtime dependencies
- Capability enablement is determined by classpath presence, not by flags
- `@ConditionalOnClass` and `@ConditionalOnMissingBean` are the primary activation mechanisms