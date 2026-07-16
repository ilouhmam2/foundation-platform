package fr.francetv.foundation.soapclient.interceptor;

import com.sun.net.httpserver.HttpServer;
import fr.francetv.foundation.common.FoundationHeaders;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.apache.cxf.message.Message;
import org.apache.cxf.message.MessageImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class CorrelationIdSoapInterceptorTest {

    private static final String SOAP_FAULT = """
            <soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
              <soap:Body>
                <soap:Fault>
                  <faultcode>soap:Server</faultcode>
                  <faultstring>stub</faultstring>
                </soap:Fault>
              </soap:Body>
            </soap:Envelope>
            """;

    private final CorrelationIdSoapInterceptor interceptor = new CorrelationIdSoapInterceptor();

    @AfterEach
    void clearMdc() {
        MDC.remove("correlationId");
    }

    // --- Unit tests ---

    @Test
    void shouldAddCorrelationIdHeaderToOutboundMessage() {
        Message message = new MessageImpl();
        Map<String, List<String>> headers = new HashMap<>();
        message.put(Message.PROTOCOL_HEADERS, headers);

        MDC.put("correlationId", "test-id-123");
        interceptor.handleMessage(message);

        assertThat(headers).containsKey(FoundationHeaders.CORRELATION_ID);
        assertThat(headers.get(FoundationHeaders.CORRELATION_ID)).containsExactly("test-id-123");
    }

    @Test
    void shouldGenerateCorrelationIdWhenMdcEmpty() {
        Message message = new MessageImpl();
        Map<String, List<String>> headers = new HashMap<>();
        message.put(Message.PROTOCOL_HEADERS, headers);

        MDC.remove("correlationId");
        interceptor.handleMessage(message);

        assertThat(headers).containsKey(FoundationHeaders.CORRELATION_ID);
        assertThat(headers.get(FoundationHeaders.CORRELATION_ID))
                .first().asString().isNotBlank().matches("[0-9a-f\\-]{36}");
    }

    @Test
    void shouldNotOverwriteExistingCorrelationIdHeader() {
        Message message = new MessageImpl();
        Map<String, List<String>> headers = new HashMap<>();
        headers.put(FoundationHeaders.CORRELATION_ID, List.of("existing-id"));
        message.put(Message.PROTOCOL_HEADERS, headers);

        MDC.put("correlationId", "mdc-id");
        interceptor.handleMessage(message);

        assertThat(headers.get(FoundationHeaders.CORRELATION_ID)).containsExactly("existing-id");
    }

    @Test
    void shouldCreateProtocolHeadersMapWhenAbsent() {
        Message message = new MessageImpl();

        MDC.put("correlationId", "map-created-id");
        interceptor.handleMessage(message);

        @SuppressWarnings("unchecked")
        Map<String, List<String>> headers =
                (Map<String, List<String>>) message.get(Message.PROTOCOL_HEADERS);
        assertThat(headers).isNotNull().containsKey(FoundationHeaders.CORRELATION_ID);
    }

    // --- Integration test: verifies the header reaches the HTTP wire ---

    @Test
    void shouldSendCorrelationIdHeaderOnOutboundSoapCall() throws IOException {
        AtomicReference<String> capturedHeader = new AtomicReference<>();

        HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/soap", exchange -> {
            capturedHeader.set(exchange.getRequestHeaders().getFirst("X-Correlation-Id"));
            byte[] body = SOAP_FAULT.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "text/xml;charset=UTF-8");
            exchange.sendResponseHeaders(200, body.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(body);
            }
        });
        server.start();

        try {
            String address = "http://localhost:" + server.getAddress().getPort() + "/soap";
            JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();
            factory.setServiceClass(TestSoapService.class);
            factory.setAddress(address);
            factory.getOutInterceptors().add(interceptor);
            TestSoapService proxy = factory.create(TestSoapService.class);

            MDC.put("correlationId", "http-wire-test-id");
            try {
                proxy.ping();
            } catch (Exception ignored) {
                // SOAP fault from stub — expected
            }
        } finally {
            server.stop(0);
        }

        assertThat(capturedHeader.get()).isEqualTo("http-wire-test-id");
    }

    @Test
    void shouldGenerateCorrelationIdWhenMdcEmptyOnWire() throws IOException {
        AtomicReference<String> capturedHeader = new AtomicReference<>();

        HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/soap", exchange -> {
            capturedHeader.set(exchange.getRequestHeaders().getFirst("X-Correlation-Id"));
            byte[] body = SOAP_FAULT.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "text/xml;charset=UTF-8");
            exchange.sendResponseHeaders(200, body.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(body);
            }
        });
        server.start();

        try {
            String address = "http://localhost:" + server.getAddress().getPort() + "/soap";
            JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();
            factory.setServiceClass(TestSoapService.class);
            factory.setAddress(address);
            factory.getOutInterceptors().add(interceptor);
            TestSoapService proxy = factory.create(TestSoapService.class);

            MDC.remove("correlationId");
            try {
                proxy.ping();
            } catch (Exception ignored) {
                // SOAP fault from stub — expected
            }
        } finally {
            server.stop(0);
        }

        assertThat(capturedHeader.get())
                .isNotBlank()
                .matches("[0-9a-f\\-]{36}");
    }
}
