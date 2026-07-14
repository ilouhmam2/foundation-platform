# Skill - OpenAPI Client Generation

## Purpose

Use this skill when a consuming microservice needs to generate a REST client from an OpenAPI or Swagger contract.

## Core rule

The generated client belongs to the consuming microservice, not to `foundation-platform`.

## Rules

- Use OpenAPI Generator Maven plugin (version managed by `foundation-bom`).
- Prefer `webclient` library for generated Java client.
- Do not manually write DTOs already generated from the contract.
- Add a MapStruct mapping layer between generated models and internal domain models.
- Do not expose generated models in domain or API layers.

## Recommended module structure

```text
quote-service
├── pom.xml
├── quote-service-app
│   └── src/main/java/...
└── quote-service-pricing-client
    ├── pom.xml
    └── src/main/resources/openapi/pricing-api.yaml
```

## Plugin configuration

```xml
<plugin>
    <groupId>org.openapitools</groupId>
    <artifactId>openapi-generator-maven-plugin</artifactId>
    <executions>
        <execution>
            <goals><goal>generate</goal></goals>
            <configuration>
                <inputSpec>${project.basedir}/src/main/resources/openapi/pricing-api.yaml</inputSpec>
                <generatorName>java</generatorName>
                <library>webclient</library>
                <apiPackage>fr.francetv.pricing.client.api</apiPackage>
                <modelPackage>fr.francetv.pricing.client.model</modelPackage>
                <generateApiTests>false</generateApiTests>
                <generateModelTests>false</generateModelTests>
                <configOptions>
                    <useJakartaEe>true</useJakartaEe>
                    <openApiNullable>false</openApiNullable>
                </configOptions>
            </configuration>
        </execution>
    </executions>
</plugin>
```

## Authentication

Configure authentication in the consuming service, not in `foundation-platform`.

The `foundation-http-client-starter` provides:
- Bearer token propagation filter
- Client credentials filter

## Timeout configuration

```yaml
foundation:
  http-client:
    pricing:
      base-url: https://pricing-service.example.com
      connect-timeout: 2s
      read-timeout: 5s
```

## Correlation ID

The `foundation-http-client-starter` automatically propagates `X-Correlation-Id` to all outgoing requests.

## Mapping pattern

```java
// Never expose generated models outside the client module
// Map at the boundary:
@Mapper(componentModel = "spring")
public interface PricingClientMapper {
    PricingQuote toDomain(PricingApiResponse response);
}
```