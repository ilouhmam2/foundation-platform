package fr.francetv.foundation.api;

import fr.francetv.foundation.api.error.GlobalExceptionHandler;
import fr.francetv.foundation.api.properties.ApiProperties;
import fr.francetv.foundation.common.FoundationBusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests for {@link GlobalExceptionHandler} using standalone MockMvc.
 *
 * <p>{@code @WebMvcTest} is not used here because the local
 * {@code spring-boot-test-autoconfigure} build does not include the web servlet slice.
 * The standalone setup is fully equivalent for testing a {@code @RestControllerAdvice}.
 */
class GlobalExceptionHandlerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        ApiProperties properties = new ApiProperties(false);
        GlobalExceptionHandler handler = new GlobalExceptionHandler(properties);
        TestApiController controller = new TestApiController();

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(handler)
                .build();
    }

    @Test
    void shouldReturn400ForValidationError() throws Exception {
        mockMvc.perform(post("/test/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").isNotEmpty())
                .andExpect(jsonPath("$.path").value("/test/validate"));
    }

    @Test
    void shouldReturn404ForResourceNotFound() throws Exception {
        mockMvc.perform(get("/test/not-found"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.path").value("/test/not-found"));
    }

    @Test
    void shouldIncludeCorrelationIdInErrorResponse() throws Exception {
        mockMvc.perform(get("/test/error")
                        .header("X-Correlation-Id", "test-corr-123"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.correlationId").value("test-corr-123"));
    }

    @Test
    void shouldReturn500ForUnhandledException() throws Exception {
        mockMvc.perform(get("/test/error"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.error").value("Internal Server Error"))
                .andExpect(jsonPath("$.message").value("An internal server error occurred"))
                .andExpect(jsonPath("$.path").value("/test/error"));
    }

    @Test
    void shouldReturn400ForConstraintViolationException() throws Exception {
        mockMvc.perform(get("/test/constraint-violation"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("name: must not be blank"));
    }

    @Test
    void shouldReturn400ForMalformedJson() throws Exception {
        mockMvc.perform(post("/test/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("not-valid-json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Malformed or unreadable request body"));
    }

    @Test
    void shouldReturn400ForFoundationBusinessException() throws Exception {
        mockMvc.perform(get("/test/business-error"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("item.not.available"));
    }

    @Test
    void shouldMaskExceptionMessageByDefault() throws Exception {
        mockMvc.perform(get("/test/error"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("An internal server error occurred"));
    }

    @Test
    void shouldExposeExceptionMessageWhenEnabled() throws Exception {
        ApiProperties propertiesWithMessageEnabled = new ApiProperties(true);
        GlobalExceptionHandler handlerWithMessage = new GlobalExceptionHandler(propertiesWithMessageEnabled);

        MockMvc mockMvcWithMessage = MockMvcBuilders.standaloneSetup(new TestApiController())
                .setControllerAdvice(handlerWithMessage)
                .build();

        mockMvcWithMessage.perform(get("/test/error"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("unexpected error"));
    }
}
