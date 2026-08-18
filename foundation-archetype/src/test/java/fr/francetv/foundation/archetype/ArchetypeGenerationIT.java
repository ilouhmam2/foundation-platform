package fr.francetv.foundation.archetype;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Integration tests for foundation-archetype generation.
 *
 * Prerequisites: the archetype must be installed in the local Maven repository.
 * Run {@code mvn install -DskipTests} on the full platform before running these tests in isolation.
 */
class ArchetypeGenerationIT {

    private static final String ARCHETYPE_GROUP_ID = "fr.francetv.foundation";
    private static final String ARCHETYPE_ARTIFACT_ID = "foundation-archetype";
    private static final String ARCHETYPE_VERSION =
            System.getProperty("project.version", "0.0.1-SNAPSHOT");

    private static final String TEST_GROUP_ID = "fr.francetv.test";
    private static final String TEST_VERSION = "1.0.0-SNAPSHOT";

    @TempDir
    Path tempDir;

    // -------------------------------------------------------------------------
    // Mandatory-only generation
    // -------------------------------------------------------------------------

    @Test
    void shouldGenerateWithMandatoryCapabilitiesOnly() throws Exception {
        Path projectDir = generate("mandatory-only", "TestService", "test-service", "");

        assertMandatoryStructure(projectDir, "test-service", "fr/francetv/test");
        assertPomContainsAllMandatoryStarters(projectDir);
        assertThat(projectContent(projectDir, "pom.xml")).doesNotContain("foundation-security-starter");
        assertThat(projectContent(projectDir, "pom.xml")).doesNotContain("foundation-data-starter");
        assertThat(projectContent(projectDir, "pom.xml")).doesNotContain("foundation-nats-starter");
        assertThat(projectContent(projectDir, "pom.xml")).doesNotContain("foundation-http-client-starter");
        assertThat(projectContent(projectDir, "pom.xml")).doesNotContain("foundation-soap-client-starter");
        assertThat(projectDir.resolve("Dockerfile")).exists();
        assertThat(projectDir.resolve(".gitlab-ci.yml")).exists();
        assertThat(projectContent(projectDir, "src/main/resources/application.yml")).doesNotContain("datasource:");
        assertThat(projectContent(projectDir, "src/main/resources/application.yml")).doesNotContain("nats:");
        assertThat(projectContent(projectDir, "src/main/resources/application.yml")).doesNotContain("oauth2:");
        assertThat(projectContent(projectDir, "src/test/resources/application.yml")).doesNotContain("oauth2:");
    }

    // -------------------------------------------------------------------------
    // Security capability
    // -------------------------------------------------------------------------

    @Test
    void shouldIncludeSecurityStarterWhenCapabilityRequested() throws Exception {
        Path projectDir = generate("with-security", "TestService", "test-service", "security");

        assertThat(projectContent(projectDir, "pom.xml")).contains("foundation-security-starter");
        assertThat(projectContent(projectDir, "src/main/resources/application.yml")).contains("oauth2:");
        assertThat(projectContent(projectDir, "src/main/resources/application.yml")).contains("issuer-uri:");
        assertThat(projectContent(projectDir, "src/test/resources/application.yml")).contains("oauth2:");
        // Mandatory starters must still be present
        assertPomContainsAllMandatoryStarters(projectDir);
    }

    // -------------------------------------------------------------------------
    // Data capability
    // -------------------------------------------------------------------------

    @Test
    void shouldGenerateWithDataCapability() throws Exception {
        Path projectDir = generate("with-data", "TestService", "test-service", "data");

        assertThat(projectContent(projectDir, "pom.xml")).contains("foundation-data-starter");
        assertThat(projectContent(projectDir, "src/main/resources/application.yml")).contains("datasource:");
        assertThat(projectContent(projectDir, "src/main/resources/application.yml")).contains("flyway:");
        assertPackageDir(projectDir, "fr/francetv/test/infrastructure/adapter/out/persistence").exists();
        assertPackageDir(projectDir, "fr/francetv/test/infrastructure/adapter/out/rest").doesNotExist();
    }

    // -------------------------------------------------------------------------
    // All capabilities
    // -------------------------------------------------------------------------

