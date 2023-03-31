package jmutation.execution;

import jmutation.model.MicrobatConfig;
import jmutation.model.TestCase;
import jmutation.model.project.ProjectConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ProjectExecutorTest {
    @Test
    void setUpForInstrumentation_ARegularProject_CreateCorrectInstrumentationCommandBuilder(@TempDir File tempDir)
            throws IOException {
        String dropInsDir = "dropInsDir";
        ProjectConfig projectConfig = mock(ProjectConfig.class);
        File compiledClassFolder = new File(tempDir, "compiledClassFolder");
        File compiledTestClassFolder = new File(tempDir, "compiledTestClassFolder");
        File compiledFolder = new File(tempDir, "compiledFolder");
        File projectRoot = new File(tempDir, "root");
        when(projectConfig.getDropInsDir()).thenReturn(dropInsDir);
        when(projectConfig.getCompiledFolder()).thenReturn(compiledFolder);
        when(projectConfig.getCompiledClassFolder()).thenReturn(compiledClassFolder);
        when(projectConfig.getCompiledTestFolder()).thenReturn(compiledTestClassFolder);
        when(projectConfig.getProjectRoot()).thenReturn(projectRoot);

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

        assertEquals(expectedResult, instrumentationCommandBuilder);
    }

    @Test
    void aggregateFileLocations_FilesWithDifferentParents_GetsCorrectAggregate() throws IOException {
        ProjectConfig projectMock = mock(ProjectConfig.class);
        when(projectMock.getProjectRoot()).thenReturn(new File(""));
        ProjectExecutor projectExecutor = new ProjectExecutor(null, projectMock);
        File dir = new File("dir");
        File dirInDir = new File(dir, "dir");
        File fileInDir = new File(dir, "file.txt");
        File fileInDir1 = new File(dir, "file1.txt");
        File fileInInnerDir = new File(dirInDir, "file.txt");
        List<File> filesToAggregate = new ArrayList<>();
        filesToAggregate.add(fileInDir);
        filesToAggregate.add(fileInDir1);
        filesToAggregate.add(fileInInnerDir);
        Set<String> actualResult = projectExecutor.aggregateFileLocations(filesToAggregate);
        Set<String> expectedResult = new HashSet<>();
        expectedResult.add(dir.getCanonicalPath() + File.separator + "*");
        expectedResult.add(dirInDir.getCanonicalPath() + File.separator + "*");
        assertEquals(expectedResult, actualResult);
    }
}