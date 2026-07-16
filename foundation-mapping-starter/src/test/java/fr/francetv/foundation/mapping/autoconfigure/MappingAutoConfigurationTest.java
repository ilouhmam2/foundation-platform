package fr.francetv.foundation.mapping.autoconfigure;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.mapstruct.Mapper;

import static org.assertj.core.api.Assertions.assertThat;

class MappingAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner =
            new ApplicationContextRunner()
                    .withConfiguration(AutoConfigurations.of(MappingAutoConfiguration.class));

    @Test
    void shouldLoadWithoutError() {
        contextRunner.run(ctx -> assertThat(ctx).hasNotFailed());
    }

    @Test
    void shouldNotActivateWhenMapStructIsAbsent() {
        new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(MappingAutoConfiguration.class))
                .withClassLoader(new FilteredClassLoader(Mapper.class))
                .run(ctx -> assertThat(ctx).hasNotFailed());
    }
}