    @Test
    void shouldGenerateWithAllCapabilities() throws Exception {
        Path projectDir = generate("with-all", "TestService", "test-service", "data,nats,http-client,soap-client");

        assertThat(projectContent(projectDir, "pom.xml")).contains("foundation-data-starter");
        assertThat(projectContent(projectDir, "pom.xml")).contains("foundation-nats-starter");
        assertThat(projectContent(projectDir, "pom.xml")).contains("foundation-http-client-starter");
        assertThat(projectContent(projectDir, "pom.xml")).contains("foundation-soap-client-starter");
        assertPackageDir(projectDir, "fr/francetv/test/infrastructure/adapter/in/messaging").exists();
        assertPackageDir(projectDir, "fr/francetv/test/infrastructure/adapter/out/persistence").exists();
        assertPackageDir(projectDir, "fr/francetv/test/infrastructure/adapter/out/rest").exists();
        assertPackageDir(projectDir, "fr/francetv/test/infrastructure/adapter/out/soap").exists();
    }

    // -------------------------------------------------------------------------
    // Unselected capabilities absent
    // -------------------------------------------------------------------------

    @Test
    void shouldNotIncludeUnselectedCapabilities() throws Exception {
        Path projectDir = generate("nats-only", "TestService", "test-service", "nats");

        assertThat(projectContent(projectDir, "pom.xml")).contains("foundation-nats-starter");
        assertThat(projectContent(projectDir, "pom.xml")).doesNotContain("foundation-data-starter");
        assertPackageDir(projectDir, "fr/francetv/test/infrastructure/adapter/in/messaging").exists();
        assertPackageDir(projectDir, "fr/francetv/test/infrastructure/adapter/out/persistence").doesNotExist();
        assertThat(projectContent(projectDir, "src/main/resources/application.yml")).doesNotContain("datasource:");
        assertThat(projectContent(projectDir, "src/main/resources/application.yml")).contains("nats:");
    }

    // -------------------------------------------------------------------------
    // Dockerfile flag
    // -------------------------------------------------------------------------

    @Test
    void shouldGenerateDockerfileByDefault() throws Exception {
        Path projectDir = generate("default-dockerfile", "TestService", "test-service", "");

        assertThat(projectDir.resolve("Dockerfile")).exists();
        assertThat(projectContent(projectDir, "Dockerfile")).contains("eclipse-temurin:21-jre-alpine");
        assertThat(projectContent(projectDir, "Dockerfile")).contains("test-service-");
    }

    @Test
    void shouldSkipDockerfileWhenDisabled() throws Exception {
        Path projectDir = generateWithFlags("no-dockerfile", "TestService", "test-service", "", "false", "true");

        assertThat(projectDir.resolve("Dockerfile")).doesNotExist();
    }

    // -------------------------------------------------------------------------
    // GitLab CI flag
    // -------------------------------------------------------------------------

    @Test
    void shouldGenerateGitlabCiByDefault() throws Exception {
        Path projectDir = generate("default-gitlabci", "TestService", "test-service", "");

        assertThat(projectDir.resolve(".gitlab-ci.yml")).exists();
        assertThat(projectContent(projectDir, ".gitlab-ci.yml")).contains("stages:");
        assertThat(projectContent(projectDir, ".gitlab-ci.yml")).contains("main");
    }

    @Test
    void shouldSkipGitlabCiWhenDisabled() throws Exception {
        Path projectDir = generateWithFlags("no-gitlabci", "TestService", "test-service", "", "true", "false");

        assertThat(projectDir.resolve(".gitlab-ci.yml")).doesNotExist();
    }

    // -------------------------------------------------------------------------
    // Service name as Java class prefix
    // -------------------------------------------------------------------------

