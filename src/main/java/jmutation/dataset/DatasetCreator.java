package jmutation.dataset;

import jmutation.MutationFramework;
import jmutation.model.MutationResult;
import jmutation.model.TestCase;
import jmutation.mutation.MutationCommand;
import jmutation.mutation.heuristic.HeuristicMutator;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class DatasetCreator {
    public static void main(String[] args) {
        String projectPath = String.join(File.separator, "sample", "math_70");
        run(projectPath, "path");
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
        MutationFramework mutationFramework = new MutationFramework();
        mutationFramework.setProjectPath(projectPath);
        mutationFramework.setMutator(new HeuristicMutator());
        StringBuilder mutatedProjPath = new StringBuilder(repositoryPath + File.separator + projectName + File.separator);
        int mutatedProjPathLen = mutatedProjPath.length();
        List<TestCase> testCaseList = mutationFramework.getTestCases();
        int bugId = 1;
        for (TestCase testCase : testCaseList) {
            mutationFramework.setTestCase(testCase);
            List<MutationCommand> commands;
            try {
                commands = mutationFramework.analyse();
            } catch (RuntimeException e) {
                continue;
            }
            for (MutationCommand command : commands) {
                mutatedProjPath.append(bugId);
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
                        continue;
                    }
                    createFile(testCase.toString(), mutatedProjPath.toString(), "testcase.txt");
                    createFile(command.toString(), mutatedProjPath.toString(), "rootcause.txt");
                } catch (RuntimeException e) {
                    System.out.println(e);
                }
                mutatedProjPath.delete(mutatedProjPathLen, mutatedProjPath.length());
                bugId++;
            }
        }
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
}
