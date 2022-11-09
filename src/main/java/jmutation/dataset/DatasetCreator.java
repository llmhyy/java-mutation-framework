package jmutation.dataset;

import jmutation.MutationFramework;
import jmutation.model.MutationFrameworkResult;
import jmutation.model.TestCase;
import jmutation.mutation.MutationCommand;
import jmutation.mutation.heuristic.HeuristicMutator;
import microbat.model.trace.TraceNode;

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
        MutationFramework mutationFramework = new MutationFramework();
        mutationFramework.setProjectPath(projectPath);
        mutationFramework.setMutator(new HeuristicMutator());
        StringBuilder mutatedProjPath = new StringBuilder(repositoryPath + File.separator + projectName + File.separator);
        int mutatedProjPathLen = mutatedProjPath.length();
        List<TestCase> testCaseList = mutationFramework.getTestCases();
        List<MutationCommand> commands = mutationFramework.analyse();
        int bugId = 1;
        for (TestCase testCase : testCaseList) {
            for (MutationCommand command : commands) {
                int mutatedBugPathLen = mutatedProjPath.length();
                mutatedProjPath.append("bug");
                mutationFramework.setMutatedProjectPath(mutatedProjPath.toString());
                mutatedProjPath.delete(mutatedBugPathLen, mutatedBugPathLen + 3);
                mutationFramework.setTestCase(testCase);
                try {
                    MutationFrameworkResult result = mutationFramework.startMutationFramework(command);
                    if (result.isTestCasePassed()) {
                        mutatedProjPath.delete(mutatedProjPathLen, mutatedProjPath.length());
                        continue;
                    }
                    List<TraceNode> rootCauses = result.getRootCauses();
                    if (!rootCauses.isEmpty()) {
                        TraceNode rootCause = rootCauses.get(0);
                        createFile(String.valueOf(rootCause.getOrder()), mutatedProjPath.toString(), "rootcause.txt");
                    }
                    mutatedProjPath.append(bugId + File.separator);
                    createFile(testCase.toString(), mutatedProjPath.toString(), "testcase.txt");
                } catch (RuntimeException e) {
                    System.out.println(e);
                }
                mutatedProjPath.delete(mutatedProjPathLen, mutatedProjPath.length());
                bugId++;
            }
        }
    }

    private static boolean createFile(String contents, String pathToFile, String fileName) {
        File file = new File(pathToFile);
        file.mkdirs();
        try (FileWriter writer = new FileWriter(new File(file, fileName))) {
            writer.write(contents);
        } catch (IOException e) {
            System.out.println(e);
            return false;
        }
        return true;
    }
}
