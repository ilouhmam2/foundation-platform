package fr.francetv.foundation.httpclient.autoconfigure;

import fr.francetv.foundation.httpclient.filter.BearerTokenExchangeFilter;
import fr.francetv.foundation.httpclient.filter.CorrelationIdExchangeFilter;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.web.reactive.function.client.WebClient;

import static org.assertj.core.api.Assertions.assertThat;

class HttpClientAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner =
            new ApplicationContextRunner()
                    .withConfiguration(AutoConfigurations.of(HttpClientAutoConfiguration.class));

    @Test
    void shouldLoadWithoutError() {
        contextRunner.run(ctx -> assertThat(ctx).hasNotFailed());
    }

    @Test
    void shouldCreateCorrelationIdFilter() {
        contextRunner.run(ctx ->
                assertThat(ctx).hasSingleBean(CorrelationIdExchangeFilter.class));
    }

    @Test
    void shouldCreateWebClientBuilder() {
        contextRunner.run(ctx ->
                assertThat(ctx).hasSingleBean(WebClient.Builder.class));
    }

    @Test
    void shouldCreateBearerTokenFilterWhenSecurityPresent() {
        contextRunner.run(ctx ->
                assertThat(ctx).hasSingleBean(BearerTokenExchangeFilter.class));
    }

    @Test
    void shouldNotActivateWhenWebClientAbsent() {
        new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(HttpClientAutoConfiguration.class))
                .withClassLoader(new FilteredClassLoader(WebClient.class))
                .run(ctx -> {
                    assertThat(ctx).hasNotFailed();
                    assertThat(ctx).doesNotHaveBean(CorrelationIdExchangeFilter.class);
                    assertThat(ctx).doesNotHaveBean(WebClient.Builder.class);
                });
    }

    @Test
    void shouldRespectCustomCorrelationIdFilter() {
        contextRunner
                .withBean(CorrelationIdExchangeFilter.class, CorrelationIdExchangeFilter::new)
                .run(ctx -> assertThat(ctx).hasSingleBean(CorrelationIdExchangeFilter.class));
    }

    @Test
    void shouldRespectCustomWebClientBuilder() {
        contextRunner
                .withBean(WebClient.Builder.class, WebClient::builder)
                .run(ctx -> assertThat(ctx).hasSingleBean(WebClient.Builder.class));
    }

    @Test
    void shouldBindConnectTimeoutProperty() {
        contextRunner
                .withPropertyValues("foundation.http-client.connect-timeout=2s")
                .run(ctx -> assertThat(ctx).hasNotFailed());
    }
}
