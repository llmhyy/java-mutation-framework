package jmutation;

import jmutation.constants.ExternalLibrary;
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

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static jmutation.constants.ResourcesPath.DEFAULT_RESOURCES_PATH;

/**
 * API class for usage as an external library
 */
public class MutationFramework {
    private MutationFrameworkConfig configuration = new MutationFrameworkConfig();
    private ProjectConfig projectConfig;
    private PrecheckExecutionResult mutatedPrecheckExecutionResult = null; // Instead of storing, should pass it into the trace collection method as argument
    private ProjectExecutor mutatedProjectExecutor = null;
    private Project mutatedProject = null;
    private Project clonedProject;
    private ProjectConfig mutatedProjConfig;
    private PrecheckExecutionResult precheckExecutionResult; // Instead of storing, should pass it into the trace collection method as argument
    private ProjectExecutor projectExecutor;
    private Project proj;

    public void setConfig(MutationFrameworkConfig config) {
        configuration = config;
    }

    /**
     * Gets the test cases found in the project.
     *
     * @return A list of test case objects.
     */
    public List<TestCase> getTestCases() {
        projectConfig = new ProjectConfig(configuration.getProjectPath(), configuration.getDropInsPath()); // Contains class paths
        Project proj = projectConfig.getProject();
        return proj.getTestCases();
    }

