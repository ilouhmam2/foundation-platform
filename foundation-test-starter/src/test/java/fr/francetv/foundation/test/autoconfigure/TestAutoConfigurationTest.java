package fr.francetv.foundation.test.autoconfigure;

import fr.francetv.foundation.test.security.JwtTestUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;

class TestAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(TestAutoConfiguration.class));

    @Test
    void shouldRegisterJwtTestUtilsBeanByDefault() {
        contextRunner.run(ctx ->
                assertThat(ctx).hasSingleBean(JwtTestUtils.class));
    }

    @Test
    void shouldAllowJwtTestUtilsBeanOverride() {
        contextRunner
                .withUserConfiguration(CustomJwtConfig.class)
                .run(ctx -> {
                    assertThat(ctx).hasSingleBean(JwtTestUtils.class);
                    assertThat(ctx.getBean(JwtTestUtils.class))
                            .isSameAs(ctx.getBean("customJwtTestUtils"));
                });
    }

    @Test
    void shouldNotRegisterJwtTestUtilsWhenNimbusIsAbsent() {
        contextRunner
                .withClassLoader(new FilteredClassLoader(com.nimbusds.jose.jwk.RSAKey.class))
                .run(ctx ->
                        assertThat(ctx).doesNotHaveBean(JwtTestUtils.class));
    }

    @Configuration(proxyBeanMethods = false)
    static class CustomJwtConfig {

        @Bean
        JwtTestUtils customJwtTestUtils() {
            return new JwtTestUtils();
        }
    }
}
