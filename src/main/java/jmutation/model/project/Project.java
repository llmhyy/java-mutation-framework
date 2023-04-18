package jmutation.model.project;

import jmutation.constants.ProjectType;
import jmutation.model.TestCase;

import java.io.File;
import java.util.List;

/**
 * a representation of a project
 *
 * @author Yun Lin
 */

/**
 * TODO: This interface does not consider defects4j, and refactoring is necessary
 */
public abstract class Project {

    private final String name;

    private final File root;
    private final List<TestCase> testCases;
    protected String compileSrcFolderPath;
    protected String compileTestFolderPath;
    protected String srcFolderPath;
    protected String testFolderPath;
    protected List<File> externalLibs;


    public Project(String name, File root, List<TestCase> testCases,
                   String srcFolderPath, String testFolderPath,
                   String compiledSrcFolderPath, String compiledTestFolderPath) {
        this.name = name;
        this.root = root;
        this.testCases = testCases;
        this.srcFolderPath = srcFolderPath;
        this.testFolderPath = testFolderPath;
        this.compileSrcFolderPath = compiledSrcFolderPath;
        this.compileTestFolderPath = compiledTestFolderPath;
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

    public abstract ProjectType projectType();

    public abstract String compileCommand();

    public abstract String cleanCommand();

    public abstract String setupDependenciesCommand();

    public abstract File getCompiledTestFolder();

    public abstract File getCompiledClassFolder();

    public abstract File getCompiledFolder();


    public abstract Project cloneToOtherPath(String path);

    public String getSrcFolderPath() {
        return srcFolderPath;
    }

    public String getTestFolderPath() {
        return testFolderPath;
    }
}