    /**
     * Generates ProjectConfig for use by mutation framework.
     * This method should be called after updating the project path or drop ins directory.
     */
    public void generateProjectConfiguration() {
        projectConfig = new ProjectConfig(configuration.getProjectPath(), configuration.getDropInsPath()); // Contains class paths
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

    /**
     * Starts trace collection on the chosen test case, mutates the covered code, and runs trace collection on the mutated test case.
     *
     * @return MutationResult object.
     */
    public MutationFrameworkResult startMutationFramework() {
        configuration.getMutator().clearHistory();
        mutate();
        return runTraceCollection();
    }

    /**
     * @param command
     * @return
     */
    public MutationFrameworkResult startMutationFramework(MutationCommand command) {
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
        if (!setup()) return null;
        // Do precheck for normal + mutation to catch issues
        // If no issues, collect trace for normal + mutation, and return them in mutation result

        projectExecutor = new ProjectExecutor(configuration.getMicrobatConfig(), projectConfig);
        // Precheck
        precheckExecutionResult = executePrecheck(projectExecutor);
        System.out.println("Normal precheck done");

        if (configuration.isAutoSeed()) {
            runWithAutoSeed(proj, precheckExecutionResult);
        } else {
            runMutation(proj, precheckExecutionResult);
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
    public List<MutationCommand> analyse() {
        if (!setup()) return null;
        // Do precheck for normal + mutation to catch issues
        // If no issues, collect trace for normal + mutation, and return them in mutation result

        projectExecutor = new ProjectExecutor(configuration.getMicrobatConfig(), projectConfig);
        // Precheck
        precheckExecutionResult = executePrecheck(projectExecutor);
        System.out.println("Normal precheck done");
        return configuration.getMutator().analyse(precheckExecutionResult.getCoverage().getRanges(), proj);
    }

    /**
     * Execute mutation with mutation command
     *
     * @param command
     * @return
     */
    public MutationResult mutate(MutationCommand command) {
        if (!setup()) return null;
        runMutation(proj, command);

        System.out.println("Mutated precheck done");
        return new MutationResult(precheckExecutionResult, mutatedPrecheckExecutionResult, configuration.getTestCase(),
                configuration.getMutator().getMutationHistory());
    }

    public boolean setup() {
        if (projectConfig == null) {
            generateProjectConfiguration();
        }

        proj = projectConfig.getProject();
        return configuration.isReady();
    }

    private PrecheckExecutionResult executePrecheck(ProjectExecutor projectExecutor) {
        PrecheckExecutionResult precheckExecutionResult = projectExecutor.execPrecheck(configuration.getTestCase(),
                configuration.isToDeletePrecheckFile());
        if (precheckExecutionResult.isOverLong()) {
            throw new RuntimeException("Precheck for test case " + configuration.getTestCase() +
                    " was over long as step limit was " + configuration.getMicrobatConfig().getStepLimit() +
                    " but had " + precheckExecutionResult.getTotalSteps() + " steps");
        }
        return precheckExecutionResult;
    }

    private void runMutation(Project proj, PrecheckExecutionResult precheckExecutionResult) {
        if (configuration.getMutatedProjectPath().isEmpty()) {
            clonedProject = proj.cloneToOtherPath();
        } else {
            clonedProject = proj.cloneToOtherPath(configuration.getMutatedProjectPath());
        }
        mutatedProject = configuration.getMutator().mutate(precheckExecutionResult.getCoverage(), clonedProject);
        mutatedProjConfig = new ProjectConfig(projectConfig, mutatedProject);
        MicrobatConfig precheckMicrobatConfig = configuration.getMicrobatConfig();
        precheckMicrobatConfig = precheckMicrobatConfig.setDumpFilePath(
                configuration.getDumpFilePathConfig().getMutatedPrecheckFilePath());
        mutatedProjectExecutor = new ProjectExecutor(precheckMicrobatConfig, mutatedProjConfig);

        mutatedPrecheckExecutionResult = executePrecheck(mutatedProjectExecutor);
    }

    private void runMutation(Project proj, MutationCommand command) {
        if (configuration.getMutatedProjectPath().isEmpty()) {
            clonedProject = proj.cloneToOtherPath();
        } else {
            clonedProject = proj.cloneToOtherPath(configuration.getMutatedProjectPath());
        }
        mutatedProject = configuration.getMutator().mutate(command, clonedProject);
        mutatedProjConfig = new ProjectConfig(projectConfig, mutatedProject);

        try {
            File dumpFile = File.createTempFile("precheck", "exec");
            MicrobatConfig microbatConfig = configuration.getMicrobatConfig().setDumpFilePath(dumpFile.getAbsolutePath());
            mutatedProjectExecutor = new ProjectExecutor(microbatConfig, mutatedProjConfig);
            mutatedPrecheckExecutionResult = executePrecheck(mutatedProjectExecutor);
            dumpFile.delete();
        } catch (IOException e) {
            System.out.println(e);
        }
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

    private MutationFrameworkResult runTraceCollection() {
        // Actual trace
        TestCase testCase = configuration.getTestCase();
        MicrobatConfig microbatConfig = configuration.getMicrobatConfig();
        MicrobatConfig updatedMicrobatConfig = microbatConfig.setExpectedSteps(precheckExecutionResult.getTotalSteps());
        updatedMicrobatConfig = updatedMicrobatConfig.
                setDumpFilePath(configuration.getDumpFilePathConfig().getTraceFilePath());
        projectExecutor.setMicrobatConfig(updatedMicrobatConfig);
        TraceCollectionResult result = projectExecutor.exec(testCase, configuration.isToDeleteTraceFile());
        System.out.println("Normal trace done");

        MicrobatConfig updatedMutationMicrobatConfig = microbatConfig.setExpectedSteps(mutatedPrecheckExecutionResult.getTotalSteps());
        updatedMutationMicrobatConfig = updatedMutationMicrobatConfig.
                setDumpFilePath(configuration.getDumpFilePathConfig().getMutatedTraceFilePath());
        mutatedProjectExecutor.setMicrobatConfig(updatedMutationMicrobatConfig);
        TraceCollectionResult mutatedResult = mutatedProjectExecutor.exec(testCase, configuration.isToDeleteTraceFile());
        System.out.println("Mutated trace done");

        // Trace with assertions to get output of test case
        MicrobatConfig includeAssertionsMicrobatConfig = addAssertionsToMicrobatConfig(updatedMicrobatConfig);
        projectExecutor.setMicrobatConfig(includeAssertionsMicrobatConfig);
        TraceCollectionResult originalResultWithAssertionsInTrace = projectExecutor.exec(testCase);

        MicrobatConfig includeAssertionsMutationMicrobatConfig = addAssertionsToMicrobatConfig(updatedMutationMicrobatConfig);
        mutatedProjectExecutor.setMicrobatConfig(includeAssertionsMutationMicrobatConfig);
        TraceCollectionResult mutatedResultWithAssertionsInTrace = mutatedProjectExecutor.exec(testCase);

        Trace mutatedTrace = mutatedResult.getTrace();
        List<TraceNode> rootCauses = TraceHelper.getMutatedTraceNodes(mutatedTrace, configuration.getMutator().getMutationHistory());

        boolean wasSuccessful = mutatedPrecheckExecutionResult.testCasePassed();


        return new MutationFrameworkResult(result.getInstrumentationResult(),
                mutatedResult.getInstrumentationResult(), originalResultWithAssertionsInTrace.getInstrumentationResult(), mutatedResultWithAssertionsInTrace.getInstrumentationResult(),
                configuration.getMutator().getMutationHistory(), proj, mutatedProject, rootCauses,
                wasSuccessful, testCase);
    }
}
