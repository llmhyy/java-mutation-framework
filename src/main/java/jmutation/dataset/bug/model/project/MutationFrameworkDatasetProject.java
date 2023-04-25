package jmutation.dataset.bug.model.project;

import jmutation.model.TestCase;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class MutationFrameworkDatasetProject extends DatasetProject {
    private static final String TEST_CASE_FILE = "testcase.txt";

    public MutationFrameworkDatasetProject(String projectPath) {
        super(projectPath);
    }

    @Override
    public List<TestCase> getFailingTests() {
        String pathToTestCaseFile = projectPath.substring(0, projectPath.length() - 3) + TEST_CASE_FILE;
        List<TestCase> result = new ArrayList<>();
        try {
            String testCaseString = Files.readString(Path.of(pathToTestCaseFile));
            String testClass = testCaseString.substring(0, testCaseString.indexOf("#"));
            String testMethod = testCaseString.substring(testCaseString.indexOf("#") + 1, testCaseString.indexOf("("));
            TestCase testCase = new TestCase("signature", 0, 0, testMethod, testClass, null);
            result.add(testCase);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }
}
