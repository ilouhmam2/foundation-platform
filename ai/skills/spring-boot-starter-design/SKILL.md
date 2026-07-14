# Skill - Spring Boot Starter Design

## Purpose

Use this skill when creating or reviewing a `foundation-platform` Spring Boot starter.

The goal is to design starters that feel natural to Spring Boot developers and avoid unnecessary framework abstractions.

## When to use

Use this skill for tasks involving:

- any `foundation-*-starter` module
- auto-configuration design
- configuration properties design
- conditional beans
- starter dependency structure

---

## Core design principle

A starter provides one technical capability.

A service uses a capability by declaring the starter dependency.

Properties configure the capability. Properties do not activate it.

---

## Starter responsibilities

A starter may provide:

- dependencies
- auto-configuration
- default beans
- configuration properties
- extension points
- documentation
- tests

A starter must not provide:

- business logic
- service-specific clients
- service-specific entities
- service-specific controllers
- deployment artifacts

---

## Auto-configuration template

```java
@AutoConfiguration
@EnableConfigurationProperties(MyProperties.class)
@ConditionalOnClass(SomeClass.class)
public class MyAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public MyService myService(MyProperties properties) {
        return new MyService(properties);
    }
}
```

Register in:
```
src/main/resources/META-INF/spring/
  org.springframework.boot.autoconfigure.AutoConfiguration.imports
```

---

## Configuration properties template

```java
@ConfigurationProperties(prefix = "foundation.{capability}")
public record MyProperties(
        String someValue,
        Duration timeout) {
}
```

---

## Test template

Use `ApplicationContextRunner` for auto-configuration tests:

```java
private final ApplicationContextRunner contextRunner =
    new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(MyAutoConfiguration.class));

@Test
void shouldCreateDefaultBean() {
    contextRunner.run(ctx -> assertThat(ctx).hasSingleBean(MyService.class));
}

@Test
void shouldRespectBeanOverride() {
    contextRunner
        .withUserConfiguration(CustomConfig.class)
        .run(ctx -> assertThat(ctx).hasSingleBean(MyService.class)
            .getBean(MyService.class).isInstanceOf(CustomMyService.class));
}
```

---

## Checklist

- [ ] Auto-configuration is conditional (`@ConditionalOnClass`, `@ConditionalOnMissingBean`).
- [ ] Beans can be overridden by consumers.
- [ ] Configuration properties are typed and only added when needed.
- [ ] `AutoConfiguration.imports` is present.
- [ ] Tests cover default bean, override, and property binding.
- [ ] README documents all properties and a usage example.
- [ ] No business logic in the starter.
- [ ] No optional runtime deps forced on all services.