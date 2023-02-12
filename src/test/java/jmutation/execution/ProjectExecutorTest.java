package jmutation.execution;

import jmutation.model.MicrobatConfig;
import jmutation.model.TestCase;
import jmutation.model.project.ProjectConfig;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ProjectExecutorTest {

    @Test
    // Enabled only in GitHub actions, as it takes very long to run locally
    @EnabledIf("jmutation.utils.MiscellaneousTestUtils#isRunningInGitHubActions")
    void findJars() {
        String projectPath = "./sample/spoon";
        String dropInsDir = "./lexpectedResult";
        ProjectConfig config = new ProjectConfig(projectPath, dropInsDir);
        ProjectExecutor pe = new ProjectExecutor(MicrobatConfig.defaultConfig(projectPath), config);
        pe.packageProj();
        List<String> jarNames = pe.findJars().stream().map(file -> file.getName()).collect(Collectors.toList());
        Iterator<File> expectedJarsIterator = FileUtils.iterateFiles(new File(projectPath + "/target"), new String[]{"jar"}, true);
        List<String> expectedJars = new ArrayList<>();
        while (expectedJarsIterator.hasNext()) {
            File jar = expectedJarsIterator.next();
            expectedJars.add(jar.getName());
        }
        assertEquals(expectedJars, jarNames);
    }

    @Test
    void setUpForInstrumentation_ProjectWithDependencies_CreateCorrectInstrumentationCommandBuilder(@TempDir File tempDir)
            throws IOException {
        String dropInsDir = "dropInsDir";
        ProjectConfig projectConfig = mock(ProjectConfig.class);
        File compiledClassFolder = new File(tempDir, "compiledClassFolder");
        File compiledTestClassFolder = new File(tempDir, "compiledTestClassFolder");
        File compiledFolder = new File(tempDir, "compiledFolder");
        File projectRoot = new File(tempDir, "root");
        List<File> externalLibs = new ArrayList<>();
        externalLibs.add(new File(tempDir, "externalLib.jar"));
        when(projectConfig.getDropInsDir()).thenReturn(dropInsDir);
        when(projectConfig.getCompiledFolder()).thenReturn(compiledFolder);
        when(projectConfig.getCompiledClassFolder()).thenReturn(compiledClassFolder);
        when(projectConfig.getCompiledTestFolder()).thenReturn(compiledTestClassFolder);
        when(projectConfig.getProjectRoot()).thenReturn(projectRoot);
        when(projectConfig.getExtLibs()).thenReturn(externalLibs);

        MicrobatConfig microbatConfig = mock(MicrobatConfig.class);
        when(microbatConfig.setPrecheck(false)).thenReturn(microbatConfig);
        File dumpFile = new File(tempDir, "dumpFile.exec");
        when(microbatConfig.getDumpFilePath()).thenReturn(dumpFile.getCanonicalPath());

        TestCase testCase = new TestCase("signature", 0, 1, "simpleName",
                "testClass", null);
        ProjectExecutor projectExecutor = new ProjectExecutor(microbatConfig, projectConfig);
        InstrumentationCommandBuilder instrumentationCommandBuilder = projectExecutor.setUpForInstrumentation(testCase,
                false);
        InstrumentationCommandBuilder expectedResult = new InstrumentationCommandBuilder(microbatConfig, dropInsDir);
        expectedResult.setTestCase(testCase.testClass, testCase.simpleName); // set class and method name
        expectedResult.addClassPath(projectConfig.getCompiledTestFolder()); // add target/test-classes
        expectedResult.addClassPath(projectConfig.getCompiledClassFolder()); // add target/classes
        expectedResult.setWorkingDirectory(projectConfig.getProjectRoot());

        externalLibs.forEach(file -> { // add jar files
            expectedResult.addClassPath(file);
            expectedResult.addExternalLibPath(file);
        });
        assertEquals(expectedResult, instrumentationCommandBuilder);
    }
}