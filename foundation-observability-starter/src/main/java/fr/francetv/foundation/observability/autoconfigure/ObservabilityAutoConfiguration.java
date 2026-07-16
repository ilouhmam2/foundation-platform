package fr.francetv.foundation.observability.autoconfigure;

import fr.francetv.foundation.observability.properties.ObservabilityProperties;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.boot.micrometer.metrics.autoconfigure.MeterRegistryCustomizer;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * Auto-configuration for foundation observability.
 *
 * <p>Activates when {@code micrometer-core} is on the classpath (provided by
 * {@code spring-boot-starter-actuator}).
 *
 * <p>Registers a {@link MeterRegistryCustomizer} that attaches {@code application}
 * and {@code environment} common tags to every metric. Consumers can override the
 * customizer by declaring their own {@code MeterRegistryCustomizer} bean.
 *
 * <p>When {@code io.opentelemetry.api.OpenTelemetry} is detected on the classpath,
 * Spring Boot's own OpenTelemetry auto-configuration is active. Configure it via
 * {@code management.opentelemetry.resource-attributes} in {@code application.yml}.
 */
@AutoConfiguration
@ConditionalOnClass(MeterRegistry.class)
@EnableConfigurationProperties(ObservabilityProperties.class)
public class ObservabilityAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(MeterRegistryCustomizer.class)
    public MeterRegistryCustomizer<MeterRegistry> commonTagsMeterRegistryCustomizer(ObservabilityProperties props) {
        return registry -> registry.config()
                .commonTags(
                        "application", props.applicationName(),
                        "environment", props.environment()
                );
    }
}
