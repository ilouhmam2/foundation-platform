package fr.francetv.foundation.api.autoconfigure;

import fr.francetv.foundation.api.error.GlobalExceptionHandler;
import fr.francetv.foundation.api.error.ValidationExceptionHandler;
import fr.francetv.foundation.api.properties.ApiProperties;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.DispatcherServlet;

/**
 * Auto-configuration for foundation API conventions.
 *
 * <p>Activates only when:
 * <ul>
 *   <li>{@code spring-webmvc} is on the classpath ({@link DispatcherServlet})</li>
 *   <li>The application is a SERVLET web application</li>
 * </ul>
 *
 * <p>Consuming services can override {@link GlobalExceptionHandler} by declaring
 * their own {@code @RestControllerAdvice} bean.
 */
@AutoConfiguration
@EnableConfigurationProperties(ApiProperties.class)
@ConditionalOnClass(DispatcherServlet.class)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class ApiAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public GlobalExceptionHandler globalExceptionHandler(ApiProperties properties) {
        return new GlobalExceptionHandler(properties);
    }

    /**
     * Inner configuration activated only when Bean Validation (jakarta.validation) is present.
     * Using an inner class avoids a {@link java.lang.TypeNotPresentException} when
     * {@code @ConditionalOnClass} is used on a {@code @Bean} method whose parameter
     * references an absent class.
     */
    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(name = "jakarta.validation.ConstraintViolationException")
    static class ValidationConfiguration {

        @Bean
        @ConditionalOnMissingBean(ValidationExceptionHandler.class)
        public ValidationExceptionHandler validationExceptionHandler() {
            return new ValidationExceptionHandler();
        }
    }
}
