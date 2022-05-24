package jmutation.model;

import jmutation.compile.ProjectCompiler;
import jmutation.parser.ProjectParser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ProjectTest {

    @org.junit.jupiter.api.Test
    void getTestCases() {
        String projectPath = "./sample/exampleFL1/FLtest1";
        ProjectConfig config = new ProjectConfig(projectPath);
        ProjectParser parser = new ProjectParser(config);
        Project proj = parser.parse();

        List<TestCase> testList = proj.getTestCases();

        List<TestCase> truth = List.of(new TestCase("testSum()", 11, 16, "testSum", null),
                new TestCase("testSubs()", 18, 23, "testSubs", null),
                new TestCase("testMul()", 25, 30, "testMul", null),
                new TestCase("testDiv()", 32, 37, "testDiv", null),
                new TestCase("testIgnore()", 39, 45, "testIgnore", null));
        assertEquals(testList, truth);
    }
}