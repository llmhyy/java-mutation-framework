package jmutation.mutation.explainable.doc;

import jmutation.execution.Coverage;
import jmutation.model.TestCase;
import jmutation.model.project.MavenProject;
import jmutation.model.project.Project;
import jmutation.mutation.explainable.ExplainableMutator;
import microbat.model.ClassLocation;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static jmutation.mutation.explainable.doc.ExplainableMutationTestConstants.EXPLAINABLE_SAMPLE_PROJECT_PATH;
import static jmutation.mutation.explainable.doc.ExplainableMutationTestConstants.EXPLAINABLE_SAMPLE_PROJECT_PATH_TO_MUTATE;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ExplainableMutatorTest {
    private static final File PROJECT_ROOT_FILE = new File(EXPLAINABLE_SAMPLE_PROJECT_PATH);
    private static final File PROJECT_TO_MUTATE_ROOT_FILE = new File(EXPLAINABLE_SAMPLE_PROJECT_PATH_TO_MUTATE);

    @Test
    void mutate_coverageAndProjectProvided_mutatesProjectCorrectly() throws IOException {
        // Given some coverage and a project, it should mutate it to remove code that handles a corner case
        ExplainableMutator explainableMutator = new ExplainableMutator();
        Coverage coverage = new Coverage();
        Set<ClassLocation> classLocationSet = new HashSet<>();
        ClassLocation classLocation = new ClassLocation("Sample", "someMethod", 1);
        classLocationSet.add(classLocation);
        TestCase testCase = new TestCase("someMethodTest", 0, 0, "someMethodTest", "SampleTest", null);
        coverage.formMutationRanges(classLocationSet, testCase);
        List<TestCase> testCaseList = new ArrayList<>();
        testCaseList.add(testCase);
        Project project = new MavenProject("sample", PROJECT_TO_MUTATE_ROOT_FILE, testCaseList);
        explainableMutator.mutate(coverage, project);
        File sourceFile = new File(PROJECT_TO_MUTATE_ROOT_FILE, String.join(File.separator, "src", "main", "java", "Sample.java"));
        String actualFileContent = Files.readString(sourceFile.toPath());
        String expectedFileContent = String.join(System.lineSeparator(), "public class Sample {", "    /**", "     * Adds 1 to the input value.", "     * corner case: If a is 0, it returns 0", "     *", "     * @param a the input integer", "     * @return a + 1, unless a is 0", "     */", "    public int someMethod(int a) {", "        int b = a + 1;", "        return b;", "    }", "}");
        assertEquals(expectedFileContent, actualFileContent);
    }

    @Test
    void mutate_compilationFailed_rewritesTheOriginalFileBack() throws IOException {
        // If compilation fails, the mutation should be reverted
        ExplainableMutator explainableMutator = new ExplainableMutatorWithCompilationFail();
        Coverage coverage = new Coverage();
        Set<ClassLocation> classLocationSet = new HashSet<>();
        ClassLocation classLocation = new ClassLocation("Sample", "someMethod", 1);
        classLocationSet.add(classLocation);
        TestCase testCase = new TestCase("someMethodTest", 0, 0, "someMethodTest", "SampleTest", null);
        coverage.formMutationRanges(classLocationSet, testCase);
        List<TestCase> testCaseList = new ArrayList<>();
        testCaseList.add(testCase);
        Project project = new MavenProject("sample", PROJECT_TO_MUTATE_ROOT_FILE, testCaseList);
        explainableMutator.mutate(coverage, project);
        File sourceFile = new File(PROJECT_TO_MUTATE_ROOT_FILE, String.join(File.separator, "src", "main", "java", "Sample.java"));
        String actualFileContent = Files.readString(sourceFile.toPath());
        String expectedFileContent = String.join(System.lineSeparator(), "public class Sample {", "    /**",
                "     * Adds 1 to the input value.",
                "     * corner case: If a is 0, it returns 0",
                "     *", "     * @param a the input integer",
                "     * @return a + 1, unless a is 0",
                "     */", "    public int someMethod(int a) {",
                "        int b = a + 1;",
                "        if (a == 0) {",
                "            return 0;",
                "        }",
                "        return b;", "    }", "}");
        assertEquals(expectedFileContent, actualFileContent);
    }

    @AfterEach
    void afterEach() throws IOException {
        FileUtils.copyDirectory(PROJECT_ROOT_FILE, PROJECT_TO_MUTATE_ROOT_FILE);
    }

    private class ExplainableMutatorWithCompilationFail extends ExplainableMutator {
        @Override
        public boolean compileProject(Project project) {
            return false;
        }
    }
}
