package fr.francetv.foundation.api;

import fr.francetv.foundation.common.FoundationBusinessException;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;

/**
 * Minimal controller used to drive the {@link GlobalExceptionHandlerTest} scenarios.
 */
@RestController
public class TestApiController {

    record TestRequest(@NotBlank String name) {
    }

    @PostMapping("/test/validate")
    public void validate(@RequestBody @Valid TestRequest request) {
    }

    @GetMapping("/test/error")
    public void error() {
        throw new RuntimeException("unexpected error");
    }

    @GetMapping("/test/not-found")
    public void notFound() {
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Resource not found");
    }

    @GetMapping("/test/business-error")
    public void businessError() {
        throw new FoundationBusinessException("item.not.available");
    }

    @GetMapping("/test/constraint-violation")
    public void constraintViolation() {
        throw new ConstraintViolationException("name: must not be blank", Collections.emptySet());
    }
}
