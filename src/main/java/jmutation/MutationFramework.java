package jmutation;

import jmutation.constants.ExternalLibrary;
import jmutation.execution.ProjectExecutor;
import jmutation.model.MicrobatConfig;
import jmutation.model.MutationFrameworkResult;
import jmutation.model.MutationResult;
import jmutation.model.PrecheckExecutionResult;
import jmutation.model.TestCase;
import jmutation.model.TraceCollectionResult;
import jmutation.model.project.Project;
import jmutation.model.project.ProjectConfig;
import jmutation.mutation.MutationCommand;
import jmutation.mutation.Mutator;
import jmutation.mutation.heuristic.HeuristicMutator;
import jmutation.mutation.heuristic.parser.MutationParser;
import jmutation.mutation.heuristic.parser.StrongMutationParser;
import jmutation.mutation.semantic.SemanticMutator;
import jmutation.utils.RandomSingleton;
import jmutation.utils.ResourceExtractor;
import jmutation.utils.TraceHelper;
import microbat.model.trace.Trace;
import microbat.model.trace.TraceNode;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static jmutation.constants.ResourcesPath.DEFAULT_DROP_INS_DIR;
import static jmutation.constants.ResourcesPath.DEFAULT_RESOURCES_PATH;

/**
 * API class for usage as an external library
 */
public class MutationFramework {
    private String projectPath;
    private String dropInsPath = String.join(File.separator, DEFAULT_RESOURCES_PATH, DEFAULT_DROP_INS_DIR);

    private String microbatConfigPath = String.join(File.separator, DEFAULT_RESOURCES_PATH,
            "microbatConfig.json");

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
    private PrecheckExecutionResult precheckExecutionResult;
    private ProjectExecutor projectExecutor;
    private Project proj;


    /**
     * Set path to project
     *
     * @param projectPath path to the project to mutate
     */
    public void setProjectPath(String projectPath) {
        this.projectPath = projectPath;
    }

    /**
     * Path to directory that contains microbat jar files. If not specified, the default is used. (%userprofile%\lib\resources\java-mutation-framework)
     *
     * @param dropInsPath path to microbat jar files.
     */
    public void setDropInsPath(String dropInsPath) {
        this.dropInsPath = dropInsPath;
    }

    /**
     * Path to microbat configuration json file. If not specified, default configurations are used. Otherwise, please reference ./microbatConfig.json for the format.
     *
     * @param microbatConfigPath path to a microbat configuration file
     */
    public void setMicrobatConfigPath(String microbatConfigPath) {
        this.microbatConfigPath = microbatConfigPath;
    }

    /**
     * Sets the test case to run mutation on.
     *
     * @param testCase Test case to run mutation on.
     */
    public void setTestCase(TestCase testCase) {
        this.testCase = testCase;
    }

    /**
     * Sets max number of mutations. If it is 0 or less, there is no limit.
     *
     * @param maxNumberOfMutations Maximum number of mutations allowed
     */
    public void setMaxNumberOfMutations(int maxNumberOfMutations) {
        this.maxNumberOfMutations = maxNumberOfMutations;
    }

    /**
     * Sets the seed to use during mutation. This makes the mutation deterministic between each run.
     *
     * @param seed Seed for the java.util.random
     */
    public void setSeed(long seed) {
        RandomSingleton.getSingleton().setSeed(seed);
        isAutoSeed = false;
    }

    /**
     * Automate changing of seed until the mutation fails the test case.
     * Attempt seeds from startSeed to endSeed.
     *
     * @param startSeed First seed to use
     * @param endSeed   Last seed to use
     */
    public void autoSeed(long startSeed, long endSeed) {
        this.startSeed = startSeed;
        this.endSeed = endSeed;
        isAutoSeed = true;
    }

    /**
     * Gets the test cases found in the project.
     *
     * @return A list of test case objects.
     */
    public List<TestCase> getTestCases() {
        config = new ProjectConfig(projectPath, dropInsPath); // Contains class paths
        Project proj = config.getProject();
        return proj.getTestCases();
    }

