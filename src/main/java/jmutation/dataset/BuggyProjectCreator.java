package jmutation.dataset;

import jmutation.MutationFramework;
import jmutation.model.MutationResult;
import jmutation.mutation.heuristic.HeuristicMutator;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import static jmutation.dataset.DatasetCreator.bugId;

public class BuggyProjectCreator implements Runnable {
    private static final Object lock = new Object();
    private static final Object storageFileLock = new Object();
    private final String repositoryPath;
    private final String projectPath;
    private final String storageFilePath;
    private final BuggyProject buggyProject;

    public BuggyProjectCreator(String repositoryPath, String projectPath, BuggyProject buggyProject, String storageFilePath) {
        this.repositoryPath = repositoryPath;
        this.projectPath = projectPath;
        this.buggyProject = buggyProject;
        this.storageFilePath = storageFilePath;
    }

    private static void createFile(String contents, String pathToFile, String fileName) {
        File file = new File(pathToFile);
        file.mkdirs();
        try (FileWriter writer = new FileWriter(new File(file, fileName))) {
            writer.write(contents);
        } catch (IOException e) {
            System.out.println(e);
        }
    }

    public void run() {
        MutationFramework mutationFramework = new MutationFramework();
        mutationFramework.setProjectPath(projectPath);
        mutationFramework.setMutator(new HeuristicMutator());
        StringBuilder mutatedProjPath = new StringBuilder(repositoryPath + File.separator + buggyProject.getProjectName() + File.separator);
        int mutatedProjPathLen = mutatedProjPath.length();
        int currBugId = increaseAndGetBugId();
        mutatedProjPath.append(currBugId);
        mutatedProjPath.append(File.separator);
        int mutatedBugPathLen = mutatedProjPath.length();
        mutatedProjPath.append(DatasetCreator.BUGGY_PROJECT_DIR);
        mutationFramework.setMutatedProjectPath(mutatedProjPath.toString());
        mutatedProjPath.delete(mutatedBugPathLen, mutatedBugPathLen + 3);
        mutationFramework.setTestCase(buggyProject.getTestCase());
        try {
            MutationResult result = mutationFramework.mutate(buggyProject.getCommand());
            if (result.getMutatedPrecheckExecutionResult().testCasePassed()) {
                mutatedProjPath.delete(mutatedProjPathLen, mutatedProjPath.length());
                return;
            }
            createFile(buggyProject.getTestCase().toString(), mutatedProjPath.toString(), "testcase.txt");
            createFile(buggyProject.getCommand().toString(), mutatedProjPath.toString(), "rootcause.txt");
            writeToStorageFile();
        } catch (RuntimeException e) {
            System.out.println(e);
        }
    }

    private int increaseAndGetBugId() {
        synchronized (lock) {
            System.out.println(Thread.currentThread().getName() + " : " + bugId);
            bugId++;
            return bugId;
        }
    }

    private void writeToStorageFile() {
        synchronized (storageFileLock) {
            System.out.println(Thread.currentThread().getName() + " : writing to json file");
            JSONObject storedJSON = DatasetCreator.getStoredProjects(storageFilePath);
            storedJSON.put(buggyProject.key(), buggyProject.createJSONObject());
            try (FileWriter writer = new FileWriter(storageFilePath)) {
                writer.write(storedJSON.toString());
            } catch (IOException e) {

            }
        }
    }
}
