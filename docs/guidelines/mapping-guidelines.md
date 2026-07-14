# Mapping Guidelines - foundation-platform

## 1. Purpose

This document defines mapping standards for services using `foundation-platform`.

The goal is to reduce boilerplate and avoid inconsistent manual mapping.

## 2. Default mapper

Use MapStruct as the default mapping solution.

The foundation provides:

- MapStruct dependency management
- annotation processor configuration
- common conventions
- optional shared mapper config

Business-specific mappers belong to consuming services.

## 3. Mapping layers

Typical mappings:

```text
API request DTO -> Command
Domain -> API response DTO

Entity -> Domain
Domain -> Entity

Generated REST client model -> Domain
Domain -> Generated REST client request

Generated SOAP model -> Domain
Domain -> Generated SOAP request

Event payload -> Domain
Domain -> Event payload
```