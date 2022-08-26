package jmutation.model;

import jmutation.constants.ProjectType;

import java.io.File;
import java.util.List;

/**
 * a representation of a project
 *
 * @author Yun Lin
 */
public abstract class Project {

    private final String name;

    private final File root;
    private List<TestCase> testCases;

    protected final File srcFolder;

    protected final File testFolder;

    protected final File compiledSrcFolder;

    protected final File compiledTestFolder;

    public Project(String name, File root, List<TestCase> testCases,
                   File srcFolder, File testFolder,
                   File compiledSrcFolder, File compiledTestFolder) {
        this.name = name;
        this.root = root;
        this.testCases = testCases;
        this.srcFolder = srcFolder;
        this.testFolder = testFolder;
        this.compiledSrcFolder = compiledSrcFolder;
        this.compiledTestFolder = compiledTestFolder;
    }

    public Project(String name, File root, List<TestCase> testCases,
                   String srcFolderPath, String testFolderPath,
                   String compiledSrcFolderPath, String compiledTestFolderPath) {
        this.name = name;
        this.root = root;
        this.testCases = testCases;
        this.srcFolder = new File(srcFolderPath);
        this.testFolder = new File(testFolderPath);
        this.compiledSrcFolder = new File(compiledSrcFolderPath);
        this.compiledTestFolder = new File(compiledTestFolderPath);
    }

    public List<TestCase> getTestCases() {
        return this.testCases;
    }

    public File getRoot() {
        return this.root;
    }

    public String getProjectName() {
        return name;
    }

    public abstract File getSrcFolder();
    public abstract File getTestFolder();

    public abstract String singleTestCommand(TestCase testCase);

    public abstract ProjectType projectType();

    public abstract String compileCommand();

    public abstract String packageCommand();

    public abstract String cleanCommand();

    public abstract File getCompiledTestFolder();

    public abstract File getCompiledClassFolder();

    public abstract File getCompiledFolder();

    public abstract Project cloneToOtherPath();

}
