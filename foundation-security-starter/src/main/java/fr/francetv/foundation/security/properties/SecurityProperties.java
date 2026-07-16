package fr.francetv.foundation.security.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * Configuration properties for {@code foundation-security-starter}.
 *
 * <p>All paths in {@code publicPaths} are treated as Ant-style patterns and are
 * permitted without authentication. The default allows the Actuator health endpoint.
 *
 * <p><strong>Replace semantics:</strong> setting this property <em>replaces</em> the
 * default list entirely. If you configure custom paths and still need the Actuator
 * health endpoint to be public, include it explicitly:
 *
 * <pre>{@code
 * foundation:
 *   security:
 *     public-paths:
 *       - /actuator/health
 *       - /actuator/health/**
 *       - /open/**
 * }</pre>
 */
@ConfigurationProperties(prefix = "foundation.security")
public record SecurityProperties(List<String> publicPaths) {

    private static final List<String> DEFAULT_PUBLIC_PATHS =
            List.of("/actuator/health", "/actuator/health/**");

    public SecurityProperties {
        if (publicPaths == null || publicPaths.isEmpty()) {
            publicPaths = DEFAULT_PUBLIC_PATHS;
        }
    }
}
