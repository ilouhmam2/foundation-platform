package fr.francetv.foundation.mapping.autoconfigure;

import org.mapstruct.Mapper;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;

/**
 * Auto-configuration for MapStruct mapping conventions.
 *
 * <p>Activates when MapStruct is on the classpath. This configuration contributes
 * no runtime beans — its purpose is to make {@link fr.francetv.foundation.mapping.config.FoundationMapperConfig}
 * available as a shared MapStruct configuration class that consuming services reference
 * in their {@code @Mapper(config = FoundationMapperConfig.class)} declarations.
 *
 * <p>Consuming services must declare the following dependency to use MapStruct:
 * <pre>{@code
 * <dependency>
 *     <groupId>org.mapstruct</groupId>
 *     <artifactId>mapstruct</artifactId>
 * </dependency>
 * }</pre>
 */
@AutoConfiguration
@ConditionalOnClass(Mapper.class)
public class MappingAutoConfiguration {
}
