package fr.francetv.foundation.sample;

import fr.francetv.foundation.api.error.ApiErrorResponse;
import fr.francetv.foundation.common.FoundationHeaders;
import fr.francetv.foundation.sample.api.HelloResponse;
import fr.francetv.foundation.test.security.JwtTestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.security.interfaces.RSAPublicKey;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SampleServiceSmokeTest {

    @LocalServerPort
    int port;

    @Autowired
    JwtTestUtils jwtTestUtils;

    RestTemplate restTemplate;

    @BeforeEach
    void setUp() {
        restTemplate = new RestTemplate();
        restTemplate.setErrorHandler(new DefaultResponseErrorHandler() {
            @Override
            public boolean hasError(ClientHttpResponse response) throws IOException {
                return false; // suppress — tests inspect the full response directly
            }
        });
    }

    @TestConfiguration
    static class TestSecurityConfig {

        @Bean
        JwtDecoder jwtDecoder(JwtTestUtils jwtTestUtils) throws Exception {
            RSAPublicKey publicKey = jwtTestUtils.getPublicRsaKey().toRSAPublicKey();
            return NimbusJwtDecoder.withPublicKey(publicKey).build();
        }
    }

    private String url(String path) {
        return "http://localhost:" + port + path;
    }

    @Test
    void applicationContextLoadsSuccessfully() {
        // context loaded by @SpringBootTest — passing this test confirms startup
    }

    @Test
    void healthEndpointIsPublic() {
        ResponseEntity<String> response = restTemplate.getForEntity(url("/actuator/health"), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void helloEndpointRequiresAuthentication() {
        ResponseEntity<String> response = restTemplate.getForEntity(url("/api/v1/hello"), String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void helloEndpointReturnsCorrelationId() {
        String token = jwtTestUtils.generateToken("test-user");
        String correlationId = "test-correlation-123";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.set(FoundationHeaders.CORRELATION_ID, correlationId);

        ResponseEntity<HelloResponse> response = restTemplate.exchange(
                url("/api/v1/hello"),
                HttpMethod.GET,
                new HttpEntity<>(headers),
                HelloResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message()).isEqualTo("Hello from foundation-sample-service");
        assertThat(response.getHeaders().getFirst(FoundationHeaders.CORRELATION_ID))
                .isEqualTo(correlationId);
    }

    @Test
    void errorResponseFollowsStandardModel() {
        String token = jwtTestUtils.generateToken("test-user");

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.set(FoundationHeaders.CORRELATION_ID, "err-correlation-456");

        ResponseEntity<ApiErrorResponse> response = restTemplate.exchange(
                url("/api/v1/nonexistent"),
                HttpMethod.GET,
                new HttpEntity<>(headers),
                ApiErrorResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(404);
        assertThat(response.getBody().error()).isEqualTo("Not Found");
        assertThat(response.getBody().path()).isEqualTo("/api/v1/nonexistent");
        assertThat(response.getBody().correlationId()).isEqualTo("err-correlation-456");
        assertThat(response.getBody().timestamp()).isNotBlank();
    }
}
