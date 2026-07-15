package fr.francetv.foundation.common;

import lombok.experimental.UtilityClass;

import java.util.Map;
import java.util.UUID;

@UtilityClass
public class CorrelationIdUtils {

    public static String generate() {
        return UUID.randomUUID().toString();
    }

    public static String extractOrGenerate(Map<String, String> headers) {
        String value = headers.get(FoundationHeaders.CORRELATION_ID);
        return (value != null && !value.isBlank()) ? value : generate();
    }
}
