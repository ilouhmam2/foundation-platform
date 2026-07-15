package fr.francetv.foundation.api;

import org.springframework.boot.SpringBootConfiguration;

/**
 * Minimal Spring Boot configuration used as the root configuration for {@code @WebMvcTest} slices.
 *
 * <p>Does NOT enable component scanning or full auto-configuration — {@code @WebMvcTest}
 * manages those itself. Its sole purpose is to satisfy the {@code @SpringBootConfiguration}
 * requirement for test slices.
 */
@SpringBootConfiguration
public class TestApiApplication {
}
