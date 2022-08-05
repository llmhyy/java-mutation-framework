package jmutation;

import jmutation.execution.ProjectExecutor;
import jmutation.model.ExecutionResult;
import jmutation.model.MicrobatConfig;
import jmutation.model.MutationResult;
import jmutation.model.Project;
import jmutation.model.ProjectConfig;
import jmutation.model.TestCase;
import jmutation.model.TestIO;
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
        ExecutionResult result = new ProjectExecutor(microbatConfig, config).exec(testCase);
        System.out.println("Normal trace done");

        Project clonedProject = proj.cloneToOtherPath();
        Project mutatedProject = mutator.mutate(result.getCoverage(), clonedProject);
        ProjectConfig mutatedProjConfig = new ProjectConfig(config, mutatedProject);

        ExecutionResult mutatedResult = new ProjectExecutor(microbatConfig, mutatedProjConfig).exec(testCase);
        System.out.println("Mutated trace done");

        Trace mutatedTrace = mutatedResult.getCoverage().getTrace();
        List<TraceNode> rootCauses = TraceHelper.getMutatedTraceNodes(mutatedTrace, mutator.getMutationHistory());
        List<TestIO> testIOs = TraceHelper.getTestInputOutputs(mutatedTrace, testCase);
        MutationResult mutationResult = new MutationResult(result.getCoverage().getTrace(),
                mutatedResult.getCoverage().getTrace(), mutator.getMutationHistory(), proj, mutatedProject, rootCauses,
                testIOs);

        return mutationResult;
    }
}
