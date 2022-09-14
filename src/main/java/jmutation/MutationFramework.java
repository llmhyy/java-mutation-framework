package jmutation;

import jmutation.execution.ProjectExecutor;
import jmutation.model.*;
import jmutation.mutation.parser.StrongMutationParser;
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

/**
 * API class for usage as an external library
 */
public class MutationFramework {
    private String projectPath = "./sample/math70";
    private String dropInsDir = "./lib";

    private String microbatConfigPath;

    private TestCase testCase;

    private ProjectConfig config;

    private MicrobatConfig microbatConfig;

    private int maxNumberOfMutations = -1;

    private long startSeed = 1;
    private long endSeed = 1;

    private boolean isAutoSeed = false;

    private boolean strongMutationsEnabled = false;
    private PrecheckExecutionResult mutatedPrecheckExecutionResult = null;
    private ProjectExecutor mutatedProjectExecutor = null;
    private Project mutatedProject = null;
    private Project clonedProject;
    private ProjectConfig mutatedProjConfig;
    private Mutator mutator;


    /**
     * Set path to project
     * @param projectPath path to the project to mutate
     */
    public void setProjectPath(String projectPath) {
        this.projectPath = projectPath;
    }

    /**
     * Path to directory that contains microbat jar files. ./lib can be used.
     * @param dropInsDir path to microbat jar files.
     */
    public void setDropInsDir(String dropInsDir) {
        this.dropInsDir = dropInsDir;
    }

    /**
     * Path to microbat configuration json file. If not specified, default configurations are used. Otherwise, please reference ./sampleMicrobatConfig.json for the format.
     * @param microbatConfigPath path to a microbat configuration file
     */
    public void setMicrobatConfigPath(String microbatConfigPath) {
        this.microbatConfigPath = microbatConfigPath;
    }

    /**
     * Sets the test case to run mutation on.
     * @param testCase Test case to run mutation on.
     */
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

    /**
     * Sets the seed to use during mutation. This makes the mutation deterministic between each run.
     * @param seed Seed for the java.util.random
     */
    public void setSeed(long seed) {
        RandomSingleton.getSingleton().setSeed(seed);
        isAutoSeed = false;
    }

    /**
     * Automate changing of seed until the mutation fails the test case.
     * Attempt seeds from startSeed to endSeed.
     * @param startSeed First seed to use
     * @param endSeed Last seed to use
     */
    public void autoSeed(long startSeed, long endSeed) {
        this.startSeed = startSeed;
        this.endSeed = endSeed;
        isAutoSeed = true;
    }

    /**
     * Gets the test cases found in the project.
     * @return A list of test case objects.
     */
    public List<TestCase> getTestCases() {
        config = new ProjectConfig(projectPath, dropInsDir); // Contains class paths
        Project proj = config.getProject();
        return proj.getTestCases();
    }

    /**
     * Generates ProjectConfig for use by mutation framework.
     * This method should be called after updating the project path or drop ins directory.
     */
    public void generateProjectConfiguration() {
        config = new ProjectConfig(projectPath, dropInsDir); // Contains class paths
    }

    /**
     * Generates MicrobatConfig for use by mutation framework.
     * This method should be called when the microbat configuration file is updated or path is changed
     */
    public void generateMicrobatConfiguration() {
        microbatConfig = microbatConfigPath == null ? MicrobatConfig.defaultConfig(projectPath) :
                MicrobatConfig.parse(microbatConfigPath, projectPath);
    }

    public void toggleStrongMutations(boolean strongMutationsEnabled) {
        this.strongMutationsEnabled = strongMutationsEnabled;
    }

    /**
     * Starts trace collection on the chosen test case, mutates the covered code, and runs trace collection on the mutated test case.
     * @return MutationResult object.
     */
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
        PrecheckExecutionResult precheckExecutionResult = executePrecheck(projectExecutor);
        System.out.println("Normal precheck done");

;
        if (isAutoSeed) {
            runWithAutoSeed(proj, precheckExecutionResult);
        } else {
            if (strongMutationsEnabled) {
                runWithStrongMutations(proj, precheckExecutionResult);
            } else {
                runMutation(proj, precheckExecutionResult);
            }
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

        boolean wasSuccessful = mutatedPrecheckExecutionResult.testCasePassed();


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

    private void runMutation(Project proj, PrecheckExecutionResult precheckExecutionResult) {
        clonedProject = proj.cloneToOtherPath();
        mutatedProject = mutator.mutate(precheckExecutionResult.getCoverage(), clonedProject);
        mutatedProjConfig = new ProjectConfig(config, mutatedProject);

        mutatedProjectExecutor = new ProjectExecutor(microbatConfig, mutatedProjConfig);
        mutatedPrecheckExecutionResult = executePrecheck(mutatedProjectExecutor);
    }

    private void runWithAutoSeed(Project proj, PrecheckExecutionResult precheckExecutionResult) {
        boolean hasFailed = false;
        for (long i = startSeed; i <= endSeed; i++) {
            RandomSingleton.getSingleton().setSeed(i);
            if (strongMutationsEnabled) {
                runWithStrongMutations(proj, precheckExecutionResult);
            } else {
                runMutation(proj, precheckExecutionResult);
            }
            if (!mutatedPrecheckExecutionResult.testCasePassed()) {
                System.out.println("Mutated test case failed with seed " + i);
                hasFailed = true;
                break;
            }
            mutator.clearHistory();
            System.out.println("Mutation with seed " + i + " failed, re-attempting with different seed");
        }
        if (!hasFailed) {
            throw new RuntimeException("Test case " + testCase + " could not fail after attempting seeds from " + startSeed + " to " + endSeed);
        }
    }

    private void runWithStrongMutations(Project proj, PrecheckExecutionResult precheckExecutionResult) {
        for (int i = 0; i < 2; i++) {
            boolean useStrongMutations = i != 0;
            mutator = useStrongMutations ? new Mutator(new StrongMutationParser()) : new Mutator(new MutationParser());
            runMutation(proj, precheckExecutionResult);
            if (!mutatedPrecheckExecutionResult.testCasePassed()) {
                break;
            }
            String mutationType = useStrongMutations ? "strong" : "normal";
            String endOfMsg = useStrongMutations ? "" : " Re-attempting with strong mutations.";
            String message = String.format("Test case %s did not fail with %s mutations.%s",
                    testCase, mutationType, endOfMsg);
            System.out.println(message);
        }
    }
}
