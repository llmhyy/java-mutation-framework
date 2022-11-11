package jmutation.dataset;

import jmutation.MutationFramework;
import jmutation.model.TestCase;
import jmutation.mutation.MutationCommand;
import jmutation.mutation.heuristic.HeuristicMutator;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DatasetCreator {
    public static int bugId = 0;

    public static void main(String[] args) {
        String projectPath = String.join(File.separator, "sample", "math_70");
        String repoPath = "C:\\Users\\bchenghi\\Desktop";
        run(projectPath, repoPath);
    }

    public static void run(String projectPath, String repositoryPath) {
        // Multi threading issues.
        // Rename precheck + instrumentation files with random values, and delete after reading.
        // Copy from microbat
        // Create a new thread for each test case + command pair
        String projectName = projectPath.substring(projectPath.lastIndexOf(File.separator) + 1);
        File fixPath = new File(String.join(File.separator, repositoryPath, projectName, "fix"));
        try {
            FileUtils.deleteDirectory(fixPath);
            FileUtils.copyDirectory(new File(projectPath), fixPath);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to clone project to " + fixPath.getAbsolutePath());
        }
        int numOfCores = Runtime.getRuntime().availableProcessors();
        ExecutorService executorService = Executors.newFixedThreadPool(numOfCores);
        MutationFramework mutationFramework = new MutationFramework();
        mutationFramework.setProjectPath(projectPath);
        mutationFramework.setMutator(new HeuristicMutator());
        List<TestCase> testCaseList = mutationFramework.getTestCases();
        for (TestCase testCase : testCaseList) {
            mutationFramework.setTestCase(testCase);
            List<MutationCommand> commands;
            try {
                commands = mutationFramework.analyse();
            } catch (RuntimeException e) {
                continue;
            }
            for (MutationCommand command : commands) {
                executorService.submit(new BuggyProjectCreator(repositoryPath, projectPath, projectName, testCase, command));
            }
        }
    }
}
