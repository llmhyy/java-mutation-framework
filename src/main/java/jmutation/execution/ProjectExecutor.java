package jmutation.execution;

import jmutation.model.Project;
import jmutation.model.TestCase;

public class ProjectExecutor extends Executor {
    private final Project project;
    public ProjectExecutor(Project proj) {
        super(proj.getRoot());
        this.project = proj;
    }

    /**
     * Compiles the entire project, including test classes
     * @return stdout as String
     */
    public String compile() {
        String cmd = project.compileCommand();
        return exec(cmd);
    }

    public ExecutionResult exec(TestCase testCase) {
        String out = compile();
        // include microbat details to instrument run
        InstrumentationBuilder ib = new InstrumentationBuilder();
        return new ExecutionResult(out);
    }
}
