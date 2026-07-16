# foundation-http-client-starter

Pre-configured `WebClient.Builder` for outgoing HTTP calls in Spring Boot microservices.

Provides:
- A prototype-scoped `WebClient.Builder` bean with all conventions pre-applied
- Automatic `X-Correlation-Id` propagation on every outgoing request
- Optional Bearer token injection from the Spring Security context
- Configurable connect and response timeouts (global defaults + per named client)

---

## Usage

### 1. Declare the dependency

```xml
<dependency>
    <groupId>fr.francetv.foundation</groupId>
    <artifactId>foundation-http-client-starter</artifactId>
</dependency>
```

The auto-configuration activates automatically when `WebClient` is on the classpath
(`spring-boot-starter-webflux` or `spring-webflux`).

---

### 2. Configure timeouts

Global defaults (applied to the auto-configured `WebClient.Builder`):

```yaml
foundation:
  http-client:
    connect-timeout: 5s    # default
    read-timeout: 30s      # default
```

Per named client overrides:

```yaml
foundation:
  http-client:
    clients:
      pricing:
        base-url: https://pricing-service.example.com
        connect-timeout: 2s
        read-timeout: 5s
      policy:
        base-url: https://policy-service.example.com
        read-timeout: 10s
```

> **Note**: the property path is `foundation.http-client.clients.{name}.*`
> (with the `clients` intermediate key), not `foundation.http-client.{name}.*`.

---

### 3. Create a WebClient for a named service

Each injection of `WebClient.Builder` receives a **fresh builder** (prototype scope)
already filtered with correlation ID and Bearer token filters:

```java
@Configuration
public class PricingClientConfig {

    @Bean
    public WebClient pricingWebClient(
            WebClient.Builder builder,
            HttpClientProperties properties) {

        HttpClientProperties.ClientConfig config =
                properties.clients().getOrDefault("pricing",
                        new HttpClientProperties.ClientConfig(null, null, null));

        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS,
                        (int) config.effectiveConnectTimeout(properties.connectTimeout()).toMillis())
                .responseTimeout(config.effectiveReadTimeout(properties.readTimeout()));

        return builder
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .baseUrl(config.baseUrl() != null ? config.baseUrl() : "https://pricing-service.example.com")
                .build();
    }
}
```

Then inject the named `WebClient` in your service:

```java
@Service
public class PricingService {

    private final WebClient pricingWebClient;

    public PricingService(WebClient pricingWebClient) {
        this.pricingWebClient = pricingWebClient;
    }

    public Mono<PriceDto> getPrice(String productId, String correlationId) {
        return pricingWebClient.get()
                .uri("/prices/{id}", productId)
                .retrieve()
                .bodyToMono(PriceDto.class);
        // X-Correlation-Id and Authorization are added automatically
    }
}
```

---

## Filters applied automatically

### `CorrelationIdExchangeFilter`

Always active when the starter is on the classpath.

Reads `correlationId` from SLF4J MDC and adds it as `X-Correlation-Id` on every outgoing request.
If the MDC key is absent (e.g. the call originates outside a request context), a fresh UUID is generated.
If the header is already present on the request, it is **not** overwritten.

### `BearerTokenExchangeFilter`

Active only when **both** of the following are on the classpath:
- `spring-security-core` (`SecurityContextHolder`, `ReactiveSecurityContextHolder`)
- `spring-security-oauth2-core` (`AbstractOAuth2Token`)

Reads the Bearer token from the Spring Security context and adds it as
`Authorization: Bearer <token>` on every outgoing request.

Resolution order:
1. Reactive context (`ReactiveSecurityContextHolder`) — for WebFlux services
2. Thread-local context (`SecurityContextHolder`) — for servlet services using `WebClient` for outbound calls

Supported authentication types: any `AbstractOAuth2Token` subtype
(e.g. `JwtAuthenticationToken`, `BearerTokenAuthentication`).

If no authenticated principal is found, or if the `Authorization` header is already set,
the request is forwarded unchanged.

> **Security note**: only OAuth2 token types are supported. String-typed credentials
> (e.g. passwords from `UsernamePasswordAuthenticationToken`) are intentionally ignored
> to prevent accidental credential leakage.

---

## Configuration properties

| Property | Type | Default | Description |
|---|---|---|---|
| `foundation.http-client.connect-timeout` | `Duration` | `5s` | Global TCP connect timeout applied to the auto-configured builder |
| `foundation.http-client.read-timeout` | `Duration` | `30s` | Global response timeout applied to the auto-configured builder |
| `foundation.http-client.clients.{name}.base-url` | `String` | — | Base URL for named client `{name}` |
| `foundation.http-client.clients.{name}.connect-timeout` | `Duration` | (global) | Connect timeout override for named client `{name}` |
| `foundation.http-client.clients.{name}.read-timeout` | `Duration` | (global) | Read timeout override for named client `{name}` |

---

## Overriding beans

All beans are conditional. Declare your own to override:

```java
// Replace the correlation ID filter (e.g. add a custom fallback header name)
@Bean
public CorrelationIdExchangeFilter correlationIdExchangeFilter() {
    return new MyCorrelationIdExchangeFilter();
}

// Replace the Bearer token filter (e.g. support a custom token type)
@Bean
public BearerTokenExchangeFilter bearerTokenExchangeFilter() {
    return new MyBearerTokenExchangeFilter();
}

// Replace the entire builder (e.g. configure mTLS or a custom codec)
@Bean
public WebClient.Builder webClientBuilder() {
    return WebClient.builder()
            .filter(new CorrelationIdExchangeFilter())
            .codecs(c -> c.defaultCodecs().maxInMemorySize(2 * 1024 * 1024));
}
```

---

## MDC propagation limitation in reactive contexts

`CorrelationIdExchangeFilter` reads from SLF4J MDC, which uses `ThreadLocal` storage.

In **servlet** services using `WebClient` for outbound calls, the MDC set by
`foundation-core-starter`'s `CorrelationIdFilter` is available on the calling thread
and is correctly propagated.

In **fully reactive WebFlux** services, a `WebClient` call inside a reactive chain may
execute on a different Netty event loop thread where the MDC context is not automatically
carried over. If your service is reactive, consider populating the MDC explicitly before
initiating outgoing calls, or use [Reactor Context](https://projectreactor.io/docs/core/release/reference/#context)
together with a custom filter that reads from the reactive context.

---

## Conditional activation

The auto-configuration activates only when `WebClient` is on the classpath.
Services that do not include `spring-webflux` are not affected.

```
@ConditionalOnClass(WebClient.class)         ← activates the starter
@ConditionalOnMissingBean(WebClient.Builder) ← backs off if you declare your own builder
```
