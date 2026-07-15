package fr.francetv.foundation.api.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for the foundation API starter.
 *
 * <p>All properties are under the {@code foundation.api} prefix.
 *
 * <pre>
 * foundation.api.include-exception-message=false   # expose exception message in 5xx responses (default: false)
 * </pre>
 */
@ConfigurationProperties(prefix = "foundation.api")
public record ApiProperties(boolean includeExceptionMessage) {
}