    @Test
    void shouldUseServiceNameAsJavaClassPrefix() throws Exception {
        Path projectDir = generate("invoice-svc", "InvoiceService", "invoice-service", "");

        Path appClass = projectDir.resolve("src/main/java/fr/francetv/test/InvoiceServiceApplication.java");
        assertThat(appClass).exists();
        assertThat(Files.readString(appClass)).contains("class InvoiceServiceApplication");
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    private Path generate(String testName, String serviceName, String artifactId, String capabilities)
            throws Exception {
        return generateWithFlags(testName, serviceName, artifactId, capabilities, "true", "true");
    }

    private Path generateWithFlags(String testName,
                                    String serviceName,
                                    String artifactId,
                                    String capabilities,
                                    String generateDockerfile,
                                    String generateGitlabCi) throws Exception {

        Path workDir = tempDir.resolve(testName);
        Files.createDirectories(workDir);

        String mvn = resolveMvnExecutable();
        assumeTrue(new File(mvn).exists() || isMvnOnPath(mvn),
                "Maven executable not found, skipping archetype IT");

        List<String> cmd = new ArrayList<>();
        cmd.add(mvn);
        cmd.add("archetype:generate");
        cmd.add("-DinteractiveMode=false");
        cmd.add("-DarchetypeCatalog=local");
        cmd.add("-DarchetypeGroupId=" + ARCHETYPE_GROUP_ID);
        cmd.add("-DarchetypeArtifactId=" + ARCHETYPE_ARTIFACT_ID);
        cmd.add("-DarchetypeVersion=" + ARCHETYPE_VERSION);
        cmd.add("-DgroupId=" + TEST_GROUP_ID);
        cmd.add("-DartifactId=" + artifactId);
        cmd.add("-Dversion=" + TEST_VERSION);
        cmd.add("-Dpackage=" + TEST_GROUP_ID);
        cmd.add("-DserviceName=" + serviceName);
        cmd.add("-Dcapabilities=" + capabilities);
        cmd.add("-DgenerateDockerfile=" + generateDockerfile);
        cmd.add("-DgenerateGitlabCi=" + generateGitlabCi);

        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.directory(workDir.toFile());
        pb.redirectErrorStream(true);

        Process process = pb.start();
        String output = new String(process.getInputStream().readAllBytes());
        int exitCode = process.waitFor();

        assertThat(exitCode)
                .as("archetype:generate failed for test [%s].\nOutput:\n%s", testName, output)
                .isZero();

        return workDir.resolve(artifactId);
    }

    private void assertMandatoryStructure(Path projectDir, String artifactId, String packagePath) {
        assertThat(projectDir.resolve("pom.xml")).exists();
        assertThat(projectDir.resolve("src/main/resources/application.yml")).exists();
        assertThat(projectDir.resolve("src/main/java/" + packagePath + "/infrastructure/adapter/in/web")).exists();
        assertThat(projectDir.resolve("src/main/java/" + packagePath + "/infrastructure/config")).exists();
        assertThat(projectDir.resolve("src/main/java/" + packagePath + "/domain/model")).exists();
        assertThat(projectDir.resolve("src/main/java/" + packagePath + "/application/usecase")).exists();
    }

    private void assertPomContainsAllMandatoryStarters(Path projectDir) throws IOException {
        String pom = projectContent(projectDir, "pom.xml");
        assertThat(pom).contains("foundation-core-starter");
        assertThat(pom).contains("foundation-api-starter");
        assertThat(pom).contains("foundation-logging-starter");
        assertThat(pom).contains("foundation-observability-starter");
        assertThat(pom).contains("foundation-mapping-starter");
        assertThat(pom).contains("foundation-test-starter");
    }

    private String projectContent(Path projectDir, String relativePath) throws IOException {
        return Files.readString(projectDir.resolve(relativePath));
    }

    private org.assertj.core.api.AbstractPathAssert<?> assertPackageDir(Path projectDir, String relPath) {
        return assertThat(projectDir.resolve("src/main/java/" + relPath));
    }

    private String resolveMvnExecutable() {
        String mavenHome = System.getenv("MAVEN_HOME");
        if (mavenHome == null) {
            mavenHome = System.getenv("M2_HOME");
        }
        boolean isWindows = System.getProperty("os.name", "").toLowerCase().contains("windows");
        String mvnBin = isWindows ? "mvn.cmd" : "mvn";
        if (mavenHome != null) {
            return mavenHome + File.separator + "bin" + File.separator + mvnBin;
        }
        return mvnBin;
    }

    private boolean isMvnOnPath(String mvnExecutable) {
        try {
            new ProcessBuilder(mvnExecutable, "--version")
                    .redirectErrorStream(true)
                    .start()
                    .waitFor();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
