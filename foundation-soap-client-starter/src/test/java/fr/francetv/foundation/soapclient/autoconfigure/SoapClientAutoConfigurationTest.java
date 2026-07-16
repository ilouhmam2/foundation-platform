package fr.francetv.foundation.soapclient.autoconfigure;

import fr.francetv.foundation.soapclient.factory.SoapClientFactory;
import fr.francetv.foundation.soapclient.interceptor.CorrelationIdSoapInterceptor;
import fr.francetv.foundation.soapclient.properties.SoapClientProperties;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SoapClientAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(SoapClientAutoConfiguration.class));

    @Test
    void shouldNotCreateBeanWhenCxfAbsent() {
        new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(SoapClientAutoConfiguration.class))
                .withClassLoader(new FilteredClassLoader(JaxWsProxyFactoryBean.class))
                .run(ctx -> {
                    assertThat(ctx).hasNotFailed();
                    assertThat(ctx).doesNotHaveBean(CorrelationIdSoapInterceptor.class);
                    assertThat(ctx).doesNotHaveBean(SoapClientFactory.class);
                });
    }

    @Test
    void shouldCreateTimeoutInterceptorWhenCxfPresent() {
        contextRunner.run(ctx -> {
            assertThat(ctx).hasSingleBean(CorrelationIdSoapInterceptor.class);
            assertThat(ctx).hasSingleBean(SoapClientFactory.class);
        });
    }

    @Test
    void shouldRespectCustomCorrelationIdInterceptor() {
        contextRunner
                .withBean(CorrelationIdSoapInterceptor.class, CorrelationIdSoapInterceptor::new)
                .run(ctx -> assertThat(ctx).hasSingleBean(CorrelationIdSoapInterceptor.class));
    }

    @Test
    void shouldRespectCustomSoapClientFactory() {
        SoapClientProperties props =
                new SoapClientProperties(Duration.ofSeconds(5), Duration.ofSeconds(30));
        contextRunner
                .withBean(SoapClientFactory.class, () -> new SoapClientFactory(props, List.of()))
                .run(ctx -> assertThat(ctx).hasSingleBean(SoapClientFactory.class));
    }

    @Test
    void shouldBindConnectTimeoutProperty() {
        contextRunner
                .withPropertyValues("foundation.soap-client.connect-timeout=2s")
                .run(ctx -> assertThat(ctx).hasNotFailed());
    }

    @Test
    void shouldBindReceiveTimeoutProperty() {
        contextRunner
                .withPropertyValues("foundation.soap-client.receive-timeout=10s")
                .run(ctx -> assertThat(ctx).hasNotFailed());
    }

    @Test
    void shouldApplyDefaultTimeouts() {
        contextRunner.run(ctx -> {
            assertThat(ctx).hasNotFailed();
            SoapClientProperties props = ctx.getBean(SoapClientProperties.class);
            assertThat(props.connectTimeout()).isEqualTo(Duration.ofSeconds(5));
            assertThat(props.receiveTimeout()).isEqualTo(Duration.ofSeconds(30));
        });
    }
}
