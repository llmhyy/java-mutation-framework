package jmutation.dataset;

import jmutation.MutationFramework;
import jmutation.model.TestCase;
import jmutation.model.mutation.MutationFrameworkConfig;
import jmutation.mutation.MutationCommand;
import jmutation.utils.JSONWrapper;
import org.apache.commons.io.FileUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DatasetCreator {
    public static final String BUGGY_PROJECT_DIR = "bug";
    public static final String WORKING_PROJECT_DIR = "fix";
    private static final String CREATED_BUGGY_PROJECT_FILE = "createdBugs.json";
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
        String datasetPath = String.join(File.separator, repositoryPath, projectName);
        File fixPath = new File(String.join(File.separator, datasetPath, WORKING_PROJECT_DIR));
        try {
            FileUtils.deleteDirectory(fixPath);
            FileUtils.copyDirectory(new File(projectPath), fixPath);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to clone project to " + fixPath.getAbsolutePath());
        }
        int numOfCores = Runtime.getRuntime().availableProcessors() - 1;
        ExecutorService executorService = Executors.newFixedThreadPool(numOfCores);
        MutationFrameworkConfig mutationFrameworkConfig = new MutationFrameworkConfig();
        mutationFrameworkConfig.setProjectPath(projectPath);
        MutationFramework mutationFramework = new MutationFramework();
        mutationFramework.setConfig(mutationFrameworkConfig);
        List<TestCase> testCaseList = mutationFramework.getTestCases();
        String createdBugsFilePath = String.join(File.separator, datasetPath, CREATED_BUGGY_PROJECT_FILE);
        JSONObject storedProjects = getStoredProjects(createdBugsFilePath);
        bugId = storedProjects.length();
        for (TestCase testCase : testCaseList) {
            mutationFrameworkConfig.setTestCase(testCase);
            List<MutationCommand> commands;
            try {
                commands = mutationFramework.analyse();
            } catch (RuntimeException e) {
                continue;
            }
            for (int i = 0; i < commands.size() && i < 7; i++) {
                MutationCommand command = commands.get(i);
                BuggyProject buggyProject = new BuggyProject(testCase, command, projectName);
                if (checkBuggyProjectAlreadyCloned(storedProjects, buggyProject)) {
                    continue;
                }
                executorService.submit(new BuggyProjectCreator(repositoryPath, projectPath, buggyProject,
                        createdBugsFilePath));
            }
        }
    }

    public static JSONObject getStoredProjects(String pathToFile) {
        /**
         * {
         * "some hash or identifier, up to buggy project obj" : {
         *     the fields in string version
         * }
         * }
         */
        try {
            return JSONWrapper.getJSONObjectFromFile(pathToFile);
        } catch (RuntimeException e) {
            return new JSONObject();
        }
    }

    private static boolean checkBuggyProjectAlreadyCloned(JSONObject createdProjects, BuggyProject project) {
        try {
            createdProjects.get(project.key());
            return true;
        } catch (JSONException e) {
            return false;
        }
    }
}
