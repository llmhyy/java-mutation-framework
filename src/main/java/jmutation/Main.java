package jmutation;

import java.util.List;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import jmutation.model.*;
import jmutation.execution.ProjectExecutor;
import jmutation.mutation.Mutator;

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
	 * @param args
	 */
	public static void main(String[] args) {

		Main params = new Main();
		JCommander.newBuilder().addObject(params).build().parse(args);

		ProjectConfig config = new ProjectConfig(params.projectPath, params.dropInsDir);
		Project proj = config.getProject();
		List<TestCase> testList = proj.getTestCases();

		MicrobatConfig microbatConfig = MicrobatConfig.parse(params.microbatConfigPath);

		for(TestCase test: testList) {
			ExecutionResult result = new ProjectExecutor(microbatConfig, config).exec(test);
			System.out.println(result);

			Project mutatedProject = new Mutator().mutate(result.getCoverage(), proj);
//			new ProjectCompiler().compile(mutatedProject);
			ProjectConfig mutatedProjConfig = new ProjectConfig(config, mutatedProject);

			ExecutionResult mutatedResult = new ProjectExecutor(microbatConfig, mutatedProjConfig).exec(test);

			System.out.println(mutatedResult);

		}


	}

}
