package fr.francetv.foundation.logging.autoconfigure;

import fr.francetv.foundation.logging.properties.LoggingProperties;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class LoggingAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner =
            new ApplicationContextRunner()
                    .withConfiguration(AutoConfigurations.of(LoggingAutoConfiguration.class));

    @Test
    void shouldLoadWithoutError() {
        contextRunner.run(ctx -> assertThat(ctx).hasNotFailed());
    }

    @Test
    void shouldExposeLoggingPropertiesBean() {
        contextRunner.run(ctx -> assertThat(ctx).hasSingleBean(LoggingProperties.class));
    }

    @Test
    void shouldDefaultJsonFormatToTrue() {
        contextRunner.run(ctx ->
                assertThat(ctx.getBean(LoggingProperties.class).jsonFormat()).isTrue());
    }

    @Test
    void shouldBindLoggingProperties() {
        contextRunner
                .withPropertyValues("foundation.logging.json-format=false")
                .run(ctx ->
                        assertThat(ctx.getBean(LoggingProperties.class).jsonFormat())
                                .isFalse());
    }

    @Test
    void shouldNotActivateWhenLogbackIsAbsent() {
        // Simulate an environment where ch.qos.logback.classic.Logger is not on the classpath
        new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(LoggingAutoConfiguration.class))
                .withClassLoader(new org.springframework.boot.test.context.FilteredClassLoader(
                        ch.qos.logback.classic.Logger.class))
                .run(ctx -> assertThat(ctx).doesNotHaveBean(LoggingProperties.class));
    }
}
