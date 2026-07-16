package fr.francetv.foundation.nats.publisher;

import java.time.Instant;

/**
 * Standard NATS message envelope.
 *
 * <p>Every message published through {@link NatsMessagePublisher} is wrapped in this
 * envelope to provide a consistent structure across all services.
 *
 * @param id           unique message identifier (UUID)
 * @param correlationId request correlation ID for distributed tracing
 * @param source       name of the publishing service (from {@code spring.application.name})
 * @param type         event or command type (e.g. {@code quote.created})
 * @param timestamp    publication time in UTC
 * @param payload      business payload — serialized as a nested JSON object
 */
public record MessageEnvelope(
        String id,
        String correlationId,
        String source,
        String type,
        Instant timestamp,
        Object payload
) {
}
