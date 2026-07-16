package fr.francetv.foundation.security;

import fr.francetv.foundation.security.autoconfigure.SecurityAutoConfiguration;
import fr.francetv.foundation.security.properties.SecurityProperties;
import jakarta.servlet.Filter;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.web.SecurityFilterChain;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * Auto-configuration tests for {@link SecurityAutoConfiguration}.
 *
 * <p>Uses {@link WebApplicationContextRunner} to verify context-level behavior:
 * bean registration, property binding, conditional activation, and bean override.
 */
class SecurityAutoConfigurationTest {

    private final WebApplicationContextRunner contextRunner =
            new WebApplicationContextRunner()
                    .withUserConfiguration(EnableWebSecurityConfig.class)
                    .withConfiguration(AutoConfigurations.of(SecurityAutoConfiguration.class))
                    .withBean(JwtDecoder.class, () -> mock(JwtDecoder.class));

    @Test
    void shouldRegisterSecurityFilterChainByDefault() {
        contextRunner.run(ctx ->
                assertThat(ctx).hasSingleBean(SecurityFilterChain.class));
    }

    @Test
    void shouldRegisterSecurityPropertiesByDefault() {
        contextRunner.run(ctx ->
                assertThat(ctx).hasSingleBean(SecurityProperties.class));
    }

    @Test
    void shouldDefaultPublicPathsToActuatorHealth() {
        contextRunner.run(ctx -> {
            SecurityProperties props = ctx.getBean(SecurityProperties.class);
            assertThat(props.publicPaths()).contains("/actuator/health/**");
        });
    }

    @Test
    void shouldBindPublicPathsProperty() {
        contextRunner
                .withPropertyValues("foundation.security.public-paths=/open/**,/public/**")
                .run(ctx -> {
                    SecurityProperties props = ctx.getBean(SecurityProperties.class);
                    assertThat(props.publicPaths()).containsExactly("/open/**", "/public/**");
                });
    }

    @Test
    void shouldAllowSecurityFilterChainOverride() {
        contextRunner
                .withUserConfiguration(CustomSecurityConfig.class)
                .run(ctx ->
                        assertThat(ctx).hasSingleBean(SecurityFilterChain.class));
    }

    @Test
    void shouldNotRegisterInNonWebContext() {
        new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(SecurityAutoConfiguration.class))
                .run(ctx ->
                        assertThat(ctx).doesNotHaveBean(SecurityFilterChain.class));
    }

    @Test
    void shouldNotActivateWhenSecurityIsAbsent() {
        new WebApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(SecurityAutoConfiguration.class))
                .withClassLoader(new FilteredClassLoader(SecurityFilterChain.class))
                .run(ctx ->
                        assertThat(ctx).doesNotHaveBean(SecurityFilterChain.class));
    }

    @Configuration(proxyBeanMethods = false)
    @EnableWebSecurity
    static class EnableWebSecurityConfig {
    }

    @Configuration(proxyBeanMethods = false)
    static class CustomSecurityConfig {

        @Bean
        SecurityFilterChain customFilterChain() {
            return new SecurityFilterChain() {
                @Override
                public boolean matches(HttpServletRequest request) {
                    return true;
                }

                @Override
                public List<Filter> getFilters() {
                    return List.of();
                }
            };
        }
    }
}
