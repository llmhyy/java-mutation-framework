package jmutation.model.project;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MavenProjectTest {
    @Test
    void setupDependenciesCommand_MavenProject_GetsCorrectCommand() {
        MavenProject mavenProject = new MavenProject(null, null,
                null, null, null, null, null);
        String command = mavenProject.setupDependenciesCommand();
        String expectedCommand = "mvn dependency:copy-dependencies";
        assertEquals(expectedCommand, command);
    }
}
