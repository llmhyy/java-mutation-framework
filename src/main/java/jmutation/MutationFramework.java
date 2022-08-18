package jmutation;

import jmutation.execution.ProjectExecutor;
import jmutation.model.ExecutionResult;
import jmutation.model.MicrobatConfig;
import jmutation.model.MutationResult;
import jmutation.model.PrecheckExecutionResult;
import jmutation.model.Project;
import jmutation.model.ProjectConfig;
import jmutation.model.TestCase;
import jmutation.model.TestIO;
import jmutation.mutation.Mutator;
import jmutation.mutation.parser.MutationParser;
import jmutation.utils.RandomSingleton;
import jmutation.utils.TraceHelper;
import microbat.model.trace.Trace;
import microbat.model.trace.TraceNode;

import java.util.Arrays;
import java.util.List;

// API class for other projects to call
public class MutationFramework {
    private String projectPath = "./sample/math70";
    private String dropInsDir = "./lib";

    private String microbatConfigPath;

    private TestCase testCase;

    private ProjectConfig config;

    private Mutator mutator;

    private MicrobatConfig microbatConfig;

    private int maxNumberOfMutations = -1;

    public void setProjectPath(String projectPath) {
        this.projectPath = projectPath;
    }

    public void setDropInsDir(String dropInsDir) {
        this.dropInsDir = dropInsDir;
    }

    public void setMicrobatConfigPath(String microbatConfigPath) {
        this.microbatConfigPath = microbatConfigPath;
    }

    public void setTestCase(TestCase testCase) {
        this.testCase = testCase;
    }

    public void setMaxNumberOfMutations(int maxNumberOfMutations) {
        this.maxNumberOfMutations = maxNumberOfMutations;
    }

    public void setSeed(long seed) {
        RandomSingleton.getSingleton().setSeed(seed);
    }

    public List<TestCase> getTestCases() {
        ProjectConfig config = new ProjectConfig(projectPath, dropInsDir); // Contains class paths
        Project proj = config.getProject();
        return proj.getTestCases();
    }

    public void generateProjectConfiguration() {
        config = new ProjectConfig(projectPath, dropInsDir); // Contains class paths
    }

    public void generateMicrobatConfiguration() {
        microbatConfig = microbatConfigPath == null ? MicrobatConfig.defaultConfig(projectPath) :
                MicrobatConfig.parse(microbatConfigPath, projectPath);
    }

    public MutationResult startMutationFramework() {
        if (projectPath == null || dropInsDir == null) {
            System.out.println("Project path or drop ins directory not specified");
            return null;
        }

        if (config == null) {
            generateProjectConfiguration();
        }

        Project proj = config.getProject();

        if (testCase == null) {
            System.out.println("Test case to mutate not specified, choosing first test case found");
            testCase = proj.getTestCases().get(0);
        }

        if (microbatConfig == null) {
            generateMicrobatConfiguration();
        }

        mutator = new Mutator(new MutationParser());
        mutator.setMaxNumberOfMutations(maxNumberOfMutations);
        System.out.println(testCase);
        // Do precheck for normal + mutation to catch issues
        // If no issues, collect trace for normal + mutation, and return them in mutation result

        ProjectExecutor projectExecutor = new ProjectExecutor(microbatConfig, config);
        // Precheck
        PrecheckExecutionResult precheckExecutionResult = projectExecutor.execPrecheck(testCase);
        if (precheckExecutionResult.isOverLong()) {
            throw new RuntimeException("Precheck for test case " + testCase + " was over long as step limit was " + microbatConfig.getStepLimit() + " but had " + precheckExecutionResult.getTotalSteps() + " steps");
        }
        System.out.println("Normal precheck done");

        Project clonedProject = proj.cloneToOtherPath();
        Project mutatedProject = mutator.mutate(precheckExecutionResult.getCoverage(), clonedProject);
        ProjectConfig mutatedProjConfig = new ProjectConfig(config, mutatedProject);

        ProjectExecutor mutatedProjectExecutor = new ProjectExecutor(microbatConfig, mutatedProjConfig);
        PrecheckExecutionResult mutatedPrecheckExecutionResult = mutatedProjectExecutor.execPrecheck(testCase);
        if (mutatedPrecheckExecutionResult.isOverLong()) {
            throw new RuntimeException("Precheck for mutated test case " + testCase + " was over long as step limit was " + microbatConfig.getStepLimit() + " but had " + mutatedPrecheckExecutionResult.getTotalSteps() + " steps");
        }
        System.out.println("Mutated precheck done");

        // Actual trace
        MicrobatConfig updatedMicrobatConfig = microbatConfig.setExpectedSteps(precheckExecutionResult.getTotalSteps());
        projectExecutor.setMicrobatConfig(updatedMicrobatConfig);
        ExecutionResult result = projectExecutor.exec(testCase);
        System.out.println("Normal trace done");
        MicrobatConfig updatedMutationMicrobatConfig = microbatConfig.setExpectedSteps(mutatedPrecheckExecutionResult.getTotalSteps());
        mutatedProjectExecutor.setMicrobatConfig(updatedMutationMicrobatConfig);
        ExecutionResult mutatedResult = mutatedProjectExecutor.exec(testCase);
        System.out.println("Mutated trace done");

        // Trace with assertions to get output of test case
        MicrobatConfig includeAssertionsMutationMicrobatConfig = updatedMutationMicrobatConfig.setIncludes(Arrays.asList("org.junit.Assert"));
        mutatedProjectExecutor.setMicrobatConfig(includeAssertionsMutationMicrobatConfig);
        ExecutionResult mutatedResultWithAssertionsInTrace = mutatedProjectExecutor.exec(testCase);

        Trace mutatedTrace = mutatedResult.getTrace();
        List<TraceNode> rootCauses = TraceHelper.getMutatedTraceNodes(mutatedTrace, mutator.getMutationHistory());
        List<TestIO> testIOs = TraceHelper.getTestInputOutputs(mutatedResult, mutatedResultWithAssertionsInTrace, testCase);

        for (TestIO testIO : testIOs) {
            testIO.setHasPassed(true);
        }
        boolean wasSuccessful = mutatedResult.isSuccessful();
        if (!testIOs.isEmpty()) {
            TestIO lastTestIO = testIOs.get(testIOs.size() - 1);
            lastTestIO.setHasPassed(wasSuccessful);
        }

        MutationResult mutationResult = new MutationResult(result.getTrace(),
                mutatedTrace, mutator.getMutationHistory(), proj, mutatedProject, rootCauses,
                testIOs, wasSuccessful);

        return mutationResult;
    }
}
