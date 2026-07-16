package fr.francetv.foundation.httpclient.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.time.Duration;
import java.util.Map;

/**
 * Configuration properties for the HTTP client starter.
 *
 * <p>Prefix: {@code foundation.http-client}
 *
 * <p>Global defaults apply to all clients unless overridden per client:
 * <pre>{@code
 * foundation.http-client.connect-timeout=5s
 * foundation.http-client.read-timeout=30s
 * }</pre>
 *
 * <p>Per-client overrides:
 * <pre>{@code
 * foundation.http-client.clients.pricing.base-url=https://pricing-service.example.com
 * foundation.http-client.clients.pricing.connect-timeout=2s
 * foundation.http-client.clients.pricing.read-timeout=5s
 * }</pre>
 */
@ConfigurationProperties(prefix = "foundation.http-client")
public record HttpClientProperties(

        /**
         * Global TCP connection timeout. Defaults to {@code 5s}.
         * Applied to the auto-configured {@code WebClient.Builder}.
         */
        @DefaultValue("5s") Duration connectTimeout,

        /**
         * Global response read timeout. Defaults to {@code 30s}.
         * Applied to the auto-configured {@code WebClient.Builder}.
         */
        @DefaultValue("30s") Duration readTimeout,

        /**
         * Per-named-client configuration. Keys are client names (e.g. {@code pricing}).
         * Each entry can override {@code baseUrl}, {@code connectTimeout}, and {@code readTimeout}.
         */
        Map<String, ClientConfig> clients

) {

    public HttpClientProperties {
        clients = (clients != null) ? Map.copyOf(clients) : Map.of();
    }

    /**
     * Configuration for a single named HTTP client.
     */
    public record ClientConfig(

            /** Base URL for this client (optional). */
            String baseUrl,

            /**
             * Connect timeout for this client.
             * Falls back to the global {@code foundation.http-client.connect-timeout} when null.
             */
            Duration connectTimeout,

            /**
             * Read timeout for this client.
             * Falls back to the global {@code foundation.http-client.read-timeout} when null.
             */
            Duration readTimeout

    ) {

        /** Returns the effective connect timeout, falling back to the global default. */
        public Duration effectiveConnectTimeout(Duration globalDefault) {
            return connectTimeout != null ? connectTimeout : globalDefault;
        }

        /** Returns the effective read timeout, falling back to the global default. */
        public Duration effectiveReadTimeout(Duration globalDefault) {
            return readTimeout != null ? readTimeout : globalDefault;
        }
    }
}
