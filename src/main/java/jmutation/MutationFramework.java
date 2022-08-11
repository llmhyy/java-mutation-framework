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
import jmutation.model.microbat.PrecheckResult;
import jmutation.mutation.Mutator;
import jmutation.mutation.parser.MutationParser;
import jmutation.utils.TraceHelper;
import microbat.model.trace.Trace;
import microbat.model.trace.TraceNode;

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

        System.out.println(testCase);
        // Do precheck for normal + mutation to catch issues
        // If there are issues, set it in MutationResult, and return it
        // Otherwise, collect trace for normal + mutation, and return them in mutation result

        ProjectExecutor projectExecutor = new ProjectExecutor(microbatConfig, config);
        // Precheck
        PrecheckExecutionResult precheckExecutionResult = projectExecutor.execPrecheck(testCase);
        if (precheckExecutionResult.isOverLong()) {
            throw new RuntimeException("Precheck for test case " + testCase + " was over long");
        }
        System.out.println("Normal precheck done");

        Project clonedProject = proj.cloneToOtherPath();
        Project mutatedProject = mutator.mutate(precheckExecutionResult.getCoverage(), clonedProject);
        ProjectConfig mutatedProjConfig = new ProjectConfig(config, mutatedProject);

        ProjectExecutor mutatedProjectExecutor = new ProjectExecutor(microbatConfig, mutatedProjConfig);
        PrecheckExecutionResult mutatedPrecheckExecutionResult = mutatedProjectExecutor.execPrecheck(testCase);
        if (mutatedPrecheckExecutionResult.isOverLong()) {
            throw new RuntimeException("Precheck for mutated test case " + testCase + " was over long");
        }
        System.out.println("Mutated precheck done");

        // Actual trace
        ExecutionResult result = projectExecutor.exec(testCase);
        System.out.println("Normal trace done");
        ExecutionResult mutatedResult = mutatedProjectExecutor.exec(testCase);
        System.out.println("Mutated trace done");

        Trace mutatedTrace = mutatedResult.getCoverage().getTrace();
        List<TraceNode> rootCauses = TraceHelper.getMutatedTraceNodes(mutatedTrace, mutator.getMutationHistory());
        List<TestIO> testIOs = TraceHelper.getTestInputOutputs(mutatedTrace, testCase);
        MutationResult mutationResult = new MutationResult(result.getCoverage().getTrace(),
                mutatedTrace, mutator.getMutationHistory(), proj, mutatedProject, rootCauses,
                testIOs);

        return mutationResult;
    }
}
