package fr.francetv.foundation.nats.autoconfigure;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.francetv.foundation.nats.publisher.MessageEnvelope;
import fr.francetv.foundation.nats.publisher.NatsMessagePublisher;
import io.nats.client.Connection;
import io.nats.client.Message;
import io.nats.client.Subscription;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Duration;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers(disabledWithoutDocker = true)
class NatsAutoConfigurationIntegrationTest {

    @Container
    static GenericContainer<?> natsServer =
            new GenericContainer<>("nats:2.10-alpine")
                    .withExposedPorts(4222);

    private ApplicationContextRunner contextRunner;

    @BeforeEach
    void setUp() {
        String serverUrl = "nats://" + natsServer.getHost() + ":" + natsServer.getMappedPort(4222);
        contextRunner = new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(NatsAutoConfiguration.class))
                .withBean(ObjectMapper.class, () -> new ObjectMapper().registerModule(new JavaTimeModule()))
                .withPropertyValues(
                        "foundation.nats.server-url=" + serverUrl,
                        "spring.application.name=test-service"
                );
    }

    @Test
    void shouldConnectToNats() {
        contextRunner.run(ctx -> {
            assertThat(ctx).hasNotFailed();
            assertThat(ctx).hasSingleBean(Connection.class);
            assertThat(ctx.getBean(Connection.class).getStatus())
                    .isEqualTo(Connection.Status.CONNECTED);
        });
    }

    @Test
    void shouldPublishMessageWithEnvelope() {
        contextRunner.run(ctx -> {
            Connection connection = ctx.getBean(Connection.class);
            NatsMessagePublisher publisher = ctx.getBean(NatsMessagePublisher.class);
            ObjectMapper objectMapper = ctx.getBean(ObjectMapper.class);

            Subscription subscription = connection.subscribe("test.event");

            publisher.publish("test.event", "test.created", "corr-id-123", Map.of("key", "value"));

            Message message = subscription.nextMessage(Duration.ofSeconds(5));

            assertThat(message).isNotNull();

            MessageEnvelope envelope = objectMapper.readValue(message.getData(), MessageEnvelope.class);
            assertThat(envelope.id()).isNotBlank();
            assertThat(envelope.type()).isEqualTo("test.created");
            assertThat(envelope.correlationId()).isEqualTo("corr-id-123");
            assertThat(envelope.source()).isEqualTo("test-service");
            assertThat(envelope.timestamp()).isNotNull();
            assertThat(envelope.payload()).isNotNull();
        });
    }

    @Test
    void shouldIncludeCorrelationIdInEnvelope() {
        contextRunner.run(ctx -> {
            Connection connection = ctx.getBean(Connection.class);
            NatsMessagePublisher publisher = ctx.getBean(NatsMessagePublisher.class);
            ObjectMapper objectMapper = ctx.getBean(ObjectMapper.class);

            Subscription subscription = connection.subscribe("test.correlation");

            publisher.publish("test.correlation", "test.type", "my-correlation-id-456", Map.of());

            Message message = subscription.nextMessage(Duration.ofSeconds(5));

            assertThat(message).isNotNull();

            MessageEnvelope envelope = objectMapper.readValue(message.getData(), MessageEnvelope.class);
            assertThat(envelope.correlationId()).isEqualTo("my-correlation-id-456");
        });
    }
}
