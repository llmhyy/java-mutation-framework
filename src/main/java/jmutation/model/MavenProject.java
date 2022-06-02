package jmutation.model;

import jmutation.execution.Executor;

import java.io.File;
import java.util.List;

public class MavenProject extends Project {
    private static final String COMPILE_CMD_FMT = "%s compile test-compile";
    public MavenProject(File root, List<TestCase> testCases) {
        super(root, testCases);
    }

    @Override
    public String singleTestCommand(TestCase testCase) {
        return String.format("mvn test -Dtest=%s", testCase.qualifiedName());
    }

    @Override
    public String compileCommand() {
        String mavenCmd = Executor.isWindows() ? "mvn.exe" : "mvn";
        return String.format(COMPILE_CMD_FMT, mavenCmd);
    }

    @Override
    public ProjectType projectType() {
        return ProjectType.MAVEN;
    }
}
