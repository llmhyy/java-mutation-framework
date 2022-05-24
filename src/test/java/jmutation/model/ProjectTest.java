package jmutation.model;

import jmutation.parser.ProjectParser;

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

        List<TestCase> truth = List.of(new TestCase("testSum()", 11, 16, "testSum", "CalculatorTest", null),
                new TestCase("testSubs()", 18, 23, "testSubs",  "CalculatorTest",null),
                new TestCase("testMul()", 25, 30, "testMul", "CalculatorTest", null),
                new TestCase("testDiv()", 32, 37, "testDiv", "CalculatorTest", null));
        assertEquals(truth, testList);
    }
}