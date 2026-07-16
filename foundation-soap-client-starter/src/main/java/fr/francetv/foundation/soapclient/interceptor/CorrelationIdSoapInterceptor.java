package fr.francetv.foundation.soapclient.interceptor;

import fr.francetv.foundation.common.CorrelationIdUtils;
import fr.francetv.foundation.common.FoundationHeaders;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
import org.slf4j.MDC;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * CXF outbound interceptor that propagates the correlation ID to outgoing SOAP requests.
 *
 * <p>Reads the correlation ID from SLF4J MDC (key: {@code correlationId}).
 * If absent, a new UUID is generated.
 *
 * <p>The value is added as {@code X-Correlation-Id} HTTP protocol header on the outgoing request.
 * If the header is already present, it is preserved as-is.
 */
public class CorrelationIdSoapInterceptor extends AbstractPhaseInterceptor<Message> {

    static final String MDC_CORRELATION_ID_KEY = "correlationId";

    public CorrelationIdSoapInterceptor() {
        super(Phase.PRE_STREAM);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void handleMessage(Message message) {
        Map<String, List<String>> headers =
                (Map<String, List<String>>) message.get(Message.PROTOCOL_HEADERS);
        if (headers == null) {
            headers = new HashMap<>();
            message.put(Message.PROTOCOL_HEADERS, headers);
        }
        if (headers.containsKey(FoundationHeaders.CORRELATION_ID)) {
            return;
        }
        String correlationId = MDC.get(MDC_CORRELATION_ID_KEY);
        if (correlationId == null || correlationId.isBlank()) {
            correlationId = CorrelationIdUtils.generate();
        }
        headers.put(FoundationHeaders.CORRELATION_ID, Collections.singletonList(correlationId));
    }
}
