package fr.francetv.foundation.data.autoconfigure;

import fr.francetv.foundation.data.properties.DataProperties;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class DataAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner =
            new ApplicationContextRunner()
                    .withConfiguration(AutoConfigurations.of(DataAutoConfiguration.class));

    @Test
    void shouldLoadContextWithoutError() {
        contextRunner
                .withBean("jpaAuditingHandler", Object.class, Object::new)
                .run(ctx -> assertThat(ctx).hasNotFailed());
    }

    @Test
    void shouldRegisterDataProperties() {
        contextRunner
                .withBean("jpaAuditingHandler", Object.class, Object::new)
                .run(ctx -> assertThat(ctx).hasSingleBean(DataProperties.class));
    }

    @Test
    void shouldNotActivateWhenDataSourceClassIsAbsent() {
        new ApplicationContextRunner()
                .withClassLoader(new org.springframework.boot.test.context.FilteredClassLoader(
                        javax.sql.DataSource.class))
                .withConfiguration(AutoConfigurations.of(DataAutoConfiguration.class))
                .run(ctx -> assertThat(ctx).doesNotHaveBean(DataAutoConfiguration.class));
    }

    @Test
    void shouldBackOffAuditConfigurationWhenJpaAuditingHandlerPresent() {
        contextRunner
                .withBean("jpaAuditingHandler", Object.class, Object::new)
                .run(ctx -> assertThat(ctx)
                        .doesNotHaveBean(fr.francetv.foundation.data.audit.AuditConfiguration.class));
    }
}
