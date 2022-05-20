package jmutation.model;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * a representation of a project
 * 
 * @author Yun Lin
 *
 */
public class Project {

    private final File root;
    private List<File> textFiles;
    private List<File> testFiles;

    public Project(File root) {
        this.root = root;
        this.textFiles = new ArrayList<File>();
        this.walk(root.getAbsolutePath());
    }

    public List<TestCase> getTestCases() {
        // TODO
        // 2. use regminer jdt ast Visitor code from regminer to identify testcases
        return null;
    }

    private void walk(String path) {
        File start = new File(path);
        File[] list = start.listFiles();

        for (File f : list) {
            if (f.isDirectory()) {
                walk(f.getAbsolutePath());
            } else {
                textFiles.add(f.getAbsoluteFile());
                // TODO
                // 1. use detection regminer detection logic
            }
        }
    }

}
