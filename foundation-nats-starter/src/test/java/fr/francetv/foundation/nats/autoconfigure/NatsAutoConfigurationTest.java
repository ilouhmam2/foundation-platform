package fr.francetv.foundation.nats.autoconfigure;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.francetv.foundation.nats.publisher.NatsMessagePublisher;
import io.nats.client.Connection;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class NatsAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner =
            new ApplicationContextRunner()
                    .withConfiguration(AutoConfigurations.of(NatsAutoConfiguration.class));

    @Test
    void shouldNotActivateWhenNatsClientAbsent() {
        new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(NatsAutoConfiguration.class))
                .withClassLoader(new FilteredClassLoader(Connection.class))
                .run(ctx -> {
                    assertThat(ctx).hasNotFailed();
                    assertThat(ctx).doesNotHaveBean(NatsMessagePublisher.class);
                });
    }

    @Test
    void shouldCreatePublisherWhenNatsBeanProvided() {
        contextRunner
                .withBean(Connection.class, () -> mock(Connection.class))
                .withPropertyValues("spring.application.name=test-service")
                .run(ctx -> assertThat(ctx).hasSingleBean(NatsMessagePublisher.class));
    }

    @Test
    void shouldRespectCustomPublisher() {
        NatsMessagePublisher customPublisher =
                new NatsMessagePublisher(mock(Connection.class), new ObjectMapper(), "custom");

        contextRunner
                .withBean(Connection.class, () -> mock(Connection.class))
                .withBean(NatsMessagePublisher.class, () -> customPublisher)
                .run(ctx -> assertThat(ctx.getBean(NatsMessagePublisher.class))
                        .isSameAs(customPublisher));
    }
}
