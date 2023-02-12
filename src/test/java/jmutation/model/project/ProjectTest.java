package jmutation.model.project;

import jmutation.model.TestCase;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ProjectTest {

    @Test
    void getTestCases() {
        String projectPath = "./sample/exampleFL1/FLtest1";
        String dropInsDir = "./lib";
        ProjectConfig config = new ProjectConfig(projectPath, dropInsDir);
        Project proj = config.getProject();

        List<TestCase> testList = proj.getTestCases();
        String testClassName = "fr.spoonlabs.FLtest1.CalculatorTest";
        List<TestCase> truth = List.of(new TestCase("testSum()", 11, 16, "testSum", testClassName, null),
                new TestCase("testSubs()", 18, 23, "testSubs", testClassName, null),
                new TestCase("testMul()", 25, 30, "testMul", testClassName, null),
                new TestCase("testDiv()", 32, 37, "testDiv", testClassName, null));
        assertEquals(truth, testList);
    }
}