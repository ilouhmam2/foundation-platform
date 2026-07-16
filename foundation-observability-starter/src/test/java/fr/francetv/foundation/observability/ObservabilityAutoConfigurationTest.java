package fr.francetv.foundation.observability;

import fr.francetv.foundation.observability.autoconfigure.ObservabilityAutoConfiguration;
import fr.francetv.foundation.observability.properties.ObservabilityProperties;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.boot.micrometer.metrics.autoconfigure.MeterRegistryCustomizer;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;

class ObservabilityAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(ObservabilityAutoConfiguration.class));

    @Test
    void shouldRegisterMeterRegistryCustomizerByDefault() {
        contextRunner.run(ctx ->
                assertThat(ctx).hasSingleBean(MeterRegistryCustomizer.class));
    }

    @Test
    void shouldBackOffWhenCustomizerBeanIsPresent() {
        contextRunner
                .withUserConfiguration(CustomMeterRegistryCustomizerConfig.class)
                .run(ctx -> {
                    assertThat(ctx).hasSingleBean(MeterRegistryCustomizer.class);
                    assertThat(ctx.getBean(MeterRegistryCustomizer.class))
                            .isInstanceOf(CustomMeterRegistryCustomizerConfig.NoOpCustomizer.class);
                });
    }

    @Test
    void shouldBindApplicationNameProperty() {
        contextRunner
                .withPropertyValues("foundation.observability.application-name=my-service")
                .run(ctx -> {
                    ObservabilityProperties props = ctx.getBean(ObservabilityProperties.class);
                    assertThat(props.applicationName()).isEqualTo("my-service");
                });
    }

    @Test
    void shouldBindEnvironmentProperty() {
        contextRunner
                .withPropertyValues("foundation.observability.environment=production")
                .run(ctx -> {
                    ObservabilityProperties props = ctx.getBean(ObservabilityProperties.class);
                    assertThat(props.environment()).isEqualTo("production");
                });
    }

    @Test
    void shouldApplyDefaultPropertyValues() {
        contextRunner.run(ctx -> {
            ObservabilityProperties props = ctx.getBean(ObservabilityProperties.class);
            assertThat(props.applicationName()).isEqualTo("application");
            assertThat(props.environment()).isEqualTo("default");
        });
    }

    @Test
    void shouldApplyCommonTagsToRegistry() {
        contextRunner
                .withPropertyValues(
                        "foundation.observability.application-name=payment-service",
                        "foundation.observability.environment=staging"
                )
                .run(ctx -> {
                    @SuppressWarnings("unchecked")
                    MeterRegistryCustomizer<MeterRegistry> customizer =
                            ctx.getBean(MeterRegistryCustomizer.class);
                    SimpleMeterRegistry registry = new SimpleMeterRegistry();
                    customizer.customize(registry);
                    Counter counter = Counter.builder("test.counter").register(registry);
                    assertThat(counter.getId().getTags())
                            .contains(Tag.of("application", "payment-service"))
                            .contains(Tag.of("environment", "staging"));
                });
    }

    @Configuration(proxyBeanMethods = false)
    static class CustomMeterRegistryCustomizerConfig {

        @Bean
        NoOpCustomizer meterRegistryCustomizer() {
            return new NoOpCustomizer();
        }

        static class NoOpCustomizer implements MeterRegistryCustomizer<MeterRegistry> {
            @Override
            public void customize(MeterRegistry registry) {
                // no-op override
            }
        }
    }
}
