package fr.francetv.foundation.soapclient.interceptor;

import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * CXF outbound interceptor that adds a Bearer token to the {@code Authorization} HTTP header.
 *
 * <p>The token is resolved at request time via the provided {@link Supplier}.
 * If the supplier returns {@code null} or a blank value, the header is not added.
 * If the {@code Authorization} header is already present on the request, it is preserved.
 *
 * <p>This interceptor is <strong>not</strong> auto-configured. Consuming services
 * instantiate it with a token supplier and add it to their {@link SoapClientFactory}:
 *
 * <pre>{@code
 * soapClientFactory.createWithBearerToken(MyService.class, address, this::getToken);
 * }</pre>
 */
public class BearerTokenSoapInterceptor extends AbstractPhaseInterceptor<Message> {

    private final Supplier<String> tokenSupplier;

    public BearerTokenSoapInterceptor(Supplier<String> tokenSupplier) {
        super(Phase.PRE_STREAM);
        this.tokenSupplier = tokenSupplier;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void handleMessage(Message message) {
        String token = tokenSupplier.get();
        if (token == null || token.isBlank()) {
            return;
        }
        Map<String, List<String>> headers =
                (Map<String, List<String>>) message.get(Message.PROTOCOL_HEADERS);
        if (headers == null) {
            headers = new HashMap<>();
            message.put(Message.PROTOCOL_HEADERS, headers);
        }
        if (!headers.containsKey("Authorization")) {
            headers.put("Authorization", Collections.singletonList("Bearer " + token));
        }
    }
}
