package jmutation.model;

import jmutation.compile.execution.Executor;

import java.io.File;
import java.util.List;

public class MavenProject extends Project {
    private static final String COMPILE_ALL_FMT = "%s test-compile";
    private static final String COMPILE_NO_RUN_TEST_FMT = "%s install -DskipTests";
    private static final String COMPILATION_FOLDER = "target";
    private static final String TEST_CLASS_FOLDER = "test-classes";
    private static final String COMPILED_CLASS_FOLDER = "classes";
    public MavenProject(File root, List<TestCase> testCases) {
        super(root, testCases);
    }

    @Override
    public String singleTestCommand(TestCase testCase) {
        return String.format("mvn test -Dtest=%s", testCase.qualifiedName());
    }

    @Override
    public String compileCommand() {
        String mavenCmd = Executor.isWindows() ? "mvn" : "mvn.exe";
        return String.format(COMPILE_ALL_FMT, mavenCmd);
    }

    @Override
    public ProjectType projectType() {
        return ProjectType.MAVEN;
    }

    @Override
    public File getCompiledTestFolder() {
        return new File(getCompiledFolder(), TEST_CLASS_FOLDER);
    }

    @Override
    public File getCompiledClassFolder() {
        return new File(getCompiledFolder(), COMPILED_CLASS_FOLDER);
    }

    public File getCompiledFolder() {
        return new File(getRoot(), COMPILATION_FOLDER);
    }
}
