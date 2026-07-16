# foundation-soap-client-starter

Apache CXF JAX-WS client conventions for Spring Boot microservices.

Provides:
- Automatic `X-Correlation-Id` propagation on every outgoing SOAP request
- Configurable connect and receive timeouts via `foundation.soap-client.*` properties
- A `SoapClientFactory` that creates CXF proxy clients with all conventions pre-applied
- Optional Basic and Bearer token authentication helpers

---

## Usage

### 1. Declare the dependency

```xml
<dependency>
    <groupId>fr.francetv.foundation</groupId>
    <artifactId>foundation-soap-client-starter</artifactId>
</dependency>
```

The auto-configuration activates automatically when `cxf-rt-frontend-jaxws` is on the classpath.

---

### 2. Configure timeouts

```yaml
foundation:
  soap-client:
    connect-timeout: 5s    # default
    receive-timeout: 30s   # default
```

---

### 3. Create a SOAP client bean

Inject `SoapClientFactory` and create a named proxy in your configuration class:

```java
@Configuration
public class PolicyClientConfig {

    @Bean
    public PolicyService policyService(SoapClientFactory factory) {
        return factory.create(PolicyService.class,
                "https://partner.example.com/ws/PolicyService");
    }
}
```

`X-Correlation-Id` propagation and timeouts are applied automatically.

---

### 4. Basic auth

```java
@Bean
public PolicyService policyService(SoapClientFactory factory) {
    return factory.createWithBasicAuth(
            PolicyService.class,
            "https://partner.example.com/ws/PolicyService",
            "username",
            "secret");
}
```

> **Note**: `createWithBasicAuth` sets credentials via the JAX-WS request context
> (`BindingProvider.USERNAME_PROPERTY` / `PASSWORD_PROPERTY`). CXF forwards these
> through its `HTTPConduit` HTTP basic auth support. If the remote endpoint requires
> preemptive Basic auth or a custom `AuthorizationPolicy`, configure the `HTTPConduit`
> directly on the proxy after creation:
>
> ```java
> PolicyService proxy = factory.createWithBasicAuth(PolicyService.class, address, user, pass);
> HTTPConduit conduit = (HTTPConduit) ClientProxy.getClient(proxy).getConduit();
> AuthorizationPolicy auth = new AuthorizationPolicy();
> auth.setUserName(user);
> auth.setPassword(pass);
> auth.setAuthorizationType("Basic");
> conduit.setAuthorization(auth);
> ```

---

### 5. Bearer token auth

The token supplier is invoked on every request, supporting dynamic token refresh:

```java
@Bean
public PolicyService policyService(SoapClientFactory factory) {
    return factory.createWithBearerToken(
            PolicyService.class,
            "https://partner.example.com/ws/PolicyService",
            this::resolveToken);
}

private String resolveToken() {
    // Resolve token from SecurityContextHolder, OAuth2AuthorizedClientManager, etc.
    return SecurityContextHolder.getContext().getAuthentication()...;
}
```

---

## Configuration properties

| Property | Type | Default | Description |
|---|---|---|---|
| `foundation.soap-client.connect-timeout` | `Duration` | `5s` | TCP connection timeout for all SOAP calls |
| `foundation.soap-client.receive-timeout` | `Duration` | `30s` | Time to wait for a SOAP response |

---

## Correlation ID propagation

`CorrelationIdSoapInterceptor` is always active when the starter is on the classpath.

It reads `correlationId` from SLF4J MDC and adds it as `X-Correlation-Id` on every outgoing request.
If the MDC key is absent, a fresh UUID is generated.
If the header is already present on the request, it is **not** overwritten.

---

## Overriding beans

All beans are conditional. Declare your own to override:

```java
// Replace the correlation ID interceptor
@Bean
public CorrelationIdSoapInterceptor correlationIdSoapInterceptor() {
    return new MyCorrelationIdSoapInterceptor();
}

// Replace the entire factory (e.g. add a logging interceptor to every client)
@Bean
public SoapClientFactory soapClientFactory(SoapClientProperties properties,
                                            CorrelationIdSoapInterceptor correlationId,
                                            LoggingSoapInterceptor logging) {
    return new SoapClientFactory(properties, List.of(correlationId, logging));
}
```

---

## WSDL code generation

The starter provides runtime support. WSDL-generated client stubs belong to the consuming service.

Configure the CXF codegen plugin managed by `foundation-bom`:

```xml
<plugin>
    <groupId>org.apache.cxf</groupId>
    <artifactId>cxf-codegen-plugin</artifactId>
    <executions>
        <execution>
            <goals>
                <goal>wsdl2java</goal>
            </goals>
            <configuration>
                <wsdlOptions>
                    <wsdlOption>
                        <wsdl>${project.basedir}/src/main/resources/wsdl/policy.wsdl</wsdl>
                        <packagenames>
                            <packagename>com.example.client.policy</packagename>
                        </packagenames>
                    </wsdlOption>
                </wsdlOptions>
            </configuration>
        </execution>
    </executions>
</plugin>
```

Then use the generated `@WebService` interface with `SoapClientFactory.create()`.
