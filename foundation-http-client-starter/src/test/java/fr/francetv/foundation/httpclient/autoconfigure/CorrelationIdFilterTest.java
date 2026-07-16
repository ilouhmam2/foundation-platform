package fr.francetv.foundation.httpclient.autoconfigure;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.netty.DisposableServer;
import reactor.netty.http.server.HttpServer;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Integration tests for {@link fr.francetv.foundation.httpclient.filter.CorrelationIdExchangeFilter}
 * and HTTP client behaviour (timeouts, 4xx handling).
 *
 * <p>Uses an embedded Reactor Netty {@link HttpServer} to avoid external Jetty/WireMock
 * version conflicts while still testing real HTTP wire-level behaviour.
 */
class CorrelationIdFilterTest {

    private DisposableServer server;

    private final ApplicationContextRunner contextRunner =
            new ApplicationContextRunner()
                    .withConfiguration(AutoConfigurations.of(HttpClientAutoConfiguration.class));

    @AfterEach
    void tearDown() {
        if (server != null) {
            server.disposeNow(Duration.ofSeconds(5));
        }
    }

    @Test
    void shouldPropagateCorrelationIdHeader() {
        AtomicReference<String> received = new AtomicReference<>();

        server = HttpServer.create().port(0)
                .handle((req, res) -> {
                    received.set(req.requestHeaders().get("X-Correlation-Id"));
                    return res.status(200).send();
                })
                .bindNow();

        String baseUrl = "http://localhost:" + server.port();
        MDC.put("correlationId", "test-correlation-id-abc");
        try {
            contextRunner.run(ctx -> ctx.getBean(WebClient.Builder.class)
                    .baseUrl(baseUrl).build()
                    .get().uri("/test").retrieve().toBodilessEntity().block());
        } finally {
            MDC.remove("correlationId");
        }

        assertThat(received.get()).isEqualTo("test-correlation-id-abc");
    }

    @Test
    void shouldGenerateCorrelationIdWhenMdcEmpty() {
        AtomicReference<String> received = new AtomicReference<>();

        server = HttpServer.create().port(0)
                .handle((req, res) -> {
                    received.set(req.requestHeaders().get("X-Correlation-Id"));
                    return res.status(200).send();
                })
                .bindNow();

        String baseUrl = "http://localhost:" + server.port();
        MDC.remove("correlationId");

        contextRunner.run(ctx -> ctx.getBean(WebClient.Builder.class)
                .baseUrl(baseUrl).build()
                .get().uri("/generate").retrieve().toBodilessEntity().block());

        assertThat(received.get())
                .isNotBlank()
                .matches("[0-9a-f\\-]{36}");
    }

    @Test
    void shouldTimeoutAfterConfiguredDuration() {
        server = HttpServer.create().port(0)
                .handle((req, res) ->
                        Mono.delay(Duration.ofSeconds(3)).then(res.status(200).send()))
                .bindNow();

        String baseUrl = "http://localhost:" + server.port();

        contextRunner
                .withPropertyValues(
                        "foundation.http-client.connect-timeout=200ms",
                        "foundation.http-client.read-timeout=200ms"
                )
                .run(ctx -> {
                    WebClient client = ctx.getBean(WebClient.Builder.class)
                            .baseUrl(baseUrl).build();
                    assertThatThrownBy(() ->
                            client.get().uri("/slow").retrieve().toBodilessEntity().block()
                    ).isInstanceOf(WebClientRequestException.class);
                });
    }

    @Test
    void shouldReturn4xxWithoutRetry() {
        AtomicInteger requestCount = new AtomicInteger(0);

        server = HttpServer.create().port(0)
                .handle((req, res) -> {
                    requestCount.incrementAndGet();
                    return res.status(404).send();
                })
                .bindNow();

        String baseUrl = "http://localhost:" + server.port();

        contextRunner.run(ctx -> {
            WebClient client = ctx.getBean(WebClient.Builder.class)
                    .baseUrl(baseUrl).build();
            assertThatThrownBy(() ->
                    client.get().uri("/missing").retrieve()
                            .bodyToMono(String.class).block()
            ).isInstanceOf(WebClientResponseException.class)
             .extracting("statusCode.value")
             .isEqualTo(404);
        });

        // Exactly one attempt — WebClient does not retry by default
        assertThat(requestCount.get()).isEqualTo(1);
    }

    @Test
    void shouldNotOverwriteExistingCorrelationIdHeader() {
        AtomicReference<String> received = new AtomicReference<>();

        server = HttpServer.create().port(0)
                .handle((req, res) -> {
                    received.set(req.requestHeaders().get("X-Correlation-Id"));
                    return res.status(200).send();
                })
                .bindNow();

        String baseUrl = "http://localhost:" + server.port();
        MDC.put("correlationId", "mdc-value");
        try {
            contextRunner.run(ctx -> ctx.getBean(WebClient.Builder.class)
                    .baseUrl(baseUrl)
                    .defaultHeader("X-Correlation-Id", "caller-supplied-value")
                    .build()
                    .get().uri("/existing").retrieve().toBodilessEntity().block());
        } finally {
            MDC.remove("correlationId");
        }

        assertThat(received.get()).isEqualTo("caller-supplied-value");
    }
}

