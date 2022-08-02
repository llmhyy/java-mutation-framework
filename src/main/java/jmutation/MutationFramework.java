package jmutation;

import jmutation.execution.ProjectExecutor;
import jmutation.model.ExecutionResult;
import jmutation.model.MicrobatConfig;
import jmutation.model.MutationResult;
import jmutation.model.Project;
import jmutation.model.ProjectConfig;
import jmutation.model.TestCase;
import jmutation.mutation.Mutator;
import jmutation.mutation.parser.MutationParser;

import java.util.ArrayList;
import java.util.List;

// API class for other projects to call
public class MutationFramework {
    private String projectPath = "./sample/math70";
    private String dropInsDir = "./lib";

    private String microbatConfigPath;

    public void setProjectPath(String projectPath) {
        this.projectPath = projectPath;
    }

    public void setDropInsDir(String dropInsDir) {
        this.dropInsDir = dropInsDir;
    }

    public void setMicrobatConfigPath(String microbatConfigPath) {
        this.microbatConfigPath = microbatConfigPath;
    }

    public List<MutationResult> startMutationFramework() {
        ProjectConfig config = new ProjectConfig(projectPath, dropInsDir); // Contains class paths
        Project proj = config.getProject();
        List<TestCase> testList = proj.getTestCases();

        MicrobatConfig microbatConfig = microbatConfigPath == null ? MicrobatConfig.defaultConfig(projectPath) :
                MicrobatConfig.parse(microbatConfigPath, projectPath);

        Mutator mutator = new Mutator(new MutationParser());
        List<MutationResult> results = new ArrayList<>();
        for (TestCase test : testList) {
            System.out.println(test);
            ExecutionResult result = new ProjectExecutor(microbatConfig, config).exec(test);
            System.out.println("Normal trace done");

            Project clonedProject = proj.cloneToOtherPath();
            Project mutatedProject = mutator.mutate(result.getCoverage(), clonedProject);
            ProjectConfig mutatedProjConfig = new ProjectConfig(config, mutatedProject);

            ExecutionResult mutatedResult = new ProjectExecutor(microbatConfig, mutatedProjConfig).exec(test);
            System.out.println("Mutated trace done");
            MutationResult mutationResult = new MutationResult(result.getCoverage().getTrace(),
                    mutatedResult.getCoverage().getTrace(), mutator.getMutationHistory(), proj, mutatedProject);
            results.add(mutationResult);
            return results;
        }
        return results;
    }
}
