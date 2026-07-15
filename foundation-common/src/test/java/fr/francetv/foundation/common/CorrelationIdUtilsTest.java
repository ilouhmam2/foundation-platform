package fr.francetv.foundation.common;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class CorrelationIdUtilsTest {

    @Test
    void shouldGenerateNonBlankUuid() {
        String id = CorrelationIdUtils.generate();
        assertThat(id).isNotBlank();
    }

    @Test
    void shouldGenerateUniqueValues() {
        assertThat(CorrelationIdUtils.generate()).isNotEqualTo(CorrelationIdUtils.generate());
    }

    @Test
    void shouldExtractExistingCorrelationId() {
        Map<String, String> headers = Map.of(FoundationHeaders.CORRELATION_ID, "test-id-123");
        assertThat(CorrelationIdUtils.extractOrGenerate(headers)).isEqualTo("test-id-123");
    }

    @Test
    void shouldGenerateWhenHeaderAbsent() {
        String id = CorrelationIdUtils.extractOrGenerate(Map.of());
        assertThat(id).isNotBlank();
    }

    @Test
    void shouldGenerateWhenHeaderBlank() {
        Map<String, String> headers = new HashMap<>();
        headers.put(FoundationHeaders.CORRELATION_ID, "  ");
        String id = CorrelationIdUtils.extractOrGenerate(headers);
        assertThat(id).isNotBlank();
    }
}
