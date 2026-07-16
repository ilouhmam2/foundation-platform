package fr.francetv.foundation.data.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "foundation.data")
public class DataProperties {
    // Extension point for foundation data configuration.
    // Consumers may contribute additional properties as the platform evolves.
}
