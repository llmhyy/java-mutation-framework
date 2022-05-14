package jmutation;

import java.util.List;

import jmutation.compile.ProjectCompiler;
import jmutation.execution.ExecutionResult;
import jmutation.execution.ProjectExecutor;
import jmutation.execution.TestCase;
import jmutation.mutation.Mutator;
import jmutation.parser.Project;
import jmutation.parser.ProjectParser;
import jmutation.testselection.TestCaseFinder;

public class Main {
	
	class Parameters{
		private String projectPath;
		
	}
	
	
	public static void main(String[] args) {
		
		Parameters params = parse(args);
		
		ProjectParser parser = new ProjectParser();
		Project proj = parser.parse(params.projectPath);
		
		new ProjectCompiler().compile(proj);
		
		List<TestCase> testList = new TestCaseFinder().findTestCases();
		
		for(TestCase test: testList) {
			ExecutionResult result = new ProjectExecutor().run(test, proj);
			
			Project mutatedProject = new Mutator().mutate(result.getCoverage(), proj);
			new ProjectCompiler().compile(mutatedProject);
			
			ExecutionResult mutatedResult = new ProjectExecutor().run(test, mutatedProject);
			
			System.out.println(mutatedResult);
			
		}
		
		
	}


	private static Parameters parse(String[] args) {
		// TODO Yuchen
		return null;
	}
	
	
}
