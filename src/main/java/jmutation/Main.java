package jmutation;

import java.util.List;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import jmutation.execution.ExecutionResult;
import jmutation.execution.ProjectExecutor;
import jmutation.model.Project;
import jmutation.model.ProjectConfig;
import jmutation.model.TestCase;
import jmutation.mutation.Mutator;
import jmutation.parser.ProjectParser;

public class Main {
	@Parameter(names = "-projectPath", description = "Path to project directory", required = true)
	private String projectPath;

	@Parameter(names = "-project", description = "Maven or Gradle")
	private String projectType;

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

		ProjectConfig config = new ProjectConfig(params.projectPath);
		ProjectParser parser = new ProjectParser(config);
		Project proj = parser.parse();

		List<TestCase> testList = proj.getTestCases();

		for(TestCase test: testList) {
			ExecutionResult result = new ProjectExecutor(proj).exec(test);
			System.out.println(result);

			Project mutatedProject = new Mutator().mutate(result.getCoverage(), proj);
//			new ProjectCompiler().compile(mutatedProject);

			ExecutionResult mutatedResult = new ProjectExecutor(mutatedProject).exec(test);

			System.out.println(mutatedResult);

		}


	}

}
