package fr.francetv.foundation.security.autoconfigure;

import fr.francetv.foundation.security.properties.SecurityProperties;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Auto-configuration for the foundation security starter.
 *
 * <p>Activates only when:
 * <ul>
 *   <li>{@code spring-security-web} is on the classpath ({@link SecurityFilterChain})</li>
 *   <li>The application is a SERVLET web application</li>
 * </ul>
 *
 * <p>Provides a default {@link SecurityFilterChain} that:
 * <ul>
 *   <li>Permits paths listed in {@code foundation.security.public-paths} (default: Actuator health)</li>
 *   <li>Requires JWT authentication for all other endpoints</li>
 * </ul>
 *
 * <p>Consuming services can override the entire security chain by declaring their own
 * {@link SecurityFilterChain} bean.
 */
@AutoConfiguration(afterName = "org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration")
@ConditionalOnClass(SecurityFilterChain.class)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@EnableConfigurationProperties(SecurityProperties.class)
public class SecurityAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(SecurityFilterChain.class)
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            SecurityProperties properties) throws Exception {
        http
                .authorizeHttpRequests(auth -> {
                    properties.publicPaths()
                            .forEach(path -> auth.requestMatchers(path).permitAll());
                    auth.anyRequest().authenticated();
                })
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));
        return http.build();
    }
}
