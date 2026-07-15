package fr.francetv.foundation.api.error;

/**
 * Standard error response body returned by the global exception handler.
 *
 * <p>{@code timestamp} is an ISO-8601 UTC string, e.g. {@code 2026-07-15T10:00:00Z}.
 * Typed as {@code String} to avoid a runtime dependency on a Jackson
 * {@code JavaTimeModule} that may not be configured in all consuming services' test setups.
 */
public record ApiErrorResponse(
        String timestamp,
        int status,
        String error,
        String message,
        String path,
        String correlationId) {
}
