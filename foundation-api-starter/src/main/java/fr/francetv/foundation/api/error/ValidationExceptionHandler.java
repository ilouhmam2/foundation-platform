package fr.francetv.foundation.api.error;

import fr.francetv.foundation.common.FoundationHeaders;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Exception handler for Jakarta Bean Validation violations.
 *
 * <p>Registered automatically by {@code ApiAutoConfiguration} only when
 * {@code jakarta.validation.ConstraintViolationException} is on the classpath,
 * i.e. when the consuming service declares {@code spring-boot-starter-validation}.
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
public class ValidationExceptionHandler {

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleConstraintViolation(
            ConstraintViolationException ex, HttpServletRequest request) {
        var violations = ex.getConstraintViolations();
        List<String> errors = (violations != null && !violations.isEmpty())
                ? violations.stream()
                        .map(cv -> cv.getPropertyPath() + ": " + cv.getMessage())
                        .collect(Collectors.toList())
                : null;
        String message = (errors != null && !errors.isEmpty())
                ? String.join(", ", errors)
                : ex.getMessage();
        return buildResponse(HttpStatus.BAD_REQUEST, message, errors, request);
    }

    private ResponseEntity<ApiErrorResponse> buildResponse(
            HttpStatus status, String message, List<String> errors, HttpServletRequest request) {
        String fromMdc = MDC.get("correlationId");
        String correlationId = (fromMdc != null && !fromMdc.isBlank())
                ? fromMdc
                : request.getHeader(FoundationHeaders.CORRELATION_ID);
        return ResponseEntity.status(status).body(new ApiErrorResponse(
                DateTimeFormatter.ISO_INSTANT.format(Instant.now()),
                status.value(),
                status.getReasonPhrase(),
                message,
                request.getRequestURI(),
                correlationId,
                errors));
    }
}
