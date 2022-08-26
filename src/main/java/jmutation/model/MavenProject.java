package jmutation.model;

import jmutation.constants.ProjectType;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static jmutation.constants.MavenConstants.COMPILATION_FOLDER;

public class MavenProject extends Project {
    private static final String COMPILE_ALL_FMT = "%s test-compile";
    private static final String CLEAN_FMT = "%s clean";
    private static final String PACKAGE_ALL_FMT = "%s package";
    private static final String COMPILE_NO_RUN_TEST_FMT = "%s install -DskipTests";

    public MavenProject(String name, File root, List<TestCase> testCases,
                        String srcFolderPath, String testFolderPath,
                        String compiledSrcFolderPath, String compiledTestFolderPath) {
        super(name, root, testCases, srcFolderPath, testFolderPath, compiledSrcFolderPath, compiledTestFolderPath);
    }

    public MavenProject(String name, File root, List<TestCase> testCases,
                   File srcFolder, File testFolder,
                   File compiledSrcFolder, File compiledTestFolder) {
        super(name, root, testCases, srcFolder, testFolder, compiledSrcFolder, compiledTestFolder);
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
    public String cleanCommand() {
        String mavenCmd = "mvn";
        return String.format(CLEAN_FMT, mavenCmd);
    }

    @Override
    public File getSrcFolder() {
        return srcFolder;
    }

    @Override
    public File getTestFolder() {
        return testFolder;
    }

    @Override
    public ProjectType projectType() {
        return ProjectType.MAVEN;
    }

    @Override
    public File getCompiledTestFolder() {
        return compiledTestFolder;
    }

    @Override
    public File getCompiledClassFolder() {
        return compiledSrcFolder;
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
            return new MavenProject(getProjectName(), dest, getTestCases(), getSrcFolder(), getTestFolder(),
                    getCompiledClassFolder(), getCompiledTestFolder());
        }
    }
}
