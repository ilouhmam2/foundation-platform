package fr.francetv.foundation.test.autoconfigure;

import com.nimbusds.jose.jwk.RSAKey;
import fr.francetv.foundation.test.security.JwtTestUtils;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;

/**
 * Auto-configuration for {@code foundation-test-starter}.
 *
 * <p>Activates only when both conditions are met:
 * <ul>
 *   <li>{@code nimbus-jose-jwt} is on the classpath ({@link RSAKey})</li>
 *   <li>{@code spring-boot-test} is on the classpath ({@link ApplicationContextRunner}) —
 *       this class is absent from production runtimes, providing a safety guard against
 *       accidental use of this starter outside of test scope.</li>
 * </ul>
 *
 * <p>Consuming services can override the bean by declaring their own {@link JwtTestUtils}:
 *
 * <pre>{@code
 * @TestConfiguration
 * public class CustomJwtConfig {
 *
 *     @Bean
 *     JwtTestUtils customJwtTestUtils() {
 *         return new JwtTestUtils();
 *     }
 * }
 * }</pre>
 */
@AutoConfiguration
@ConditionalOnClass({RSAKey.class, ApplicationContextRunner.class})
public class TestAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public JwtTestUtils jwtTestUtils() {
        return new JwtTestUtils();
    }
}
