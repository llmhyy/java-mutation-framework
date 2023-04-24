package jmutation.dataset.bug.creator;

import jmutation.MutationFramework;
import jmutation.MutationFramework.MutationFrameworkBuilder;
import jmutation.dataset.bug.Log;
import jmutation.dataset.bug.model.BuggyProject;
import jmutation.model.mutation.DumpFilePathConfig;
import jmutation.model.mutation.MutationFrameworkConfig;
import jmutation.model.mutation.MutationFrameworkConfig.MutationFrameworkConfigBuilder;
import jmutation.model.mutation.MutationResult;
import jmutation.mutation.heuristic.HeuristicMutator;
import jmutation.utils.JSONWrapper;
import org.apache.commons.io.FileUtils;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

import static jmutation.dataset.constants.FileNames.BUGGY_PROJECT_DIR;

public class BuggyProjectCreator implements Callable<Boolean> {
    public static final String ROOTCAUSE_FILE_NAME = "rootcause.txt";
    public static final String TESTCASE_FILE_NAME = "testcase.txt";
    private static final Logger logger = Log.createLogger(BuggyProjectCreator.class);
    private static final Object storageFileLock = new Object();
    private final String repositoryPath;
    private final String projectPath;
    private final String storageFilePath;
    private final BuggyProject buggyProject;
    private final int bugId;
    private final int timeoutMinutes;

    public BuggyProjectCreator(String repositoryPath, String projectPath, BuggyProject buggyProject,
                               String storageFilePath, int bugId) {
        this.repositoryPath = repositoryPath;
        this.projectPath = projectPath;
        this.buggyProject = buggyProject;
        this.storageFilePath = storageFilePath;
        this.bugId = bugId;
        this.timeoutMinutes = 0;
    }

    public BuggyProjectCreator(String repositoryPath, String projectPath, BuggyProject buggyProject,
                               String storageFilePath, int bugId, int timeoutMinutes) {
        this.repositoryPath = repositoryPath;
        this.projectPath = projectPath;
        this.buggyProject = buggyProject;
        this.storageFilePath = storageFilePath;
        this.bugId = bugId;
        this.timeoutMinutes = timeoutMinutes;
    }

    private static void createFile(String contents, String pathToFile, String fileName) {
        File file = new File(pathToFile);
        file.mkdirs();
        try (FileWriter writer = new FileWriter(new File(file, fileName))) {
            writer.write(contents);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static JSONObject getStoredProjects(String pathToFile) {
        try {
            return JSONWrapper.getJSONObjectFromFile(pathToFile);
        } catch (RuntimeException e) {
            return new JSONObject();
        }
    }

    public Boolean call() {
        String mutatedProjPath = createMutatedProjectPath(bugId);
        MutationFrameworkConfig config = createMutationFrameworkConfig(mutatedProjPath);
        MutationFrameworkBuilder mutationFrameworkBuilder = new MutationFrameworkBuilder(config);
        MutationFramework mutationFramework = mutationFrameworkBuilder.build();
        mutationFramework.setTestCase(buggyProject.testCase());
        try {
            log(bugId, "Start mutating");
            MutationResult result = mutationFramework.mutate(buggyProject.command(), null);
            log(bugId, "Finish mutating");
            if (result.getMutatedPrecheckExecutionResult() == null || result.getMutatedPrecheckExecutionResult().testCasePassed()) {
                try {
                    log(bugId, "Test case passed or precheck is null, deleting", Level.WARNING);
                    FileUtils.deleteDirectory(new File(mutatedProjPath));
                    log(bugId, "Test case passed or precheck is null, deleted", Level.WARNING);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return false;
            }
            log(bugId, "Test case failed, creating testcase and rootcause files");
            createFile(buggyProject.testCase().toString(), mutatedProjPath, TESTCASE_FILE_NAME);
            createFile(buggyProject.command().toString(), mutatedProjPath, ROOTCAUSE_FILE_NAME);
            log(bugId, "Created testcase and rootcause files. Writing to storage file.");
            writeToStorageFile();
            log(bugId, "Written to storage file");
        } catch (RuntimeException | TimeoutException e) {
            e.printStackTrace();
            return false;
        }
        log(bugId, "Done");
        return true;
    }

    /**
     * Writes to json file that this project, test case and command combination is already stored
     */
    private void writeToStorageFile() {
        synchronized (storageFileLock) {
            logger.log(Level.INFO, Thread.currentThread().getName() + " : writing to json file");
            JSONObject storedJSON = getStoredProjects(storageFilePath);
            storedJSON.put(buggyProject.key(), buggyProject.createJSONObject());
            try (FileWriter writer = new FileWriter(storageFilePath)) {
                writer.write(storedJSON.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void log(int bugId, String msg) {
        logger.info(bugId + ": " + msg);
    }

    private void log(int bugId, String msg, Level level) {
        logger.log(level, bugId + ": " + msg);
    }

    MutationFrameworkConfig createMutationFrameworkConfig(String mutatedProjPathStr) {
        StringBuilder mutatedProjPath = new StringBuilder(mutatedProjPathStr);
        MutationFrameworkConfigBuilder configBuilder = new MutationFrameworkConfigBuilder();
        configBuilder.setProjectPath(projectPath);
        configBuilder.setMutator(new HeuristicMutator());
        DumpFilePathConfig dumpFilePathConfig = createDumpFilePathConfig(mutatedProjPath.toString());
        configBuilder.setDumpFilePathConfig(dumpFilePathConfig);
        int mutatedBugPathLen = mutatedProjPath.length();
        mutatedProjPath.append(File.separator);
        mutatedProjPath.append(BUGGY_PROJECT_DIR);
        configBuilder.setMutatedProjectPath(mutatedProjPath.toString());
        mutatedProjPath.delete(mutatedBugPathLen, mutatedBugPathLen + 3);
        configBuilder.setInstrumentationTimeout(timeoutMinutes);
        return configBuilder.build();
    }

    DumpFilePathConfig createDumpFilePathConfig(String mutatedProjPath) {
        String mutatedProjPathWithSeparator = mutatedProjPath + File.separator;
        DumpFilePathConfig dumpFilePathConfig = new DumpFilePathConfig();
        dumpFilePathConfig.setMutatedPrecheckFilePath(mutatedProjPathWithSeparator +
                DumpFilePathConfig.DEFAULT_BUGGY_PRECHECK_FILE);
        dumpFilePathConfig.setMutatedTraceFilePath(mutatedProjPathWithSeparator +
                DumpFilePathConfig.DEFAULT_BUGGY_TRACE_FILE);
        dumpFilePathConfig.setTraceFilePath(mutatedProjPathWithSeparator +
                DumpFilePathConfig.DEFAULT_TRACE_FILE);
        dumpFilePathConfig.setPrecheckFilePath(mutatedProjPathWithSeparator +
                DumpFilePathConfig.DEFAULT_PRECHECK_FILE);
        return dumpFilePathConfig;
    }

    private String createMutatedProjectPath(int currBugId) {
        StringBuilder mutatedProjPath = new StringBuilder(repositoryPath + File.separator +
                buggyProject.projectName() + File.separator);
        mutatedProjPath.append(currBugId);
        return mutatedProjPath.toString();
    }
}
