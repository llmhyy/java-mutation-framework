package jmutation.model.project;

import jmutation.constants.ProjectType;
import jmutation.model.TestCase;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static jmutation.constants.GradleConstants.COMPILATION_FOLDER;
import static jmutation.constants.GradleConstants.GRADLE_CMD;

public class GradleProject extends Project {
    private static final String COMPILE_ALL_FMT = "%s compileTest";
    private static final String CLEAN_FMT = "%s clean";
    private static final String PACKAGE_ALL_FMT = "%s assemble";
    private static final String COMPILE_NO_RUN_TEST_FMT = "%s install -DskipTests";


    public GradleProject(String name, File root, List<TestCase> testCases,
                        String srcFolderPath, String testFolderPath,
                        String compiledSrcFolderPath, String compiledTestFolderPath) {
        super(name, root, testCases, srcFolderPath, testFolderPath, compiledSrcFolderPath, compiledTestFolderPath);
    }

    @Override
    public String singleTestCommand(TestCase testCase) {
        return String.format("%s test -Dtest=%s", GRADLE_CMD, testCase.qualifiedName());
    }

    @Override
    public String compileCommand() {
        return String.format(COMPILE_ALL_FMT, GRADLE_CMD);
    }

    @Override
    public String packageCommand() {
        return String.format(PACKAGE_ALL_FMT, GRADLE_CMD);
    }

    @Override
    public String cleanCommand() {
        return String.format(CLEAN_FMT, GRADLE_CMD);
    }

    @Override
    public File getSrcFolder() {
        return new File(getRoot(), srcFolderPath);
    }

    @Override
    public File getTestFolder() {
        return new File(getRoot(), testFolderPath);
    }

    @Override
    public ProjectType projectType() {
        return ProjectType.GRADLE;
    }

    @Override
    public File getCompiledTestFolder() {
        return new File(getRoot(), compileTestFolderPath);
    }

    @Override
    public File getCompiledClassFolder() {
        return new File(getRoot(), compileSrcFolderPath);
    }

    public File getCompiledFolder() {
        return new File(getRoot(), COMPILATION_FOLDER);
    }

    @Override
    public Project cloneToOtherPath() {
        String tmpdir = System.getProperty("java.io.tmpdir");
        File dest = new File(tmpdir + "/mutation");
        try {
            FileUtils.deleteDirectory(dest);
            FileUtils.copyDirectory(getRoot(), dest);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to clone project to " + dest.getAbsolutePath());
        }
        return new GradleProject(getProjectName(), dest, getTestCases(), srcFolderPath, testFolderPath,
                compileSrcFolderPath, compileTestFolderPath);
    }
}