    /**
     * Generates ProjectConfig for use by mutation framework.
     * This method should be called after updating the project path or drop ins directory.
     */
    public void generateProjectConfiguration() {
        config = new ProjectConfig(projectPath, dropInsPath); // Contains class paths
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

    public void setMicrobatConfig(MicrobatConfig microbatConfig) {
        this.microbatConfig = microbatConfig;
        projectPath = microbatConfig.getWorkingDir();
    }

    public void setMutator(Mutator mutator) {
        this.mutator = mutator;
    }

    public void extractResources() throws IOException {
        extractResources(DEFAULT_RESOURCES_PATH);
    }

    public void extractResources(String path) throws IOException {
        for (ExternalLibrary externalLibrary : ExternalLibrary.values()) {
            ResourceExtractor.extractFile("lib/" + externalLibrary.getName() + ".jar", path);
        }
        ResourceExtractor.extractFile("microbatConfig.json", path);
        ResourceExtractor.extractFile("semantic/bug-fix-patterns.json", path);
        // TODO: model.bin is too large to push to git.
        // ResourceExtractor.extractFile("semantic/model.bin", path);
    }

    /**
     * Starts trace collection on the chosen test case, mutates the covered code, and runs trace collection on the mutated test case.
     *
     * @return MutationResult object.
     */
    public MutationFrameworkResult startMutationFramework() {
        mutate();
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
        MicrobatConfig includeAssertionsMicrobatConfig = addAssertionsToMicrobatConfig(updatedMicrobatConfig);
        projectExecutor.setMicrobatConfig(includeAssertionsMicrobatConfig);
        TraceCollectionResult originalResultWithAssertionsInTrace = projectExecutor.exec(testCase);

        MicrobatConfig includeAssertionsMutationMicrobatConfig = addAssertionsToMicrobatConfig(updatedMutationMicrobatConfig);
        mutatedProjectExecutor.setMicrobatConfig(includeAssertionsMutationMicrobatConfig);
        TraceCollectionResult mutatedResultWithAssertionsInTrace = mutatedProjectExecutor.exec(testCase);

        Trace mutatedTrace = mutatedResult.getTrace();
        List<TraceNode> rootCauses = TraceHelper.getMutatedTraceNodes(mutatedTrace, mutator.getMutationHistory());

        boolean wasSuccessful = mutatedPrecheckExecutionResult.testCasePassed();


        return new MutationFrameworkResult(result.getInstrumentationResult(),
                mutatedResult.getInstrumentationResult(), originalResultWithAssertionsInTrace.getInstrumentationResult(), mutatedResultWithAssertionsInTrace.getInstrumentationResult(),
                mutator.getMutationHistory(), proj, mutatedProject, rootCauses,
                wasSuccessful, testCase);
    }

    public MutationResult mutate() {
        if (!setup()) return null;
        // Do precheck for normal + mutation to catch issues
        // If no issues, collect trace for normal + mutation, and return them in mutation result

        projectExecutor = new ProjectExecutor(microbatConfig, config);
        // Precheck
        precheckExecutionResult = executePrecheck(projectExecutor);
        System.out.println("Normal precheck done");

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
        return new MutationResult(precheckExecutionResult, mutatedPrecheckExecutionResult, testCase, mutator.getMutationHistory());
    }

    public List<MutationCommand> analyse() {
        if (!setup()) return null;
        // Do precheck for normal + mutation to catch issues
        // If no issues, collect trace for normal + mutation, and return them in mutation result

        projectExecutor = new ProjectExecutor(microbatConfig, config);
        // Precheck
        precheckExecutionResult = executePrecheck(projectExecutor);
        System.out.println("Normal precheck done");
        return mutator.analyse(precheckExecutionResult.getCoverage().getRanges(), proj);
    }

    public MutationResult mutate(MutationCommand command) {
        runMutation(proj, command);

        System.out.println("Mutated precheck done");
        return new MutationResult(precheckExecutionResult, mutatedPrecheckExecutionResult, testCase, mutator.getMutationHistory());
    }

    public boolean setup() {
        if (projectPath == null || dropInsPath == null) {
            System.out.println("Project path or drop ins directory not specified");
            return false;
        }

        if (config == null) {
            generateProjectConfiguration();
        }

        proj = config.getProject();

        if (testCase == null) {
            System.out.println("Test case to mutate not specified, choosing first test case found");
            testCase = proj.getTestCases().get(0);
        }

        if (microbatConfig == null) {
            generateMicrobatConfiguration();
        }

        microbatConfig = microbatConfig.setWorkingDir(projectPath);

        setupMutator(new MutationParser());
        System.out.println(testCase);
        return true;
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
        mutatedProjConfig = new ProjectConfig(config, clonedProject);
        mutatedProjectExecutor = new ProjectExecutor(microbatConfig, mutatedProjConfig);
        String resourcesPath = String.join(File.separator, "src", "main", "resources", "semantic");
        mutator = new SemanticMutator(resourcesPath + File.separator + "bug-fix-patterns.json",
                resourcesPath + File.separator + "model.bin");
        mutatedProject = mutator.mutate(precheckExecutionResult.getCoverage(), clonedProject);
        mutatedProjConfig = new ProjectConfig(config, mutatedProject);

        mutatedProjectExecutor = new ProjectExecutor(microbatConfig, mutatedProjConfig);
        mutatedPrecheckExecutionResult = executePrecheck(mutatedProjectExecutor);
    }

    private void runMutation(Project proj, MutationCommand command) {
        clonedProject = proj.cloneToOtherPath();
        mutatedProject = mutator.mutate(command, clonedProject);
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
            MutationParser mutationParser = useStrongMutations ? new StrongMutationParser() : new MutationParser();
            setupMutator(mutationParser);
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

    private void setupMutator(MutationParser mutationParser) {
        mutator = new HeuristicMutator(mutationParser);
    }

    private MicrobatConfig addAssertionsToMicrobatConfig(MicrobatConfig config) {
        String[] assertionsArr = new String[]{"org.junit.Assert", "org.junit.jupiter.api.Assertions", "org.testng.Assert"};
        return config.setIncludes(Arrays.asList(assertionsArr));
    }
}
