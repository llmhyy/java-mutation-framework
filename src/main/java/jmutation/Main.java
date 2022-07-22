package jmutation;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import jmutation.execution.ProjectExecutor;
import jmutation.model.ExecutionResult;
import jmutation.model.MicrobatConfig;
import jmutation.model.Project;
import jmutation.model.ProjectConfig;
import jmutation.model.TestCase;
import jmutation.mutation.Mutator;
import jmutation.mutation.parser.MutationParser;

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

        String projectPath = params.projectPath;
        ProjectConfig config = new ProjectConfig(projectPath, params.dropInsDir); // Contains class paths
        Project proj = config.getProject();
        List<TestCase> testList = proj.getTestCases();

        MicrobatConfig microbatConfig = params.microbatConfigPath == null ? MicrobatConfig.defaultConfig(projectPath) : MicrobatConfig.parse(params.microbatConfigPath, projectPath);

        Mutator mutator = new Mutator(new MutationParser());
        for (TestCase test : testList) {
            System.out.println(test);
            ExecutionResult result = new ProjectExecutor(microbatConfig, config).exec(test);
            System.out.println("Normal trace done");

            Project mutatedProject = mutator.mutate(result.getCoverage(), proj);
            ProjectConfig mutatedProjConfig = new ProjectConfig(config, mutatedProject);

            ExecutionResult mutatedResult = new ProjectExecutor(microbatConfig, mutatedProjConfig).exec(test);
            System.out.println("Mutated trace done");
        }
    }
}
