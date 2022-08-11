package jmutation;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import jmutation.model.TestCase;

import java.util.List;


public class Main {
    @Parameter(names = "-projectPath", description = "Path to project directory", required = true)
    private String projectPath;

    @Parameter(names = "-dropInsDir", description = "Path to instrumentation dependencies", required = true)
    private String dropInsDir;

    @Parameter(names = "-project", description = "Maven or Gradle")
    private String projectType;

    @Parameter(names = "-microbatConfig", description = "Path to JSON settings for Microbat")
    private String microbatConfigPath;


    /**
     * Given a project, we
     * 1. Determine the type of project it is (maven or gradle)
     * 2. Parse testcases statically and collect names
     * 3. Run the testcases via the `mvn` or `gradle` command
     *
     * @param args
     */
    public static void main(String[] args) {

        Main params = new Main();
        JCommander.newBuilder().addObject(params).build().parse(args);

        MutationFramework mutationFramework = new MutationFramework();
        mutationFramework.setDropInsDir(params.dropInsDir);
        mutationFramework.setMicrobatConfigPath(params.microbatConfigPath);
        mutationFramework.setProjectPath(params.projectPath);
        List<TestCase> testCaseList = mutationFramework.getTestCases();
        for (TestCase testCase : testCaseList) {
            mutationFramework.setTestCase(testCase);
            try {
                mutationFramework.startMutationFramework();
            } catch (RuntimeException e) {
                System.out.println(e);
                continue;
            }
        }
    }
}
