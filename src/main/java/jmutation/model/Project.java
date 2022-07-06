package jmutation.model;

import org.apache.commons.io.FileUtils;
import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * a representation of a project
 *
 * @author Yun Lin
 *
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

    public abstract String singleTestCommand(TestCase testCase);

    public abstract ProjectType projectType();
    public abstract String compileCommand();

    public abstract File getCompiledTestFolder();

    public abstract  File getCompiledClassFolder();

    public abstract File getCompiledFolder();

	public abstract Project cloneToOtherPath();

}
