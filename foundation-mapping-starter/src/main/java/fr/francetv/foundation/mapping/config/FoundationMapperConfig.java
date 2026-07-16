package fr.francetv.foundation.mapping.config;

import org.mapstruct.MapperConfig;
import org.mapstruct.MappingConstants;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

/**
 * Shared MapStruct configuration for all foundation-based services.
 *
 * <p>Consuming services reference this config in their mapper interfaces:
 * <pre>{@code
 * @Mapper(config = FoundationMapperConfig.class)
 * public interface PersonMapper { ... }
 * }</pre>
 *
 * <p>Conventions applied:
 * <ul>
 *   <li>Spring component model — mappers are Spring beans injected via {@code @Autowired}</li>
 *   <li>Null properties are ignored during mapping — existing target values are preserved</li>
 *   <li>Unmapped target properties produce a compile-time warning</li>
 * </ul>
 */
@MapperConfig(
        componentModel = MappingConstants.ComponentModel.SPRING,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.WARN
)
public interface FoundationMapperConfig {
}
