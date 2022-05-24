package jmutation.model;

import java.io.File;
import java.util.List;

/**
 * a representation of a project
 * 
 * @author Yun Lin
 *
 */
public class Project {

    private final File root;
    private List<TestCase> testCases;

    public Project(File root, List<TestCase> testCases) {
        this.root = root;
        this.testCases = testCases;
    }

    public List<TestCase> getTestCases() {
        return this.testCases;
    }

}
