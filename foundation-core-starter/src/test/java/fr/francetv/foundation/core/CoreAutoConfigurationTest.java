package fr.francetv.foundation.core;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class CoreAutoConfigurationTest {

    private final WebApplicationContextRunner contextRunner =
            new WebApplicationContextRunner()
                    .withConfiguration(AutoConfigurations.of(CoreAutoConfiguration.class));

    @Test
    void shouldRegisterCorrelationIdFilterByDefault() {
        contextRunner.run(ctx ->
                assertThat(ctx).hasSingleBean(CorrelationIdFilter.class));
    }

    @Test
    void shouldAllowBeanOverride() {
        contextRunner
                .withUserConfiguration(CustomFilterConfig.class)
                .run(ctx -> {
                    assertThat(ctx).hasSingleBean(CorrelationIdFilter.class);
                    assertThat(ctx.getBean(CorrelationIdFilter.class))
                            .isInstanceOf(CustomCorrelationIdFilter.class);
                });
    }

    @Test
    void shouldNotRegisterFilterInNonWebContext() {
        new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(CoreAutoConfiguration.class))
                .run(ctx -> assertThat(ctx).doesNotHaveBean(CorrelationIdFilter.class));
    }

    @org.springframework.context.annotation.Configuration(proxyBeanMethods = false)
    static class CustomFilterConfig {

        @org.springframework.context.annotation.Bean
        CustomCorrelationIdFilter correlationIdFilter() {
            return new CustomCorrelationIdFilter();
        }
    }

    static class CustomCorrelationIdFilter extends CorrelationIdFilter {
    }
}
