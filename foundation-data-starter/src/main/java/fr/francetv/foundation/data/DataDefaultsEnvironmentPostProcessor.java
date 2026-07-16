package fr.francetv.foundation.data;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.util.Map;

/**
 * Applies foundation data defaults at the lowest property priority so that
 * consuming services can override any value in their own configuration.
 *
 * <p>Defaults applied:
 * <ul>
 *   <li>{@code spring.jpa.hibernate.ddl-auto=validate}</li>
 *   <li>{@code spring.jpa.open-in-view=false}</li>
 * </ul>
 */
@SuppressWarnings("removal")
public class DataDefaultsEnvironmentPostProcessor implements EnvironmentPostProcessor {

    private static final String PROPERTY_SOURCE_NAME = "foundation-data-defaults";

    private static final Map<String, Object> DEFAULTS = Map.of(
            "spring.jpa.hibernate.ddl-auto", "validate",
            "spring.jpa.open-in-view", "false"
    );

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        if (!environment.getPropertySources().contains(PROPERTY_SOURCE_NAME)) {
            environment.getPropertySources().addLast(
                    new MapPropertySource(PROPERTY_SOURCE_NAME, DEFAULTS)
            );
        }
    }
}
