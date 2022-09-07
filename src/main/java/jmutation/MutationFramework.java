package jmutation;

import jmutation.execution.ProjectExecutor;
import jmutation.model.*;
import jmutation.utils.TraceHelper;
import jmutation.model.project.Project;
import jmutation.model.project.ProjectConfig;
import jmutation.mutation.Mutator;
import jmutation.mutation.parser.MutationParser;
import jmutation.utils.RandomSingleton;
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

    private MicrobatConfig microbatConfig;

    private int maxNumberOfMutations = -1;

    private int startSeed = 1;
    private int endSeed = 1;

    private boolean isAutoSeed = false;

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

    /**
     * Sets max number of mutations. If it is 0 or less, there is no limit.
     * @param maxNumberOfMutations Maximum number of mutations allowed
     */
    public void setMaxNumberOfMutations(int maxNumberOfMutations) {
        this.maxNumberOfMutations = maxNumberOfMutations;
    }

    public void setSeed(long seed) {
        RandomSingleton.getSingleton().setSeed(seed);
        isAutoSeed = false;
    }

    /**
     * Automate changing of seed until the mutation fails the test case
     * Attempts seeds from startSeed to endSeed
     */
    public void autoSeed(int startSeed, int endSeed) {
        this.startSeed = startSeed;
        this.endSeed = endSeed;
        isAutoSeed = true;
    }

    public List<TestCase> getTestCases() {
        config = new ProjectConfig(projectPath, dropInsDir); // Contains class paths
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

        Mutator mutator = new Mutator(new MutationParser());
        mutator.setMaxNumberOfMutations(maxNumberOfMutations);
        System.out.println(testCase);
        // Do precheck for normal + mutation to catch issues
        // If no issues, collect trace for normal + mutation, and return them in mutation result

        ProjectExecutor projectExecutor = new ProjectExecutor(microbatConfig, config);
        // Precheck
        PrecheckExecutionResult precheckExecutionResult = executePrecheck(projectExecutor);
        System.out.println("Normal precheck done");

        PrecheckExecutionResult mutatedPrecheckExecutionResult = null;
        ProjectExecutor mutatedProjectExecutor = null;
        Project mutatedProject = null;
        Project clonedProject;
        ProjectConfig mutatedProjConfig;
        if (isAutoSeed) {
            boolean hasFailed = false;
            for (int i = startSeed; i <= endSeed; i++) {
                this.setSeed(i);
                clonedProject = proj.cloneToOtherPath();
                mutatedProject = mutator.mutate(precheckExecutionResult.getCoverage(), clonedProject);
                mutatedProjConfig = new ProjectConfig(config, mutatedProject);

                mutatedProjectExecutor = new ProjectExecutor(microbatConfig, mutatedProjConfig);
                mutatedPrecheckExecutionResult = executePrecheck(mutatedProjectExecutor);
                if (!mutatedPrecheckExecutionResult.testCasePassed()) {
                    System.out.println("Mutated test case failed with seed " + i);
                    hasFailed = true;
                    break;
                }
                System.out.println("Mutation did not fail with seed " + i + ", re-attempting with different seed");
                mutator.clearHistory();
            }
            if (!hasFailed) {
                throw new RuntimeException("Test case " + testCase + " could not fail after attempting seeds from " + startSeed + " to " + endSeed);
            }
        } else {
            clonedProject = proj.cloneToOtherPath();
            mutatedProject = mutator.mutate(precheckExecutionResult.getCoverage(), clonedProject);
            mutatedProjConfig = new ProjectConfig(config, mutatedProject);

            mutatedProjectExecutor = new ProjectExecutor(microbatConfig, mutatedProjConfig);
            mutatedPrecheckExecutionResult = executePrecheck(mutatedProjectExecutor);
        }
        System.out.println("Mutated precheck done");

        // Actual trace
        MicrobatConfig updatedMicrobatConfig = microbatConfig.setExpectedSteps(precheckExecutionResult.getTotalSteps());
        projectExecutor.setMicrobatConfig(updatedMicrobatConfig);
        TraceCollectionResult result = projectExecutor.exec(testCase);
        System.out.println("Normal trace done");
        MicrobatConfig updatedMutationMicrobatConfig = microbatConfig.setExpectedSteps(mutatedPrecheckExecutionResult.getTotalSteps());
        mutatedProjectExecutor.setMicrobatConfig(updatedMutationMicrobatConfig);
        TraceCollectionResult mutatedResult = mutatedProjectExecutor.exec(testCase);
        System.out.println("Mutated trace done");

        // Trace with assertions to get output of test case
        MicrobatConfig includeAssertionsMicrobatConfig = updatedMicrobatConfig.setIncludes(Arrays.asList("org.junit.Assert"));
        projectExecutor.setMicrobatConfig(includeAssertionsMicrobatConfig);
        TraceCollectionResult originalResultWithAssertionsInTrace = projectExecutor.exec(testCase);

        MicrobatConfig includeAssertionsMutationMicrobatConfig = updatedMutationMicrobatConfig.setIncludes(Arrays.asList("org.junit.Assert"));
        mutatedProjectExecutor.setMicrobatConfig(includeAssertionsMutationMicrobatConfig);
        TraceCollectionResult mutatedResultWithAssertionsInTrace = mutatedProjectExecutor.exec(testCase);

        Trace mutatedTrace = mutatedResult.getTrace();
        List<TraceNode> rootCauses = TraceHelper.getMutatedTraceNodes(mutatedTrace, mutator.getMutationHistory());

        boolean wasSuccessful = mutatedResult.isSuccessful();


        return new MutationResult(result.getInstrumentationResult(),
                mutatedResult.getInstrumentationResult(), originalResultWithAssertionsInTrace.getInstrumentationResult(), mutatedResultWithAssertionsInTrace.getInstrumentationResult(),
                mutator.getMutationHistory(), proj, mutatedProject, rootCauses,
                wasSuccessful, testCase);
    }

    private PrecheckExecutionResult executePrecheck(ProjectExecutor projectExecutor) {
        PrecheckExecutionResult precheckExecutionResult = projectExecutor.execPrecheck(testCase);
        if (precheckExecutionResult.isOverLong()) {
            throw new RuntimeException("Precheck for test case " + testCase + " was over long as step limit was " + microbatConfig.getStepLimit() + " but had " + precheckExecutionResult.getTotalSteps() + " steps");
        }
        return precheckExecutionResult;
    }

}
