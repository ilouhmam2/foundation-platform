package fr.francetv.foundation.nats.publisher;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.francetv.foundation.common.CorrelationIdUtils;
import fr.francetv.foundation.common.FoundationTechnicalException;
import io.nats.client.Connection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.UUID;

/**
 * Spring bean for publishing NATS messages wrapped in the standard {@link MessageEnvelope}.
 *
 * <p>The envelope is automatically populated with:
 * <ul>
 *   <li>A generated UUID as message {@code id}</li>
 *   <li>The current UTC {@code timestamp}</li>
 *   <li>The {@code source} from {@code spring.application.name}</li>
 * </ul>
 *
 * <p>The caller provides the {@code subject}, {@code type}, optional {@code correlationId},
 * and the business {@code payload}.
 *
 * <p>Example usage:
 * <pre>{@code
 * publisher.publish("quote.created", "quote.created", correlationId, quoteDto);
 * }</pre>
 */
public class NatsMessagePublisher {

    private static final Logger log = LoggerFactory.getLogger(NatsMessagePublisher.class);

    private final Connection connection;
    private final ObjectMapper objectMapper;
    private final String source;

    public NatsMessagePublisher(Connection connection, ObjectMapper objectMapper, String source) {
        this.connection = connection;
        this.objectMapper = objectMapper;
        this.source = source;
    }

    /**
     * Publish a message with a generated correlation ID.
     *
     * @param subject NATS subject (e.g. {@code quote.created})
     * @param type    event or command type
     * @param payload business payload
     */
    public void publish(String subject, String type, Object payload) {
        publish(subject, type, CorrelationIdUtils.generate(), payload);
    }

    /**
     * Publish a message with an explicit correlation ID for request tracing.
     *
     * @param subject       NATS subject
     * @param type          event or command type
     * @param correlationId correlation ID propagated from the incoming request
     * @param payload       business payload
     */
    public void publish(String subject, String type, String correlationId, Object payload) {
        MessageEnvelope envelope = new MessageEnvelope(
                UUID.randomUUID().toString(),
                correlationId,
                source,
                type,
                Instant.now(),
                payload
        );
        byte[] data;
        try {
            data = objectMapper.writeValueAsBytes(envelope);
        } catch (JsonProcessingException e) {
            throw new FoundationTechnicalException(
                    "Failed to serialize NATS message for subject '" + subject + "'", e);
        }
        connection.publish(subject, data);
        log.debug("Published message to subject '{}' with type '{}', correlationId '{}'",
                subject, type, correlationId);
    }
}
