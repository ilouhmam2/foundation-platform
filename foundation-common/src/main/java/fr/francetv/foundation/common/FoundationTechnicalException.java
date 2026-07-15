package fr.francetv.foundation.common;

/**
 * Indicates an infrastructure or server-side failure (database, external service, etc.).
 *
 * <p>Mapped to HTTP 5xx by {@code foundation-api-starter}.
 */
public class FoundationTechnicalException extends FoundationException {

    public FoundationTechnicalException(String message) {
        super(message);
    }

    public FoundationTechnicalException(String message, Throwable cause) {
        super(message, cause);
    }
}
