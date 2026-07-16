package fr.francetv.foundation.test.wiremock;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * JUnit 5 extension that provides a pre-configured {@link WireMockServer} for HTTP stub tests.
 *
 * <p>Each test class that declares a {@link RegisterExtension} field gets its own independent
 * WireMock server. This is safe for parallel test class execution, unlike the static-field
 * inheritance pattern.
 *
 * <p>Usage:
 * <pre>{@code
 * class MyHttpClientTest {
 *
 *     @RegisterExtension
 *     static WireMockSupport wireMock = WireMockSupport.create();
 *
 *     @Test
 *     void shouldCallExternalService() {
 *         wireMock.server().stubFor(get("/api/resource")
 *             .willReturn(okJson("{\"id\": 1}")));
 *
 *         String result = myClient.fetch(wireMock.baseUrl() + "/api/resource");
 *     }
 * }
 * }</pre>
 */
public final class WireMockSupport implements BeforeAllCallback, AfterAllCallback, AfterEachCallback {

    private WireMockServer wireMockServer;

    private WireMockSupport() {
    }

    /**
     * Creates a new {@link WireMockSupport} extension. Declare as a
     * {@code static @RegisterExtension} field in your test class.
     */
    public static WireMockSupport create() {
        return new WireMockSupport();
    }

    @Override
    public void beforeAll(ExtensionContext context) {
        wireMockServer = new WireMockServer(WireMockConfiguration.options().dynamicPort());
        wireMockServer.start();
    }

    @Override
    public void afterAll(ExtensionContext context) {
        if (wireMockServer != null) {
            wireMockServer.stop();
        }
    }

    @Override
    public void afterEach(ExtensionContext context) {
        if (wireMockServer != null) {
            wireMockServer.resetAll();
        }
    }

    /**
     * Returns the underlying {@link WireMockServer} for stubbing.
     */
    public WireMockServer server() {
        return wireMockServer;
    }

    /**
     * Returns the base URL of the WireMock server (e.g. {@code http://localhost:58432}).
     */
    public String baseUrl() {
        return "http://localhost:" + wireMockServer.port();
    }
}
