package jmutation.model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ProjectTest {

    @Test
    void getTestCases() {
        String projectPath = "./sample/exampleFL1/FLtest1";
        String dropInsDir = "./lib";
        ProjectConfig config = new ProjectConfig(projectPath, dropInsDir);
        Project proj = config.getProject();

        List<TestCase> testList = proj.getTestCases();

        List<TestCase> truth = List.of(new TestCase("testSum()", 11, 16, "testSum", "CalculatorTest", null),
                new TestCase("testSubs()", 18, 23, "testSubs",  "CalculatorTest",null),
                new TestCase("testMul()", 25, 30, "testMul", "CalculatorTest", null),
                new TestCase("testDiv()", 32, 37, "testDiv", "CalculatorTest", null));
        assertEquals(truth, testList);
    }
}