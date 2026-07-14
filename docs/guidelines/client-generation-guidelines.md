# Client Generation Guidelines

## 1. Purpose

This document defines how REST and SOAP clients should be generated and used by microservices consuming `foundation-platform`.

The objective is to standardize external service integration while keeping generated clients outside the foundation repository.

---

## 2. Key principle

Generated clients belong to consuming microservices.

`foundation-platform` provides:

- plugin version management
- templates
- guidelines
- runtime support
- authentication support
- timeout support
- correlation ID propagation
- error mapping conventions

It must not contain service-specific generated clients.

---

## 3. Target use cases

A microservice may need to call:

- REST APIs described by OpenAPI or Swagger
- SOAP services described by WSDL
- internal services
- external partner APIs
- legacy systems
- cloud/SaaS APIs

---

## 4. Recommended service structure

For a service with generated clients, prefer a multi-module structure when complexity justifies it.

Example:

```text
quote-service
├── pom.xml
├── quote-service-pricing-client
│   ├── pom.xml
│   └── src/main/resources/openapi/pricing-api.yaml
└── quote-service-policy-soap-client
    ├── pom.xml
    └── src/main/resources/wsdl/policy-service.wsdl
```

---

## 5. OpenAPI / REST client generation

Use the OpenAPI Generator Maven plugin managed by `foundation-bom`.

Recommended generator: `java` with `webclient` library.

Key configuration:

```xml
<configuration>
    <generatorName>java</generatorName>
    <library>webclient</library>
    <apiPackage>com.example.client.api</apiPackage>
    <modelPackage>com.example.client.model</modelPackage>
    <generateApiTests>false</generateApiTests>
    <generateModelTests>false</generateModelTests>
</configuration>
```

Do not manually write DTOs that are already generated from the contract.

---

## 6. SOAP / WSDL client generation

Use Apache CXF `cxf-codegen-plugin` managed by `foundation-bom`.

Key configuration:

```xml
<configuration>
    <wsdlOptions>
        <wsdlOption>
            <wsdl>${project.basedir}/src/main/resources/wsdl/service.wsdl</wsdl>
            <packagenames>
                <packagename>com.example.client.soap</packagename>
            </packagenames>
        </wsdlOption>
    </wsdlOptions>
</configuration>
```

---

## 7. Mapping layer

Never expose generated models directly in the domain or API layers.

Add an explicit mapping layer using MapStruct:

```text
Generated model  ->  Domain model  ->  API response DTO
```

---

## 8. Authentication

Configuring auth per client is the responsibility of the consuming service.

Common strategies supported by `foundation-http-client-starter`:
- Bearer token from security context
- Client credentials flow
- API key header

---

## 9. Timeout configuration

Always configure explicit timeouts on generated clients.

Never rely on default infinite timeouts.

Example for WebClient-based client:

```yaml
foundation:
  http-client:
    pricing:
      base-url: https://pricing-service.example.com
      connect-timeout: 2s
      read-timeout: 5s
```

---

## 10. Correlation ID

All outgoing HTTP and SOAP calls must propagate `X-Correlation-Id`.

The foundation provides a request interceptor for this purpose.