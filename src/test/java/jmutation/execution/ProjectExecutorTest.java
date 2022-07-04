package jmutation.execution;

import jmutation.model.MicrobatConfig;
import jmutation.model.ProjectConfig;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class ProjectExecutorTest {

    @Test
    void findJars() {
        String projectPath = "./sample/math_70";
        String dropInsDir = "./lib";
        ProjectConfig config = new ProjectConfig(projectPath, dropInsDir);

        ProjectExecutor pe = new ProjectExecutor(MicrobatConfig.defaultConfig(projectPath), config);
        List<String> jarNames = pe.findJars().stream().map(file -> file.getName()).collect(Collectors.toList());
        List<String> expectedJars = List.of("commons-math-2.2-SNAPSHOT.jar");
        assertIterableEquals(expectedJars, jarNames);

        projectPath = "./sample/spoon";
        dropInsDir = "./lib";
        config = new ProjectConfig(projectPath, dropInsDir);
        pe = new ProjectExecutor(MicrobatConfig.defaultConfig(projectPath), config);
        jarNames = pe.findJars().stream().map(file -> file.getName()).collect(Collectors.toList());
        expectedJars = List.of("slf4j-api-1.7.32.jar", "checker-qual-3.12.0.jar", "commons-io-2.11.0.jar",
                "plexus-utils-3.3.0.jar", "jsap-2.1.jar", "lidalia-lang-1.0.0.jar", "org.eclipse.jdt.core-3.27.0.jar",
                "junit-4.13.2.jar", "logback-classic-1.2.7.jar", "lidalia-slf4j-ext-1.0.0.jar", "maven-model-3.8.5.jar",
                "failureaccess-1.0.1.jar", "jdtCompilerAdapter.jar", "error_prone_annotations-2.7.1.jar",
                "jackson-core-2.13.0.jar", "commons-compress-1.21.jar", "logback-core-1.2.7.jar",
                "listenablefuture-9999.0-empty-to-avoid-conflict-with-guava.jar", "maven-shared-utils-3.3.3.jar",
                "validation-api-2.0.1.Final.jar", "joda-time-2.2.jar", "hamcrest-core-1.3.jar",
                "jackson-databind-2.13.0.jar", "jackson-annotations-2.13.0.jar",
                "spoon-core-10.0.1-SNAPSHOT-jar-with-dependencies.jar", "j2objc-annotations-1.3.jar",
                "maven-invoker-3.1.0.jar", "commons-lang3-3.12.0.jar", "slf4j-test-1.2.0.jar", "xz-1.9.jar",
                "guava-31.0.1-jre.jar", "jsr305-3.0.2.jar", "querydsl-core-3.6.9.jar", "javax.inject-1.jar",
                "gson-2.8.9.jar", "spoon-core-10.0.1-SNAPSHOT.jar");
        assertIterableEquals(expectedJars, jarNames);
    }
}