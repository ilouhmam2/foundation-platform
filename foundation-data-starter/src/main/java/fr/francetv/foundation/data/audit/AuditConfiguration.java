package fr.francetv.foundation.data.audit;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(AuditingEntityListener.class)
@ConditionalOnBean(EntityManagerFactory.class)
@ConditionalOnMissingBean(name = "jpaAuditingHandler")
@EnableJpaAuditing
public class AuditConfiguration {
}
