package fr.francetv.foundation.soapclient.factory;

import fr.francetv.foundation.soapclient.interceptor.BearerTokenSoapInterceptor;
import fr.francetv.foundation.soapclient.properties.SoapClientProperties;
import jakarta.xml.ws.BindingProvider;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.interceptor.Interceptor;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.apache.cxf.message.Message;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Factory for creating Apache CXF JAX-WS proxy clients with standard foundation conventions:
 * <ul>
 *   <li>Connect and receive timeouts from {@code foundation.soap-client.*} properties</li>
 *   <li>All auto-configured outbound interceptors (e.g. {@code CorrelationIdSoapInterceptor})</li>
 * </ul>
 *
 * <p>Usage:
 * <pre>{@code
 * @Bean
 * MyService myService(SoapClientFactory factory) {
 *     return factory.create(MyService.class, "https://partner.example.com/ws/MyService");
 * }
 * }</pre>
 *
 * <p>For Basic auth:
 * <pre>{@code
 * factory.createWithBasicAuth(MyService.class, address, "user", "secret");
 * }</pre>
 *
 * <p>For Bearer token auth:
 * <pre>{@code
 * factory.createWithBearerToken(MyService.class, address, this::resolveToken);
 * }</pre>
 */
public class SoapClientFactory {

    private final SoapClientProperties properties;
    private final List<Interceptor<? extends Message>> outInterceptors;

    public SoapClientFactory(SoapClientProperties properties,
                              List<Interceptor<? extends Message>> outInterceptors) {
        this.properties = properties;
        this.outInterceptors = new ArrayList<>(outInterceptors);
    }

    /**
     * Creates a JAX-WS proxy for the given service interface and endpoint address.
     * Timeouts and correlation ID propagation are applied automatically.
     *
     * @param serviceInterface JAX-WS service interface (annotated with {@code @WebService})
     * @param address          SOAP endpoint URL
     * @param <T>              service type
     * @return configured proxy instance
     */
    public <T> T create(Class<T> serviceInterface, String address) {
        JaxWsProxyFactoryBean factory = buildFactory(serviceInterface, address);
        T proxy = factory.create(serviceInterface);
        configureTimeouts(proxy);
        return proxy;
    }

    /**
     * Creates a proxy configured with HTTP Basic authentication.
     *
     * @param serviceInterface JAX-WS service interface
     * @param address          SOAP endpoint URL
     * @param username         Basic auth username
     * @param password         Basic auth password
     * @param <T>              service type
     * @return configured proxy instance
     */
    public <T> T createWithBasicAuth(Class<T> serviceInterface, String address,
                                      String username, String password) {
        T proxy = create(serviceInterface, address);
        BindingProvider provider = (BindingProvider) proxy;
        provider.getRequestContext().put(BindingProvider.USERNAME_PROPERTY, username);
        provider.getRequestContext().put(BindingProvider.PASSWORD_PROPERTY, password);
        return proxy;
    }

    /**
     * Creates a proxy with Bearer token authentication.
     * The token supplier is invoked on every request, supporting dynamic token refresh.
     *
     * @param serviceInterface JAX-WS service interface
     * @param address          SOAP endpoint URL
     * @param tokenSupplier    supplier returning the current Bearer token (without "Bearer " prefix)
     * @param <T>              service type
     * @return configured proxy instance
     */
    public <T> T createWithBearerToken(Class<T> serviceInterface, String address,
                                        Supplier<String> tokenSupplier) {
        T proxy = create(serviceInterface, address);
        ClientProxy.getClient(proxy)
                .getOutInterceptors()
                .add(new BearerTokenSoapInterceptor(tokenSupplier));
        return proxy;
    }

    private <T> JaxWsProxyFactoryBean buildFactory(Class<T> serviceInterface, String address) {
        JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();
        factory.setServiceClass(serviceInterface);
        factory.setAddress(address);
        outInterceptors.forEach(factory.getOutInterceptors()::add);
        return factory;
    }

    private void configureTimeouts(Object proxy) {
        HTTPConduit conduit = (HTTPConduit) ClientProxy.getClient(proxy).getConduit();
        HTTPClientPolicy policy = new HTTPClientPolicy();
        policy.setConnectionTimeout(properties.connectTimeout().toMillis());
        policy.setReceiveTimeout(properties.receiveTimeout().toMillis());
        conduit.setClient(policy);
    }
}
