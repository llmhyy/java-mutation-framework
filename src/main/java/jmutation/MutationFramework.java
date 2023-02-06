package jmutation;

import jmutation.constants.ExternalLibrary;
import jmutation.execution.Coverage;
import jmutation.execution.ProjectExecutor;
import jmutation.model.MicrobatConfig;
import jmutation.model.PrecheckExecutionResult;
import jmutation.model.TestCase;
import jmutation.model.TraceCollectionResult;
import jmutation.model.mutation.MutationFrameworkConfig;
import jmutation.model.mutation.MutationFrameworkResult;
import jmutation.model.mutation.MutationResult;
import jmutation.model.project.Project;
import jmutation.model.project.ProjectConfig;
import jmutation.mutation.MutationCommand;
import jmutation.utils.RandomSingleton;
import jmutation.utils.ResourceExtractor;
import jmutation.utils.TraceHelper;
import microbat.model.trace.Trace;
import microbat.model.trace.TraceNode;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeoutException;

import static jmutation.constants.ResourcesPath.DEFAULT_RESOURCES_PATH;

/**
 * API class for usage as an external library
 */
public class MutationFramework {
    private final MutationFrameworkConfig configuration;
    private ProjectConfig projectConfig;
    private PrecheckExecutionResult mutatedPrecheckExecutionResult = null; // Instead of storing, should pass it into the trace collection method as argument
    private ProjectExecutor mutatedProjectExecutor = null;
    private Project mutatedProject = null;
    private ProjectConfig mutatedProjConfig;
    private PrecheckExecutionResult precheckExecutionResult; // Instead of storing, should pass it into the trace collection method as argument
    private ProjectExecutor projectExecutor;
    private Project project;

    private MutationFramework(MutationFrameworkConfig config, ProjectConfig projectConfig) {
        configuration = config;
        this.projectConfig = projectConfig;
        project = projectConfig.getProject();
    }

    /**
     * Gets the test cases found in the project.
     *
     * @return A list of test case objects.
     */
    public List<TestCase> getTestCases() {
        generateProjectConfiguration();
        Project proj = projectConfig.getProject();
        return proj.getTestCases();
    }

    public MutationFrameworkConfig getConfiguration() {
        return configuration;
    }

    /**
     * Generates ProjectConfig for use by mutation framework.
     * This method should be called after updating the project path or drop ins directory.
     */
    private void generateProjectConfiguration() {
        if (projectConfig == null) {
            projectConfig = new ProjectConfig(configuration.getProjectPath(), configuration.getDropInsPath()); // Contains class paths
        }
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
        // TODO: token-embeddings-FT.bin is too large to push to git.
        // ResourceExtractor.extractFile("semantic/token-embeddings-FT.bin", path);
    }

    public void setTestCase(TestCase testCase) {
        configuration.setTestCase(testCase);
    }

    /**
     * Starts trace collection on the chosen test case, mutates the covered code, and runs trace collection on the mutated test case.
     *
     * @return MutationResult object.
     */
    public MutationFrameworkResult startMutationFramework() throws TimeoutException {
        configuration.getMutator().clearHistory();
        mutate();
        return runTraceCollection();
    }

    /**
     * @param command
     * @return
     */
    public MutationFrameworkResult startMutationFramework(MutationCommand command) throws TimeoutException {
        configuration.getMutator().clearHistory();
        mutate(command);
        return runTraceCollection();
    }

    /**
     * Obtains coverage, clones the project, and randomly mutates part of the coverage
     *
     * @return
     */
    public MutationResult mutate() {
        testCaseIsNotNull();
        // Do precheck for normal + mutation to catch issues
        // If no issues, collect trace for normal + mutation, and return them in mutation result
        MicrobatConfig updatedMicrobatConfig = configuration.getMicrobatConfig();
        updatedMicrobatConfig = updatedMicrobatConfig.setDumpFilePath(configuration.getDumpFilePathConfig().getPrecheckFilePath());
        projectExecutor = new ProjectExecutor(updatedMicrobatConfig, projectConfig);
        // Precheck
        precheckExecutionResult = executePrecheck(projectExecutor);
        System.out.println("Normal precheck done");

        if (configuration.isAutoSeed()) {
            runWithAutoSeed(project, precheckExecutionResult);
        } else {
            runMutation(project, precheckExecutionResult);
        }
        System.out.println("Mutated precheck done");
        return new MutationResult(precheckExecutionResult, mutatedPrecheckExecutionResult, configuration.getTestCase(),
                configuration.getMutator().getMutationHistory());
    }

