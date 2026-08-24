package fr.francetv.foundation.api.error;

import java.util.List;

/**
 * Standard error response body returned by the global exception handler.
 *
 * <p>{@code timestamp} is an ISO-8601 UTC string, e.g. {@code 2026-07-15T10:00:00Z}.
 * Typed as {@code String} to avoid a runtime dependency on a Jackson
 * {@code JavaTimeModule} that may not be configured in all consuming services' test setups.
 *
 * <p>{@code errors} carries individual field-level messages for validation failures
 * ({@code MethodArgumentNotValidException}). {@code null} for all other error types.
 */
public record ApiErrorResponse(
        String timestamp,
        int status,
        String error,
        String message,
        String path,
        String correlationId,
        List<String> errors) {
}
