package jmutation.model;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class MavenProject extends Project {
    private static final String COMPILE_ALL_FMT = "%s test-compile";
    private static final String PACKAGE_ALL_FMT = "%s package";
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
        String mavenCmd = "mvn";
        return String.format(COMPILE_ALL_FMT, mavenCmd);
    }

    @Override
    public String packageCommand() {
        String mavenCmd = "mvn";
        return String.format(PACKAGE_ALL_FMT, mavenCmd);
    }

    @Override
    public String getSrcPath() {
        return getRoot().getAbsolutePath() + "src/main/java";
    }

    @Override
    public String getTestPath() {
        return getRoot().getAbsolutePath() + "src/main/test";
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

    @Override
    public Project cloneToOtherPath() {
        String tmpdir = System.getProperty("java.io.tmpdir");
        File dest = new File(tmpdir + "/mutation");
        try {
            FileUtils.copyDirectory(getRoot(), dest);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to clone project to " + dest.getAbsolutePath());
        } finally {
            return new MavenProject(dest, getTestCases());
        }
    }
}
