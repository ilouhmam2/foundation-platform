import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

// Resolve generated project directory
def projectDir = new File(request.outputDirectory, request.artifactId)

// Retrieve user-defined properties
// capabilities defaults to "none" (the non-empty Maven sentinel for "no optional capabilities")
def rawCapabilities = request.properties.getProperty("capabilities") ?: "none"
def capabilities = rawCapabilities == "none" ? "" : rawCapabilities
def generateDockerfile = request.properties.getProperty("generateDockerfile") ?: "true"
def generateGitlabCi = request.properties.getProperty("generateGitlabCi") ?: "true"

// Derive package path from the package property (defaults to groupId)
def pkg = request.properties.getProperty("package") ?: request.groupId
def packagePath = pkg.replace('.', File.separator)
def javaSourceDir = new File(projectDir, "src${File.separator}main${File.separator}java${File.separator}${packagePath}")

// Helper: delete a directory and its contents recursively
def deleteDir = { File dir ->
    if (dir.exists()) {
        dir.deleteDir()
    }
}

// Helper: delete a parent directory if it is now empty
def deleteIfEmpty = { File dir ->
    if (dir.exists() && dir.isDirectory() && dir.list().length == 0) {
        dir.delete()
    }
}

// -- Optional infrastructure directories --

if (!capabilities.contains("nats")) {
    deleteDir(new File(javaSourceDir, "infrastructure${File.separator}adapter${File.separator}in${File.separator}messaging"))
}

if (!capabilities.contains("data")) {
    deleteDir(new File(javaSourceDir, "infrastructure${File.separator}adapter${File.separator}out${File.separator}persistence"))
}

if (!capabilities.contains("http-client")) {
    deleteDir(new File(javaSourceDir, "infrastructure${File.separator}adapter${File.separator}out${File.separator}rest"))
}

if (!capabilities.contains("soap-client")) {
    deleteDir(new File(javaSourceDir, "infrastructure${File.separator}adapter${File.separator}out${File.separator}soap"))
}

// Clean up empty adapter/out directory when no optional out-adapters were kept
def adapterOutDir = new File(javaSourceDir, "infrastructure${File.separator}adapter${File.separator}out")
deleteIfEmpty(adapterOutDir)

// -- Optional top-level files --

if (generateDockerfile != "true") {
    new File(projectDir, "Dockerfile").delete()
}

if (generateGitlabCi != "true") {
    new File(projectDir, ".gitlab-ci.yml").delete()
}