    /**
     * Obtains a list of possible mutations for the configured test case
     *
     * @return
     */
    public List<MutationCommand> analyse(Coverage coverage) {
        testCaseIsNotNull();
        return configuration.getMutator().analyse(coverage.getRanges(), project);
    }

    /**
     * Runs precheck.
     * Checks whether test case passed and coverage to be used for creating possible mutations in analyse
     *
     * @return
     */
    public PrecheckExecutionResult runPrecheck() {
        testCaseIsNotNull();
        // Do precheck for normal + mutation to catch issues
        // If no issues, collect trace for normal + mutation, and return them in mutation result
        MicrobatConfig updatedMicroBatConfig = configuration.getMicrobatConfig().
                setDumpFilePath(configuration.getDumpFilePathConfig().getPrecheckFilePath());
        ProjectExecutor projectExecutor = new ProjectExecutor(updatedMicroBatConfig, projectConfig);
        // Precheck
        return executePrecheck(projectExecutor);
    }

    /**
     * Execute mutation with mutation command
     *
     * @param command
     * @return
     */
    public MutationResult mutate(MutationCommand command) {
        testCaseIsNotNull();
        runMutation(project, command);

        System.out.println("Mutated precheck done");
        return new MutationResult(precheckExecutionResult, mutatedPrecheckExecutionResult, configuration.getTestCase(),
                configuration.getMutator().getMutationHistory());
    }

    private void testCaseIsNotNull() {
        if (configuration.getTestCase() == null)
            throw new IllegalStateException("Test case cannot be null");
    }

    private PrecheckExecutionResult executePrecheck(ProjectExecutor projectExecutor) {
        PrecheckExecutionResult precheckResult = projectExecutor.execPrecheck(configuration.getTestCase(),
                configuration.isToDeletePrecheckFile());
        if (precheckResult.isOverLong()) {
            throw new RuntimeException("Precheck for test case " + configuration.getTestCase() +
                    " was over long as step limit was " + configuration.getMicrobatConfig().getStepLimit() +
                    " but had " + precheckResult.getTotalSteps() + " steps");
        }
        return precheckResult;
    }

    private void runMutation(Project proj, PrecheckExecutionResult precheckExecutionResult) {
        Project clonedProject = proj.cloneToOtherPath(configuration.getMutatedProjectPath());
        mutatedProject = configuration.getMutator().mutate(precheckExecutionResult.getCoverage(), clonedProject);
        mutatedProjConfig = new ProjectConfig(projectConfig, mutatedProject);
        MicrobatConfig precheckMicrobatConfig = configuration.getMicrobatConfig();
        precheckMicrobatConfig = precheckMicrobatConfig.setDumpFilePath(
                configuration.getDumpFilePathConfig().getMutatedPrecheckFilePath());
        mutatedProjectExecutor = new ProjectExecutor(precheckMicrobatConfig, mutatedProjConfig);

        mutatedPrecheckExecutionResult = executePrecheck(mutatedProjectExecutor);
    }

    private void runMutation(Project proj, MutationCommand command) {
        Project clonedProject = proj.cloneToOtherPath(configuration.getMutatedProjectPath());
        Project mutatedProject = configuration.getMutator().mutate(command, clonedProject);
        ProjectConfig mutatedProjConfig = new ProjectConfig(projectConfig, mutatedProject);
        MicrobatConfig microbatConfig = configuration.getMicrobatConfig().setDumpFilePath(
                configuration.getDumpFilePathConfig().getMutatedPrecheckFilePath());
        ProjectExecutor mutatedProjectExecutor = new ProjectExecutor(microbatConfig, mutatedProjConfig);
        executePrecheck(mutatedProjectExecutor);
    }

    private void runWithAutoSeed(Project proj, PrecheckExecutionResult precheckExecutionResult) {
        boolean hasFailed = false;
        for (long i = configuration.getStartSeed(); i <= configuration.getEndSeed(); i++) {
            RandomSingleton.getSingleton().setSeed(i);
            runMutation(proj, precheckExecutionResult);
            if (!mutatedPrecheckExecutionResult.testCasePassed()) {
                System.out.println("Mutated test case failed with seed " + i);
                hasFailed = true;
                break;
            }
            configuration.getMutator().clearHistory();
            System.out.println("Mutation with seed " + i + " failed, re-attempting with different seed");
        }
        if (!hasFailed) {
            throw new RuntimeException("Test case " + configuration.getTestCase() +
                    " could not fail after attempting seeds from " +
                    configuration.getStartSeed() + " to " + configuration.getEndSeed());
        }
    }

