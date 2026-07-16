package fr.francetv.foundation.sample.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Sample controller demonstrating a secured endpoint with correlation ID propagation.
 *
 * <p>The {@code X-Correlation-Id} header is automatically extracted and re-attached to
 * the response by {@code CorrelationIdFilter} (from {@code foundation-core-starter}).
 */
@RestController
@RequestMapping("/api/v1")
public class HelloController {

    @GetMapping("/hello")
    public ResponseEntity<HelloResponse> hello() {
        return ResponseEntity.ok(new HelloResponse("Hello from foundation-sample-service"));
    }
}
