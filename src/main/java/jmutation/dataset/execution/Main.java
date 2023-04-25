package jmutation.dataset.execution;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import jmutation.MutationFramework;
import jmutation.MutationFramework.MutationFrameworkBuilder;
import jmutation.dataset.bug.model.BuggyProject;
import jmutation.dataset.bug.model.path.MutationFrameworkPathConfiguration;
import jmutation.dataset.execution.handler.BugCheckHandler;
import jmutation.model.PrecheckExecutionResult;
import jmutation.model.TestCase;
import jmutation.model.mutation.MutationFrameworkConfig;
import jmutation.model.mutation.MutationFrameworkConfig.MutationFrameworkConfigBuilder;
import jmutation.mutation.MutationCommand;
import jmutation.mutation.heuristic.HeuristicMutator;
import jmutation.mutation.heuristic.parser.StrongMutationParser;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static jmutation.dataset.bug.creator.BuggyProjectCreator.getStoredProjects;
import static jmutation.dataset.constants.FileNames.WORKING_PROJECT_DIR;


public class Main {
    @Parameter(names = "-timeout")
    private int instrumentationTimeout = 0;

    @Parameter(names = "-projectName", required = true)
    private String projectName;
    @Parameter(names = "-repoPath", required = true)
    private String repoPath;
    @Parameter(names = "-projectPath", required = true)
    private String projectPath;

    public static void main(String[] args) {
        Main params = new Main();
        JCommander.newBuilder().addObject(params).build().parse(args);
        runBugDataCollection(params.repoPath, params.projectName, params.projectPath, params.instrumentationTimeout);
    }

    private static void checkDone(String repoPath, String projectName, List<Integer> bugIds) {
        int count = 0;
        for (int bugId : bugIds) {
            if (new File(repoPath + File.separator + projectName + File.separator + bugId).exists()) {
                BugCheckHandler handler = new BugCheckHandler(repoPath, projectName, bugId);
                if (!handler.individualHandler(new Request(true))) {
                    System.out.println(count);
                    count++;
                }
            }
        }
        System.out.println(count);
    }

    /**
     * @param repoPath
     * @param projectName
     * @param originalProjectPath
     */
    private static void runBugDataCollection(String repoPath, String projectName, String originalProjectPath,
                                             int instrumentationTimeout) {
        MutationFrameworkPathConfiguration pathConfiguration = new MutationFrameworkPathConfiguration(repoPath);
        int numOfCores = 1;
        MutationFrameworkConfigBuilder configBuilder = new MutationFrameworkConfigBuilder();
        configBuilder.setProjectPath(originalProjectPath);
        configBuilder.setMutator(new HeuristicMutator(new StrongMutationParser()));
        MutationFrameworkConfig mutationFrameworkConfig = configBuilder.build();
        MutationFrameworkBuilder mutationFrameworkBuilder = new MutationFrameworkBuilder(mutationFrameworkConfig);
        MutationFramework mutationFramework = mutationFrameworkBuilder.build();
        String datasetPath = String.join(File.separator, repoPath, projectName);
        generateWorkingProject(originalProjectPath, String.join(File.separator, datasetPath, WORKING_PROJECT_DIR));
        ExecutorService executorService = Executors.newFixedThreadPool(numOfCores);
        List<TestCase> testCaseList = mutationFramework.getTestCases();
        JSONObject storedProjects = getStoredProjects(pathConfiguration.getStoragePath(projectName));
        int bugId = getLargestBugIdValue(repoPath, projectName) + 1;
        for (TestCase testCase : testCaseList) {
            mutationFramework.setTestCase(testCase);
            List<MutationCommand> commands;
            try {
                /**
                 * If we use multi-threading,
                 * rmb to set the precheck dump file to different location each time before running runPrecheck
                 */
                PrecheckExecutionResult precheckExecutionResult = mutationFramework.runPrecheck();
                if (!precheckExecutionResult.testCasePassed()) continue;
                commands = mutationFramework.analyse(precheckExecutionResult.getCoverage());
                for (int i = 0; i < commands.size(); i++) {
                    MutationCommand command = commands.get(i);
                    BuggyProject buggyProject = new BuggyProject(testCase, command, projectName);
                    if (checkBuggyProjectAlreadyCloned(storedProjects, buggyProject)) {
                        continue;
                    }
                    /**
                     * The number of buggy steps is not correct,
                     * but currently unsure of how to get it from the mutation handler
                     */
                    new DatasetWithoutTracesCreationRunner(repoPath, projectName, bugId, buggyProject,
                            pathConfiguration.getBugPath(projectName, String.valueOf(bugId)), originalProjectPath,
                            instrumentationTimeout).run();
                    bugId++;
                }
            } catch (RuntimeException | TimeoutException e) {
                e.printStackTrace();
            }
        }
        executorService.shutdown();
    }

    private static boolean checkBuggyProjectAlreadyCloned(JSONObject createdProjects, BuggyProject project) {
        try {
            createdProjects.get(project.key());
            return true;
        } catch (JSONException e) {
            return false;
        }
    }

    private static void generateWorkingProject(String projectPath, String fixPath) {
        File workingDir = new File(fixPath);
        try {
            FileUtils.deleteDirectory(workingDir);
            FileUtils.copyDirectory(new File(projectPath), workingDir);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to clone project to " + workingDir.getAbsolutePath());
        }
    }

    private static int getLargestBugIdValue(String repoPath, String projectName) {
        List<String> buggyDirs = Stream.of(new File(repoPath, projectName).listFiles())
                .filter(file -> !file.isDirectory())
                .map(File::getName)
                .collect(Collectors.toList());
        int maxBugId = 0;
        for (String buggyDir : buggyDirs) {
            int currBugId;
            try {
                currBugId = Integer.parseInt(FilenameUtils.removeExtension(buggyDir));
            } catch (NumberFormatException e) {
                e.printStackTrace();
                continue;
            }
            if (currBugId > maxBugId) maxBugId = currBugId;
        }
        return maxBugId;
    }

    private static void writeToFile(String str, String fileName) {
        try (FileWriter fileWriter = new FileWriter(fileName, true)) {
            fileWriter.append(str);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}