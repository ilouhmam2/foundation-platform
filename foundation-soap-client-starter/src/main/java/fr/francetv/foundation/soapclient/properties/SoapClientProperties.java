package fr.francetv.foundation.soapclient.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.time.Duration;

/**
 * Configuration properties for the SOAP client starter.
 *
 * <p>Prefix: {@code foundation.soap-client}
 *
 * <pre>{@code
 * foundation:
 *   soap-client:
 *     connect-timeout: 5s   # default
 *     receive-timeout: 30s  # default
 * }</pre>
 */
@ConfigurationProperties(prefix = "foundation.soap-client")
public record SoapClientProperties(

        /**
         * TCP connection timeout for SOAP calls. Defaults to {@code 5s}.
         */
        @DefaultValue("5s") Duration connectTimeout,

        /**
         * Receive timeout for SOAP calls (time to wait for a response). Defaults to {@code 30s}.
         */
        @DefaultValue("30s") Duration receiveTimeout

) {
}
