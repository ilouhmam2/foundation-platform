package ${package};

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
#if($capabilities.contains("nats"))
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
#end

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
#if($capabilities.contains("nats"))
@Testcontainers
#end
class ${serviceName}ApplicationTests {

#if($capabilities.contains("nats"))
    @Container
    static GenericContainer<?> natsContainer =
            new GenericContainer<>(DockerImageName.parse("nats:2-alpine"))
                    .withExposedPorts(4222);

    @DynamicPropertySource
    static void natsProperties(DynamicPropertyRegistry registry) {
        registry.add("foundation.nats.server-url",
                () -> "nats://" + natsContainer.getHost() + ":" + natsContainer.getMappedPort(4222));
    }

#end
    @Test
    void contextLoads() {
    }
}
