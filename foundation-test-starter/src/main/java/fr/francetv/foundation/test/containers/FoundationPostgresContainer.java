package fr.francetv.foundation.test.containers;

import org.testcontainers.containers.PostgreSQLContainer;

/**
 * Pre-configured singleton PostgreSQL container for integration tests.
 *
 * <p>Uses the singleton pattern so the container is started once per JVM and shared across
 * all test classes, reducing total test suite startup time.
 *
 * <p>Typical usage in a {@code @SpringBootTest} integration test:
 *
 * <pre>{@code
 * @SpringBootTest
 * class MyRepositoryTest {
 *
 *     @BeforeAll
 *     static void startContainer() {
 *         FoundationPostgresContainer.getInstance().start();
 *     }
 * }
 * }</pre>
 *
 * <p>Or, using a {@code @TestConfiguration} class shared across test classes:
 *
 * <pre>{@code
 * @TestConfiguration
 * public class PostgresTestConfig {
 *
 *     static {
 *         FoundationPostgresContainer.getInstance().start();
 *     }
 * }
 * }</pre>
 *
 * <p>On {@link #start()}, this container automatically sets the following system properties
 * so Spring Boot picks them up without additional configuration:
 * <ul>
 *   <li>{@code spring.datasource.url}</li>
 *   <li>{@code spring.datasource.username}</li>
 *   <li>{@code spring.datasource.password}</li>
 * </ul>
 */
public final class FoundationPostgresContainer extends PostgreSQLContainer<FoundationPostgresContainer> {

    private static final String DEFAULT_IMAGE = "postgres:16-alpine";

    private static volatile FoundationPostgresContainer instance;

    private FoundationPostgresContainer() {
        super(DEFAULT_IMAGE);
        withDatabaseName("testdb");
        withUsername("test");
        withPassword("test");
    }

    /**
     * Returns the shared singleton instance of the PostgreSQL container.
     * The container is not started until {@link #start()} is called.
     */
    public static FoundationPostgresContainer getInstance() {
        if (instance == null) {
            synchronized (FoundationPostgresContainer.class) {
                if (instance == null) {
                    instance = new FoundationPostgresContainer();
                }
            }
        }
        return instance;
    }

    @Override
    public void start() {
        super.start();
        System.setProperty("spring.datasource.url", getJdbcUrl());
        System.setProperty("spring.datasource.username", getUsername());
        System.setProperty("spring.datasource.password", getPassword());
    }

    @Override
    public void stop() {
        // Intentionally not stopped — container lifecycle is managed by the JVM shutdown hook
        // (Testcontainers' Ryuk or explicit shutdown hook registered by the framework).
    }
}
