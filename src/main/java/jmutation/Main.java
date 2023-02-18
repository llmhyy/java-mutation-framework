package jmutation;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import jmutation.MutationFramework.MutationFrameworkBuilder;
import jmutation.model.TestCase;
import jmutation.model.mutation.MutationFrameworkConfig;
import jmutation.model.mutation.MutationFrameworkConfig.MutationFrameworkConfigBuilder;
import jmutation.mutation.Mutator;
import jmutation.mutation.heuristic.HeuristicMutator;
import jmutation.mutation.heuristic.parser.StrongMutationParser;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeoutException;


public class Main {
    @Parameter(names = "-projectPath", description = "Path to project directory", required = true)
    private String projectPath;

    @Parameter(names = "-testCase", description = "Test case name", required = true)
    private String testCase;

    @Parameter(names = "-dropInsDir", description = "Path to instrumentation dependencies")
    private String dropInsDir;

    @Parameter(names = "-microbatConfig", description = "Path to JSON settings for Microbat")
    private String microbatConfigPath;

    @Parameter(names = "-debug", description = "Run Microbat Jar in Debug Mode")
    private boolean microbatDebug = false;

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


        MutationFrameworkConfigBuilder configurationBuilder = new MutationFrameworkConfigBuilder();

        configurationBuilder.setInstrumentationTimeout(5);
        if (params.dropInsDir != null) {
            configurationBuilder.setDropInsPath(params.dropInsDir);
        }
        if (params.microbatConfigPath != null) {
            configurationBuilder.setMicrobatConfigPath(params.microbatConfigPath);
        }
        if (params.microbatDebug) {
            configurationBuilder.setInstrumentationDebug(true);
        }
        configurationBuilder.setProjectPath(params.projectPath);

        Mutator mutator = new HeuristicMutator(new StrongMutationParser());

        configurationBuilder.setMutator(mutator);
        MutationFrameworkConfig configuration = configurationBuilder.build();
        MutationFramework mutationFramework = new MutationFrameworkBuilder(configuration).build();

        try {
            mutationFramework.extractResources();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        List<TestCase> testCaseList = mutationFramework.getTestCases();
        for (TestCase testCase : testCaseList) {
            if (testCase.qualifiedName().equals(params.testCase)) {
                mutationFramework.setTestCase(testCase);
                try {
                    mutationFramework.startMutationFramework();
                    return;
                } catch (RuntimeException | TimeoutException e) {
                    e.printStackTrace();
                }
            }
        }
        throw new IllegalArgumentException("Test case " + params.testCase + " not found");
    }
}
