package fr.francetv.foundation.api.error;

import fr.francetv.foundation.api.properties.ApiProperties;
import fr.francetv.foundation.common.FoundationBusinessException;
import fr.francetv.foundation.common.FoundationHeaders;
import fr.francetv.foundation.common.FoundationTechnicalException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Global exception handler that maps exceptions to the standard {@link ApiErrorResponse}.
 *
 * <p>Registered automatically by {@code ApiAutoConfiguration} when Spring Web MVC is on the
 * classpath. Consuming services can override it by declaring their own
 * {@code @RestControllerAdvice} bean.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    protected static final String MDC_CORRELATION_ID_KEY = "correlationId";

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private final ApiProperties properties;

    public GlobalExceptionHandler(ApiProperties properties) {
        this.properties = properties;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<String> errors = ex.getBindingResult().getAllErrors().stream()
                .map(error -> error instanceof FieldError fe
                        ? fe.getField() + ": " + fe.getDefaultMessage()
                        : error.getDefaultMessage())
                .collect(Collectors.toList());
        return buildResponse(HttpStatus.BAD_REQUEST, String.join(", ", errors), errors, request);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleMessageNotReadable(
            HttpMessageNotReadableException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.BAD_REQUEST, "Malformed or unreadable request body", request);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNoResourceFound(
            NoResourceFoundException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiErrorResponse> handleResponseStatus(
            ResponseStatusException ex, HttpServletRequest request) {
        HttpStatus status = HttpStatus.resolve(ex.getStatusCode().value());
        if (status == null) {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }
        String message = ex.getReason() != null ? ex.getReason() : status.getReasonPhrase();
        return buildResponse(status, message, request);
    }

    @ExceptionHandler(FoundationBusinessException.class)
    public ResponseEntity<ApiErrorResponse> handleBusinessException(
            FoundationBusinessException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    @ExceptionHandler(FoundationTechnicalException.class)
    public ResponseEntity<ApiErrorResponse> handleTechnicalException(
            FoundationTechnicalException ex, HttpServletRequest request) {
        log.error("Technical exception at {}: {}", request.getRequestURI(), ex.getMessage(), ex);
        String message = properties.includeExceptionMessage()
                ? ex.getMessage()
                : "An internal server error occurred";
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, message, request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGeneric(
            Exception ex, HttpServletRequest request) {
        log.error("Unhandled exception at {}: {}", request.getRequestURI(), ex.getMessage(), ex);
        String message = properties.includeExceptionMessage()
                ? ex.getMessage()
                : "An internal server error occurred";
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, message, request);
    }

    protected ResponseEntity<ApiErrorResponse> buildResponse(
            HttpStatus status, String message, List<String> errors, HttpServletRequest request) {
        return ResponseEntity.status(status).body(new ApiErrorResponse(
                DateTimeFormatter.ISO_INSTANT.format(Instant.now()),
                status.value(),
                status.getReasonPhrase(),
                message,
                request.getRequestURI(),
                resolveCorrelationId(request),
                errors));
    }

    protected ResponseEntity<ApiErrorResponse> buildResponse(
            HttpStatus status, String message, HttpServletRequest request) {
        return buildResponse(status, message, null, request);
    }

    protected String resolveCorrelationId(HttpServletRequest request) {
        String fromMdc = MDC.get(MDC_CORRELATION_ID_KEY);
        if (fromMdc != null && !fromMdc.isBlank()) {
            return fromMdc;
        }
        String fromHeader = request.getHeader(FoundationHeaders.CORRELATION_ID);
        return (fromHeader != null && !fromHeader.isBlank()) ? fromHeader : null;
    }
}
