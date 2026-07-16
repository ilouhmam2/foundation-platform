package fr.francetv.foundation.data.autoconfigure;

import fr.francetv.foundation.data.TestDataApplication;
import fr.francetv.foundation.data.TestItem;
import fr.francetv.foundation.data.TestItemRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = TestDataApplication.class)
@Testcontainers(disabledWithoutDocker = true)
class DataAutoConfigurationIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void configure(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private Environment environment;

    @Autowired
    private TestItemRepository testItemRepository;

    @Test
    void flywayMigrationShouldRunAtStartup() {
        // If the context loaded successfully, Flyway ran and Hibernate validated the schema.
        assertThat(testItemRepository.findAll()).isEmpty();
    }

    @Test
    void ddlAutoShouldDefaultToValidate() {
        assertThat(environment.getProperty("spring.jpa.hibernate.ddl-auto")).isEqualTo("validate");
    }

    @Test
    void openInViewShouldBeDisabled() {
        assertThat(environment.getProperty("spring.jpa.open-in-view")).isEqualTo("false");
    }

    @Test
    @Transactional
    void auditFieldsShouldBePopulatedOnSave() {
        TestItem saved = testItemRepository.save(new TestItem("audit-test"));

        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
    }
}
