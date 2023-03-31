package jmutation.model.project;

import jmutation.constants.ProjectType;
import jmutation.model.TestCase;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class Defects4jProject extends Project {
    private static final String COMPILE_FMT = "%s compile";
    private static final String MUTATION_FMT = "%s mutation";
    private static final String EXPORT_FMT = "%s export -p %s";
    private static final String CHECKOUT_FMT = "%s checkout -p %s -v %s -w %s";
    private static final String DEFECTS4J_CMD = "defects4j";

    public Defects4jProject(String name, File root, List<TestCase> testCases, String srcFolderPath, String testFolderPath, String compiledSrcFolderPath, String compiledTestFolderPath) {
        super(name, root, testCases, srcFolderPath, testFolderPath, compiledSrcFolderPath, compiledTestFolderPath);
    }

    public static String checkoutCommand(String project, String version, String path) {
        return String.format(CHECKOUT_FMT, DEFECTS4J_CMD, project, version, path);
    }

    @Override
    public ProjectType projectType() {
        return ProjectType.DEFECTS4J;
    }

    @Override
    public String compileCommand() {
        return String.format(COMPILE_FMT, DEFECTS4J_CMD);
    }

    @Override
    public String cleanCommand() {
        return null;
    }

    public String exportCommand(String properties) {
        return String.format(EXPORT_FMT, DEFECTS4J_CMD, properties);
    }

    @Override
    public String setupDependenciesCommand() {
        return null;
    }

    @Override
    public File getCompiledTestFolder() {
        return null;
    }

    @Override
    public File getCompiledClassFolder() {
        return null;
    }

    @Override
    public File getCompiledFolder() {
        return null;
    }

    @Override
    public Project cloneToOtherPath(String path) {
        File dest = new File(path);
        try {
            FileUtils.deleteDirectory(dest);
            FileUtils.copyDirectory(getRoot(), dest);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to clone project to " + dest.getAbsolutePath());
        }
        return new Defects4jProject(getProjectName(), dest, getTestCases(), srcFolderPath, testFolderPath,
                compileSrcFolderPath, compileTestFolderPath);
    }
}
