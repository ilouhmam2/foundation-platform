# Skill - Maven Multi-Module Design

## Purpose

Use this skill when creating or reviewing Maven multi-module structure for `foundation-platform`.

The goal is to create a clean, maintainable Maven architecture that separates:

- build standards
- dependency version management
- reusable runtime capabilities
- optional capabilities
- sample services

## When to use

Use this skill for tasks involving:

- root `pom.xml`
- `foundation-parent`
- `foundation-bom`
- new foundation modules
- module dependency review
- parent/BOM refactoring
- starter dependency structure
- consuming service POM examples

## Core principle

Use this separation:

```text
foundation-parent = build standards (compiler, plugins, quality)
foundation-bom    = dependency versions
foundation-*-starter = runtime capabilities
consuming service pom.xml = selected capabilities
```

## Anti-patterns to avoid

- Do not add runtime dependencies (JPA, Flyway, NATS) to `foundation-parent`.
- Do not manage plugin versions in `foundation-bom` (belongs in parent).
- Do not create a single mega-module that bundles all starters.
- Do not use `<dependencyManagement>` in starter modules unless necessary.
- Do not inherit from `foundation-parent` if the module is a consuming service.

## Dependency matrix

```text
foundation-parent
  └── (inherited by all foundation modules)

foundation-bom
  └── imports spring-boot BOM
  └── declares all third-party versions
  └── declares all foundation module versions

foundation-*-starter
  └── parent = foundation-parent
  └── imports = foundation-bom
  └── deps = only what the starter needs

consuming service
  └── parent = foundation-parent (optional)
  └── imports = foundation-bom
  └── deps = only the starters it uses
```

## Checklist

- [ ] Parent POM centralizes plugin and build standards.
- [ ] BOM centralizes dependency versions.
- [ ] Modules remain focused and loosely coupled.
- [ ] No optional runtime deps in parent.
- [ ] Each starter has a single responsibility.
- [ ] Documentation matches the module layout.
- [ ] `AutoConfiguration.imports` file is present in each starter.
