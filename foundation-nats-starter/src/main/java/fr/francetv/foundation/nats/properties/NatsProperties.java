package fr.francetv.foundation.nats.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Configuration properties for the NATS starter.
 *
 * <p>Prefix: {@code foundation.nats}
 */
@ConfigurationProperties(prefix = "foundation.nats")
public record NatsProperties(

        /**
         * NATS server URL.
         * Defaults to {@code nats://localhost:4222}.
         */
        @DefaultValue("nats://localhost:4222") String serverUrl

) {
}
