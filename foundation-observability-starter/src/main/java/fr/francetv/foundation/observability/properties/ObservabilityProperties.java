package fr.francetv.foundation.observability.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Configuration properties for the foundation observability starter.
 *
 * <p>Prefix: {@code foundation.observability}
 *
 * <p>Set {@code foundation.observability.application-name} to ${spring.application.name}
 * in your {@code application.yml} so the tag reflects the actual service name.
 */
@ConfigurationProperties(prefix = "foundation.observability")
public record ObservabilityProperties(

        /**
         * Application name used as the {@code application} common tag on every metric.
         * Defaults to {@code application}. Override with ${spring.application.name}.
         */
        @DefaultValue("application") String applicationName,

        /**
         * Deployment environment used as the {@code environment} common tag on every metric.
         * Defaults to {@code default}. Override per environment (e.g. production, staging).
         */
        @DefaultValue("default") String environment

) {
}
