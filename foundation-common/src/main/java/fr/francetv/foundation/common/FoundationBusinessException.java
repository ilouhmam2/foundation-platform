package fr.francetv.foundation.common;

/**
 * Indicates a business rule violation or an invalid client request.
 *
 * <p>Mapped to HTTP 4xx by {@code foundation-api-starter}.
 */
public class FoundationBusinessException extends FoundationException {

    public FoundationBusinessException(String message) {
        super(message);
    }

    public FoundationBusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}
