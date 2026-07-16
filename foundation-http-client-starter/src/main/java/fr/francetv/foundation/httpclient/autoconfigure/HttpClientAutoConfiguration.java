package fr.francetv.foundation.httpclient.autoconfigure;

import fr.francetv.foundation.httpclient.filter.BearerTokenExchangeFilter;
import fr.francetv.foundation.httpclient.filter.CorrelationIdExchangeFilter;
import fr.francetv.foundation.httpclient.properties.HttpClientProperties;
import io.netty.channel.ChannelOption;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.AbstractOAuth2Token;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

/**
 * Auto-configuration for outgoing HTTP client conventions.
 *
 * <p>Activates when {@link WebClient} is on the classpath (i.e. when
 * {@code spring-boot-starter-webflux} or {@code spring-webflux} is present).
 *
 * <p>Provides:
 * <ul>
 *   <li>A prototype-scoped {@link WebClient.Builder} pre-configured with:
 *       correlation ID propagation, optional Bearer token injection,
 *       and timeouts from {@code foundation.http-client.*} properties</li>
 *   <li>A {@link CorrelationIdExchangeFilter} bean</li>
 *   <li>A {@link BearerTokenExchangeFilter} bean (when Spring Security + OAuth2 Core are present)</li>
 * </ul>
 */
@AutoConfiguration
@ConditionalOnClass(WebClient.class)
@EnableConfigurationProperties(HttpClientProperties.class)
public class HttpClientAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public CorrelationIdExchangeFilter correlationIdExchangeFilter() {
        return new CorrelationIdExchangeFilter();
    }

    /**
     * Inner configuration activated only when Spring Security Core and OAuth2 Core are present.
     * Using an inner class avoids classloading issues with {@code @ConditionalOnClass}
     * on a {@code @Bean} method whose return type references the conditional classes.
     */
    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass({SecurityContextHolder.class, AbstractOAuth2Token.class})
    static class SecurityFilterConfiguration {

        @Bean
        @ConditionalOnMissingBean
        public BearerTokenExchangeFilter bearerTokenExchangeFilter() {
            return new BearerTokenExchangeFilter();
        }
    }

    /**
     * Prototype-scoped {@link WebClient.Builder} pre-configured with:
     * <ul>
     *   <li>A {@link ReactorClientHttpConnector} with connect and response timeouts
     *       from {@code foundation.http-client.connect-timeout} / {@code read-timeout}</li>
     *   <li>{@link CorrelationIdExchangeFilter} — always applied</li>
     *   <li>{@link BearerTokenExchangeFilter} — applied when Spring Security is present</li>
     * </ul>
     *
     * <p>Each injection point receives a fresh builder. Consuming services call
     * {@code builder.baseUrl(...).build()} to create a named {@link WebClient}.
     */
    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    @ConditionalOnMissingBean(WebClient.Builder.class)
    public WebClient.Builder foundationWebClientBuilder(
            HttpClientProperties properties,
            ObjectProvider<CorrelationIdExchangeFilter> correlationIdFilter,
            ObjectProvider<BearerTokenExchangeFilter> bearerTokenFilter) {

        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS,
                        (int) properties.connectTimeout().toMillis())
                .responseTimeout(properties.readTimeout());

        WebClient.Builder builder = WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient));

        correlationIdFilter.ifAvailable(builder::filter);
        bearerTokenFilter.ifAvailable(builder::filter);

        return builder;
    }
}
