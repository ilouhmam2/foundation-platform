package fr.francetv.foundation.nats.autoconfigure;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.francetv.foundation.nats.properties.NatsProperties;
import fr.francetv.foundation.nats.publisher.NatsMessagePublisher;
import io.nats.client.Connection;
import io.nats.client.Nats;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import java.io.IOException;

/**
 * Auto-configuration for NATS messaging.
 *
 * <p>Activates when both the NATS Java client ({@code io.nats.client.Connection}) and
 * Jackson ({@code ObjectMapper}) are on the classpath. Provides:
 * <ul>
 *   <li>A NATS {@link Connection} bean — overrideable via {@code @Bean}</li>
 *   <li>A {@link NatsMessagePublisher} bean for publishing messages with the standard envelope</li>
 * </ul>
 *
 * <p>Jackson must be on the classpath (e.g. via {@code spring-boot-starter-json} or
 * {@code spring-boot-starter-web}). Consuming services that only use NATS without a web
 * stack must declare {@code jackson-databind} or {@code spring-boot-starter-json} explicitly.
 *
 * <p>Configure the server URL via:
 * <pre>{@code
 * foundation.nats.server-url=nats://nats-server:4222
 * }</pre>
 */
@AutoConfiguration
@ConditionalOnClass({Connection.class, ObjectMapper.class})
@EnableConfigurationProperties(NatsProperties.class)
public class NatsAutoConfiguration {

    /**
     * Provides a minimal {@link ObjectMapper} when none is already registered.
     * Registers all available Jackson modules on the classpath (e.g. JavaTimeModule
     * for {@code java.time.Instant} support) via {@link ObjectMapper#findAndRegisterModules()}.
     *
     * <p>Consuming services that already declare an {@link ObjectMapper} bean (e.g. via
     * a web starter) will have that bean injected into the publisher instead.
     */
    @Bean
    @ConditionalOnMissingBean(ObjectMapper.class)
    public ObjectMapper natsObjectMapper() {
        return new ObjectMapper().findAndRegisterModules();
    }

    @Bean(destroyMethod = "close")
    @ConditionalOnMissingBean
    public Connection natsConnection(NatsProperties properties) throws IOException, InterruptedException {
        try {
            return Nats.connect(properties.serverUrl());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw e;
        }
    }

    @Bean
    @ConditionalOnMissingBean
    public NatsMessagePublisher natsMessagePublisher(
            Connection connection,
            ObjectMapper objectMapper,
            @Value("${spring.application.name:}") String applicationName) {
        return new NatsMessagePublisher(connection, objectMapper, applicationName);
    }
}
