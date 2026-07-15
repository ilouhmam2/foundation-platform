package fr.francetv.foundation.common;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FoundationHeadersTest {

    @Test
    void shouldDefineCorrelationIdHeader() {
        assertThat(FoundationHeaders.CORRELATION_ID).isEqualTo("X-Correlation-Id");
    }
}
