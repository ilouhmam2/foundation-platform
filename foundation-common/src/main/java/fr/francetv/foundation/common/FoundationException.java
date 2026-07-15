package fr.francetv.foundation.common;

/**
 * Base exception for all foundation-platform errors.
 *
 * <p>Subtypes carry semantic meaning for HTTP error mapping:
 * <ul>
 *   <li>{@link FoundationTechnicalException} — infrastructure failures, mapped to HTTP 5xx
 *   <li>{@link FoundationBusinessException} — business rule violations, mapped to HTTP 4xx
 * </ul>
 *
 * <p>The actual HTTP mapping is the responsibility of {@code foundation-api-starter}.
 */
public class FoundationException extends RuntimeException {

    public FoundationException(String message) {
        super(message);
    }

    public FoundationException(String message, Throwable cause) {
        super(message, cause);
    }
}
