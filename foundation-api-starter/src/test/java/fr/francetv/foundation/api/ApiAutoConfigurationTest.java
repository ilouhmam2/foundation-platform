package fr.francetv.foundation.api;

import fr.francetv.foundation.api.autoconfigure.ApiAutoConfiguration;
import fr.francetv.foundation.api.error.GlobalExceptionHandler;
import fr.francetv.foundation.api.properties.ApiProperties;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Auto-configuration tests for {@link ApiAutoConfiguration}.
 */
class ApiAutoConfigurationTest {

    private final WebApplicationContextRunner contextRunner =
            new WebApplicationContextRunner()
                    .withConfiguration(AutoConfigurations.of(ApiAutoConfiguration.class));

    @Test
    void shouldRegisterGlobalExceptionHandlerByDefault() {
        contextRunner.run(ctx ->
                assertThat(ctx).hasSingleBean(GlobalExceptionHandler.class));
    }

    @Test
    void shouldRegisterApiPropertiesByDefault() {
        contextRunner.run(ctx ->
                assertThat(ctx).hasSingleBean(ApiProperties.class));
    }

    @Test
    void shouldDefaultIncludeExceptionMessageToFalse() {
        contextRunner.run(ctx -> {
            ApiProperties props = ctx.getBean(ApiProperties.class);
            assertThat(props.includeExceptionMessage()).isFalse();
        });
    }

    @Test
    void shouldBindIncludeExceptionMessageProperty() {
        contextRunner
                .withPropertyValues("foundation.api.include-exception-message=true")
                .run(ctx -> {
                    ApiProperties props = ctx.getBean(ApiProperties.class);
                    assertThat(props.includeExceptionMessage()).isTrue();
                });
    }

    @Test
    void shouldAllowGlobalExceptionHandlerOverride() {
        contextRunner
                .withUserConfiguration(CustomHandlerConfig.class)
                .run(ctx -> {
                    assertThat(ctx).hasSingleBean(GlobalExceptionHandler.class);
                    assertThat(ctx.getBean(GlobalExceptionHandler.class))
                            .isInstanceOf(CustomExceptionHandler.class);
                });
    }

    @Test
    void shouldNotRegisterInNonWebContext() {
        new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(ApiAutoConfiguration.class))
                .run(ctx ->
                        assertThat(ctx).doesNotHaveBean(GlobalExceptionHandler.class));
    }

    @org.springframework.context.annotation.Configuration(proxyBeanMethods = false)
    static class CustomHandlerConfig {

        @org.springframework.context.annotation.Bean
        CustomExceptionHandler globalExceptionHandler(ApiProperties properties) {
            return new CustomExceptionHandler(properties);
        }
    }

    @RestControllerAdvice
    static class CustomExceptionHandler extends GlobalExceptionHandler {

        CustomExceptionHandler(ApiProperties properties) {
            super(properties);
        }
    }
}
