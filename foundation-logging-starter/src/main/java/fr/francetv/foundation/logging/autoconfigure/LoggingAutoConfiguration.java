package fr.francetv.foundation.logging.autoconfigure;

import ch.qos.logback.classic.Logger;
import fr.francetv.foundation.logging.properties.LoggingProperties;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * Auto-configuration for structured JSON logging.
 *
 * <p>Activates when Logback is on the classpath. Registers {@link LoggingProperties}
 * as a bean so that consuming services can read and react to {@code foundation.logging.*}
 * properties.
 *
 * <p>The actual Logback configuration is driven by {@code logback-spring.xml} bundled
 * in this starter. It reads {@code foundation.logging.json-format} via
 * {@code <springProperty>} and switches between JSON and plain-text output accordingly.
 *
 * <p>Consuming services can override {@code logback-spring.xml} by providing their own
 * file in {@code src/main/resources}.
 */
@AutoConfiguration
@ConditionalOnClass(Logger.class)
@EnableConfigurationProperties(LoggingProperties.class)
public class LoggingAutoConfiguration {
}