    private MicrobatConfig addAssertionsToMicrobatConfig(MicrobatConfig config) {
        String[] assertionsArr = new String[]{"org.junit.Assert", "org.junit.jupiter.api.Assertions", "org.testng.Assert"};
        return config.setIncludes(Arrays.asList(assertionsArr));
    }

    private MutationFrameworkResult runTraceCollection() throws TimeoutException {
        // Actual trace
        TestCase testCase = configuration.getTestCase();
        MicrobatConfig microbatConfig = configuration.getMicrobatConfig();
        MicrobatConfig updatedMicrobatConfig = microbatConfig.setExpectedSteps(precheckExecutionResult.getTotalSteps());
        updatedMicrobatConfig = updatedMicrobatConfig.
                setDumpFilePath(configuration.getDumpFilePathConfig().getTraceFilePath());
        projectExecutor = projectExecutor.setMicrobatConfig(updatedMicrobatConfig);
        TraceCollectionResult result = projectExecutor.exec(testCase, configuration.isToDeleteTraceFile(),
                configuration.getInstrumentationTimeout());
        System.out.println("Normal trace done");

        MicrobatConfig updatedMutationMicrobatConfig = microbatConfig.setExpectedSteps(mutatedPrecheckExecutionResult.getTotalSteps());
        updatedMutationMicrobatConfig = updatedMutationMicrobatConfig.
                setDumpFilePath(configuration.getDumpFilePathConfig().getMutatedTraceFilePath());
        mutatedProjectExecutor = mutatedProjectExecutor.setMicrobatConfig(updatedMutationMicrobatConfig);
        TraceCollectionResult mutatedResult = mutatedProjectExecutor.exec(testCase, configuration.isToDeleteTraceFile(),
                configuration.getInstrumentationTimeout());
        System.out.println("Mutated trace done");

        // Trace with assertions to get output of test case
        MicrobatConfig includeAssertionsMicrobatConfig = addAssertionsToMicrobatConfig(updatedMicrobatConfig);
        includeAssertionsMicrobatConfig =
                includeAssertionsMicrobatConfig.setDumpFilePath(
                        configuration.getDumpFilePathConfig().getTraceWithAssertsFilePath());
        projectExecutor = projectExecutor.setMicrobatConfig(includeAssertionsMicrobatConfig);
        TraceCollectionResult originalResultWithAssertionsInTrace = projectExecutor.exec(testCase,
                configuration.isToDeleteTraceFile(), configuration.getInstrumentationTimeout());

        MicrobatConfig includeAssertionsMutationMicrobatConfig =
                addAssertionsToMicrobatConfig(updatedMutationMicrobatConfig);
        includeAssertionsMutationMicrobatConfig =
                includeAssertionsMutationMicrobatConfig.setDumpFilePath(
                        configuration.getDumpFilePathConfig().getMutatedTraceWithAssertsFilePath()
                );
        mutatedProjectExecutor = mutatedProjectExecutor.setMicrobatConfig(includeAssertionsMutationMicrobatConfig);
        TraceCollectionResult mutatedResultWithAssertionsInTrace = mutatedProjectExecutor.exec(testCase,
                configuration.isToDeleteTraceFile(), configuration.getInstrumentationTimeout());

        Trace mutatedTrace = mutatedResult.getTrace();
        List<TraceNode> rootCauses = TraceHelper.getMutatedTraceNodes(mutatedTrace, configuration.getMutator().getMutationHistory());

        boolean wasSuccessful = mutatedPrecheckExecutionResult.testCasePassed();


        return new MutationFrameworkResult(result.getInstrumentationResult(),
                mutatedResult.getInstrumentationResult(), originalResultWithAssertionsInTrace.getInstrumentationResult(),
                mutatedResultWithAssertionsInTrace.getInstrumentationResult(),
                configuration.getMutator().getMutationHistory(), project, mutatedProject, rootCauses,
                wasSuccessful, testCase);
    }

    public static class MutationFrameworkBuilder {
        private MutationFrameworkConfig mutationFrameworkConfig;

        public MutationFrameworkBuilder(MutationFrameworkConfig mutationFrameworkConfig) {
            this.mutationFrameworkConfig = mutationFrameworkConfig;
        }

        public MutationFramework build() {
            ProjectConfig projectConfig = new ProjectConfig(mutationFrameworkConfig.getProjectPath(),
                    mutationFrameworkConfig.getDropInsPath());
            return new MutationFramework(mutationFrameworkConfig, projectConfig);
        }
    }
}
