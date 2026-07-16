package fr.francetv.foundation.logging.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Configuration properties for the foundation logging starter.
 *
 * <p>Prefix: {@code foundation.logging}
 */
@ConfigurationProperties(prefix = "foundation.logging")
public record LoggingProperties(

        /**
         * Whether to output logs in JSON format using logstash-logback-encoder.
         * Defaults to {@code true}. Set to {@code false} for plain-text output (e.g. local development).
         */
        @DefaultValue("true") boolean jsonFormat

) {
}
