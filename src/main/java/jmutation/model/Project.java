package jmutation.model;

import java.io.File;
import java.util.List;

/**
 * a representation of a project
 *
 * @author Yun Lin
 */
public abstract class Project {

    private final File root;
    private List<TestCase> testCases;

    public Project(File root, List<TestCase> testCases) {
        this.root = root;
        this.testCases = testCases;
    }

    public List<TestCase> getTestCases() {
        return this.testCases;
    }

    public File getRoot() {
        return this.root;
    }

    public abstract String getSrcPath();
    public abstract String getTestPath();

    public abstract String singleTestCommand(TestCase testCase);

    public abstract ProjectType projectType();

    public abstract String compileCommand();

    public abstract String packageCommand();

    public abstract File getCompiledTestFolder();

    public abstract File getCompiledClassFolder();

    public abstract File getCompiledFolder();

    public abstract Project cloneToOtherPath();

}
