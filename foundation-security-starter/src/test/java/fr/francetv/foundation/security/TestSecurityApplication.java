package fr.francetv.foundation.security;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Minimal Spring Boot application used by integration tests only.
 * Not part of the starter's production code.
 */
@SpringBootApplication
public class TestSecurityApplication {

    public static void main(String[] args) {
        SpringApplication.run(TestSecurityApplication.class, args);
    }

    @RestController
    @RequestMapping("/api")
    static class TestController {

        @GetMapping("/test")
        public String test() {
            return "ok";
        }
    }
}
