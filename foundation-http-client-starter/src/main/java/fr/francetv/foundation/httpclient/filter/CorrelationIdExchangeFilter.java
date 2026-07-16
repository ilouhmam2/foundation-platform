package fr.francetv.foundation.httpclient.filter;

import fr.francetv.foundation.common.CorrelationIdUtils;
import fr.francetv.foundation.common.FoundationHeaders;
import org.slf4j.MDC;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import reactor.core.publisher.Mono;

/**
 * {@link ExchangeFilterFunction} that propagates the correlation ID to outgoing HTTP requests.
 *
 * <p>Reads the correlation ID from SLF4J MDC (key: {@code correlationId}).
 * If absent (e.g. the call originates outside an HTTP request context), a new UUID is generated.
 *
 * <p>The value is added as {@code X-Correlation-Id} header on the outgoing request.
 * If the header is already present on the request, it is preserved as-is.
 */
public class CorrelationIdExchangeFilter implements ExchangeFilterFunction {

    static final String MDC_CORRELATION_ID_KEY = "correlationId";

    @Override
    public Mono<ClientResponse> filter(ClientRequest request, ExchangeFunction next) {
        if (request.headers().getFirst(FoundationHeaders.CORRELATION_ID) != null) {
            return next.exchange(request);
        }

        String correlationId = MDC.get(MDC_CORRELATION_ID_KEY);
        if (correlationId == null || correlationId.isBlank()) {
            correlationId = CorrelationIdUtils.generate();
        }

        ClientRequest enriched = ClientRequest.from(request)
                .header(FoundationHeaders.CORRELATION_ID, correlationId)
                .build();
        return next.exchange(enriched);
    }
}
