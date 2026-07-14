# Skill - WSDL Client Generation

## Purpose

Use this skill when a consuming microservice needs to generate a SOAP client from a WSDL contract.

## Core rule

The generated client belongs to the consuming microservice, not to `foundation-platform`.

## Rules

- Use Apache CXF `cxf-codegen-plugin` (version managed by `foundation-bom`).
- Never commit generated code to the repository.
- Add a MapStruct mapping layer between generated SOAP models and internal domain models.
- Do not expose generated JAXB models in domain or API layers.

## Recommended module structure

```text
quote-service
├── pom.xml
├── quote-service-app
│   └── src/main/java/...
└── quote-service-policy-soap-client
    ├── pom.xml
    └── src/main/resources/wsdl/policy-service.wsdl
```

## Plugin configuration

```xml
<plugin>
    <groupId>org.apache.cxf</groupId>
    <artifactId>cxf-codegen-plugin</artifactId>
    <executions>
        <execution>
            <phase>generate-sources</phase>
            <goals><goal>wsdl2java</goal></goals>
            <configuration>
                <wsdlOptions>
                    <wsdlOption>
                        <wsdl>${project.basedir}/src/main/resources/wsdl/policy-service.wsdl</wsdl>
                        <extraargs>
                            <extraarg>-p</extraarg>
                            <extraarg>fr.francetv.policy.client.soap</extraarg>
                        </extraargs>
                    </wsdlOption>
                </wsdlOptions>
            </configuration>
        </execution>
    </executions>
</plugin>
```

## Dependency

```xml
<dependency>
    <groupId>fr.francetv.foundation</groupId>
    <artifactId>foundation-soap-client-starter</artifactId>
</dependency>
```

## Authentication

Configure authentication per client in the consuming service.

The `foundation-soap-client-starter` provides:
- WS-Security username/password handler
- Basic auth support
- Bearer token header injection

## Timeout configuration

```yaml
foundation:
  soap-client:
    policy:
      wsdl-location: classpath:wsdl/policy-service.wsdl
      connect-timeout: 2000
      receive-timeout: 10000
```

## Correlation ID

The `foundation-soap-client-starter` propagates `X-Correlation-Id` as a SOAP header or HTTP header on all outgoing calls.

## Mapping pattern

```java
// Map at the boundary of the client module
@Mapper(componentModel = "spring")
public interface PolicyClientMapper {
    PolicyDomain toDomain(PolicySoapResponse response);
}
```
