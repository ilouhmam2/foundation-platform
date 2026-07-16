package fr.francetv.foundation.soapclient.autoconfigure;

import fr.francetv.foundation.soapclient.factory.SoapClientFactory;
import fr.francetv.foundation.soapclient.interceptor.CorrelationIdSoapInterceptor;
import fr.francetv.foundation.soapclient.properties.SoapClientProperties;
import org.apache.cxf.interceptor.Interceptor;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.apache.cxf.message.Message;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import java.util.ArrayList;
import java.util.List;

/**
 * Auto-configuration for SOAP client conventions based on Apache CXF.
 *
 * <p>Activates when {@link JaxWsProxyFactoryBean} is on the classpath
 * (i.e. when {@code cxf-rt-frontend-jaxws} is declared as a dependency).
 *
 * <p>Provides:
 * <ul>
 *   <li>A {@link CorrelationIdSoapInterceptor} that propagates {@code X-Correlation-Id}
 *       on every outgoing SOAP request</li>
 *   <li>A {@link SoapClientFactory} pre-configured with timeouts from
 *       {@code foundation.soap-client.*} properties and all registered outbound interceptors</li>
 * </ul>
 */
@AutoConfiguration
@ConditionalOnClass(JaxWsProxyFactoryBean.class)
@EnableConfigurationProperties(SoapClientProperties.class)
public class SoapClientAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public CorrelationIdSoapInterceptor correlationIdSoapInterceptor() {
        return new CorrelationIdSoapInterceptor();
    }

    @Bean
    @ConditionalOnMissingBean
    public SoapClientFactory soapClientFactory(
            SoapClientProperties properties,
            ObjectProvider<CorrelationIdSoapInterceptor> correlationIdInterceptor) {
        List<Interceptor<? extends Message>> interceptors = new ArrayList<>();
        correlationIdInterceptor.ifAvailable(interceptors::add);
        return new SoapClientFactory(properties, interceptors);
    }
}
