package jmutation.model;

import jmutation.compile.ProjectCompiler;

import javax.sound.midi.SysexMessage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
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
    private List<TestCase> testCases;

    public Project(File root) {
        this.root = root;
        this.textFiles = new ArrayList<>();
        this.testCases = new ArrayList<>();
        this.walk(root);
    }

    public List<TestCase> getTestCases() {
        // 2. use regminer jdt ast Visitor code from regminer to identify testcases
        return this.testCases;
    }

    private void walk(File start) {
        File[] list = start.listFiles();

        for (File f : list) {
            if (f.isDirectory()) {
                walk(f);
            } else {
                textFiles.add(f.getAbsoluteFile());
                // 1. use detection regminer detection logic
                try {
                    String fileContent = Files.readString(f.toPath());
                    if (isTestSuite(fileContent)) {
                        // get walk code and retrieve all methods
                        System.out.println(fileContent);
                        testCases.addAll(ProjectCompiler.getAllMethod(fileContent));
                    }
                } catch (IOException e) {
                    System.out.print("Unable to open file at ");
                    System.out.println(f.getAbsolutePath());
                }
            }
        }
    }

    private boolean isTestSuite(String code) {
        return code.contains("junit") || code.contains("@Test");
    }
}
