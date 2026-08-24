package fr.francetv.foundation.nats.health;

import io.nats.client.Connection;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;

/**
 * Actuator health indicator for the NATS connection.
 *
 * <p>Reports:
 * <ul>
 *   <li>{@code UP} — connection is {@code CONNECTED}</li>
 *   <li>{@code UNKNOWN} — connection is {@code CONNECTING} or {@code RECONNECTING}</li>
 *   <li>{@code DOWN} — connection is {@code CLOSED} or {@code DISCONNECTED}</li>
 * </ul>
 *
 * <p>Registered automatically when both {@code io.nats.client.Connection} and
 * {@code spring-boot-actuator} are on the classpath.
 * Override by declaring your own {@link HealthIndicator} bean named {@code natsHealthIndicator}.
 */
public class NatsHealthIndicator implements HealthIndicator {

    private final Connection connection;

    public NatsHealthIndicator(Connection connection) {
        this.connection = connection;
    }

    @Override
    public Health health() {
        Connection.Status status = connection.getStatus();
        return switch (status) {
            case CONNECTED -> {
                Health.Builder builder = Health.up().withDetail("status", status.name());
                String serverUrl = connection.getConnectedUrl();
                if (serverUrl != null) {
                    builder.withDetail("serverUrl", serverUrl);
                }
                yield builder.build();
            }
            case CONNECTING, RECONNECTING -> Health.unknown()
                    .withDetail("status", status.name())
                    .build();
            default -> Health.down()
                    .withDetail("status", status.name())
                    .build();
        };
    }
}
