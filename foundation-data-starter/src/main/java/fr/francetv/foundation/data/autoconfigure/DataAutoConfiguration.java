package fr.francetv.foundation.data.autoconfigure;

import fr.francetv.foundation.data.audit.AuditConfiguration;
import fr.francetv.foundation.data.properties.DataProperties;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.hibernate.autoconfigure.HibernateJpaAutoConfiguration;
import org.springframework.context.annotation.Import;

import javax.sql.DataSource;

@AutoConfiguration(after = HibernateJpaAutoConfiguration.class)
@ConditionalOnClass(DataSource.class)
@EnableConfigurationProperties(DataProperties.class)
@Import(AuditConfiguration.class)
public class DataAutoConfiguration {
    // Default conventions (ddl-auto=validate, open-in-view=false) are applied
    // via DataDefaultsEnvironmentPostProcessor at lowest property priority.
}
