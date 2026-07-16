# foundation-mapping-starter

Provides shared MapStruct configuration conventions for `foundation-platform` services.

## What it does

- Makes `FoundationMapperConfig` available — a shared `@MapperConfig` interface encoding standard MapStruct conventions
- Auto-configuration activates automatically when MapStruct is on the classpath
- Contributes no runtime beans; mapping is entirely compile-time

## Conventions applied by `FoundationMapperConfig`

| Convention | Value | Effect |
|---|---|---|
| `componentModel` | `spring` | Mappers are Spring beans, injected via `@Autowired` |
| `nullValuePropertyMappingStrategy` | `IGNORE` | Null source properties do not overwrite target values |
| `unmappedTargetPolicy` | `WARN` | Unmapped target properties produce a compile-time warning |

## Usage

### 1. Declare dependencies

```xml
<dependency>
    <groupId>fr.francetv.foundation</groupId>
    <artifactId>foundation-mapping-starter</artifactId>
</dependency>
<dependency>
    <groupId>org.mapstruct</groupId>
    <artifactId>mapstruct</artifactId>
</dependency>
```

### 2. Configure the annotation processor

Add `mapstruct-processor` to the `annotationProcessorPaths` of `maven-compiler-plugin` in your service `pom.xml`.
If your service also uses Lombok, both processors must be listed together with the binding helper that enforces the correct ordering:

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-compiler-plugin</artifactId>
    <configuration>
        <annotationProcessorPaths>
            <path>
                <groupId>org.projectlombok</groupId>
                <artifactId>lombok</artifactId>
                <version>${lombok.version}</version>
            </path>
            <path>
                <groupId>org.projectlombok</groupId>
                <artifactId>lombok-mapstruct-binding</artifactId>
                <version>0.2.0</version>
            </path>
            <path>
                <groupId>org.mapstruct</groupId>
                <artifactId>mapstruct-processor</artifactId>
                <version>${mapstruct.version}</version>
            </path>
        </annotationProcessorPaths>
    </configuration>
</plugin>
```

> The `mapstruct-processor` version is managed by `foundation-bom`.
> Use `${mapstruct.version}` if your service imports the BOM, or omit the `<version>` tag when the BOM is active.

### 3. Reference `FoundationMapperConfig` in your mapper

```java
import fr.francetv.foundation.mapping.config.FoundationMapperConfig;
import org.mapstruct.Mapper;

@Mapper(config = FoundationMapperConfig.class)
public interface PersonMapper {

    PersonDto toDto(Person person);

    Person toEntity(PersonDto dto);
}
```

The mapper will be available as a Spring bean:

```java
@Service
public class PersonService {

    private final PersonMapper personMapper;

    public PersonService(PersonMapper personMapper) {
        this.personMapper = personMapper;
    }
}
```

### 4. Override conventions (optional)

Override individual settings directly on your `@Mapper` annotation:

```java
@Mapper(
    config = FoundationMapperConfig.class,
    unmappedTargetPolicy = ReportingPolicy.ERROR  // stricter than the default WARN
)
public interface StrictMapper { ... }
```

## Notes

- MapStruct is a compile-time annotation processor. This starter provides no runtime beans.
- `FoundationMapperConfig` is intentionally an empty interface — it only carries the `@MapperConfig` annotation.
- Services must declare the `mapstruct` dependency explicitly; it is not transitive from this starter.
