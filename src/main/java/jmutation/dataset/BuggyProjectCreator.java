package jmutation.dataset;

import jmutation.MutationFramework;
import jmutation.model.MutationResult;
import jmutation.model.TestCase;
import jmutation.mutation.MutationCommand;
import jmutation.mutation.heuristic.HeuristicMutator;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import static jmutation.dataset.DatasetCreator.bugId;

public class BuggyProjectCreator implements Runnable {
    private static final Object lock = new Object();
    private final String repositoryPath;
    private final String projectPath;
    private final String projectName;
    private final TestCase testCase;
    private final MutationCommand command;

    public BuggyProjectCreator(String repositoryPath, String projectPath, String projectName, TestCase testCase, MutationCommand command) {
        this.repositoryPath = repositoryPath;
        this.projectPath = projectPath;
        this.projectName = projectName;
        this.testCase = testCase;
        this.command = command;
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
        StringBuilder mutatedProjPath = new StringBuilder(repositoryPath + File.separator + projectName + File.separator);
        int mutatedProjPathLen = mutatedProjPath.length();
        int currBugId = increaseAndGetBugId();
        mutatedProjPath.append(currBugId);
        mutatedProjPath.append(File.separator);
        int mutatedBugPathLen = mutatedProjPath.length();
        mutatedProjPath.append("bug");
        mutationFramework.setMutatedProjectPath(mutatedProjPath.toString());
        mutatedProjPath.delete(mutatedBugPathLen, mutatedBugPathLen + 3);
        mutationFramework.setTestCase(testCase);
        try {
            MutationResult result = mutationFramework.mutate(command);
            if (result.getMutatedPrecheckExecutionResult().testCasePassed()) {
                mutatedProjPath.delete(mutatedProjPathLen, mutatedProjPath.length());
                return;
            }
            createFile(testCase.toString(), mutatedProjPath.toString(), "testcase.txt");
            createFile(command.toString(), mutatedProjPath.toString(), "rootcause.txt");
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
}
