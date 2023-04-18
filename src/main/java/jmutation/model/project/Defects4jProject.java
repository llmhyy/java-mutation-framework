package jmutation.model.project;

import jmutation.constants.ProjectType;
import jmutation.model.TestCase;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class Defects4jProject extends Project {
    private static final String COMPILE_FMT = "%s compile";
    private static final String MUTATION_FMT = "%s mutation";
    private static final String EXPORT_FMT = "%s export -p %s";
    private static final String CHECKOUT_FMT = "%s checkout -p %s -v %s -w %s";
    private static final String TEST_FMT = "%s test";
    private static final String DEFECTS4J_CMD = "defects4j";

    public Defects4jProject(String name, File root, List<TestCase> testCases, String srcFolderPath,
                            String testFolderPath, String compiledSrcFolderPath, String compiledTestFolderPath) {
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

    public static String exportCommand(String property) {
        return String.format(EXPORT_FMT, DEFECTS4J_CMD, property);
    }

    public String testCommand() {
        return String.format(TEST_FMT, DEFECTS4J_CMD);
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

    public String[] getFailingTests() throws IOException {
        String failingTests = Files.readString(getRoot().toPath().resolve("failing_tests"));
        if (failingTests.isEmpty()) {
            return new String[] {};
        }
        String[] lines = failingTests.split(System.lineSeparator());
        String startOfTestName = "--- ";
        List<String> result = new ArrayList<>();
        for (String line : lines) {
            if (line.startsWith(startOfTestName)) {
                int nameStartIdx = line.indexOf(startOfTestName) + startOfTestName.length();
                result.add(line.substring(nameStartIdx));
            }
        }
        String[] arrayResult = new String[result.size()];
        for (int i = 0; i < result.size(); i++) {
            arrayResult[i] = result.get(i);
        }
        return arrayResult;
    }

    public String[] getAllTests() throws IOException {
        String tests = Files.readString(getRoot().toPath().resolve("all_tests"));
        return tests.split(System.lineSeparator());
    }

    public MutationAnalysis getMutationAnalysis() throws IOException {
        return null;
    }

    public String mutationCommand() {
        return String.format(MUTATION_FMT, DEFECTS4J_CMD);
    }
    public static class MutationAnalysis {
        private final int executedMutations;
        private final int survivedMutations;
        private final int killedMutations;

        public MutationAnalysis(int executedMutations, int survivedMutations, int killedMutations) {
            this.executedMutations = executedMutations;
            this.survivedMutations = survivedMutations;
            this.killedMutations = killedMutations;
        }

        public int getExecutedMutations() {
            return executedMutations;
        }

        public int getSurvivedMutations() {
            return survivedMutations;
        }

        public int getKilledMutations() {
            return killedMutations;
        }
    }
}
