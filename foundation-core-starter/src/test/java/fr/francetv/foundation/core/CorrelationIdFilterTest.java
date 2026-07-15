package fr.francetv.foundation.core;

import fr.francetv.foundation.common.FoundationHeaders;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;

class CorrelationIdFilterTest {

    private final CorrelationIdFilter filter = new CorrelationIdFilter();

    @AfterEach
    void clearMdc() {
        MDC.remove(CorrelationIdFilter.MDC_KEY);
    }

    @Test
    void shouldPropagateExistingCorrelationId() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(FoundationHeaders.CORRELATION_ID, "existing-id");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, (req, res) -> {});

        assertThat(response.getHeader(FoundationHeaders.CORRELATION_ID)).isEqualTo("existing-id");
    }

    @Test
    void shouldGenerateCorrelationIdWhenAbsent() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, (req, res) -> {});

        assertThat(response.getHeader(FoundationHeaders.CORRELATION_ID)).isNotBlank();
    }

    @Test
    void shouldSetMdcDuringFilterChain() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(FoundationHeaders.CORRELATION_ID, "mdc-test-id");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, (req, res) ->
                assertThat(MDC.get(CorrelationIdFilter.MDC_KEY)).isEqualTo("mdc-test-id"));
    }

    @Test
    void shouldClearMdcAfterFilterChain() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, (req, res) -> {});

        assertThat(MDC.get(CorrelationIdFilter.MDC_KEY)).isNull();
    }
}
