package jmutation.experiment;

import jmutation.execution.Defects4jExecutor;
import jmutation.model.mutation.MutationRange;
import jmutation.model.project.Defects4jProject;
import jmutation.mutation.MutationCommand;
import jmutation.mutation.explainable.ExplainableMutator;
import jmutation.mutation.explainable.client.Client;
import jmutation.mutation.explainable.client.ExplainableMutationClient;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class Defects4jExperiment {
    private static final String[] PROJECT_NAMES = {"Chart"};
    private static final int[] BUG_IDS = {26};
    private static final String PATH_TO_EXP_OUTPUT = Path.of(".", "reports"); // Assume working dir is root
    private ExplainableMutator mutator;

    public static void main(String[] args) {
        Defects4jExperiment experiment = new Defects4jExperiment();
        experiment.run();
    }

    public void run() {
        String serverIP = "";
        int serverPort = 0;
        mutator = new ExplainableMutator(new ExplainableMutationClient(new Client(serverIP, serverPort)));
        String path = Path.of("C:", "Users", "bchenghi", "Desktop", "mutation-experiment").toString();
        for (int i = 0; i < PROJECT_NAMES.length; i++) {
            String projName = PROJECT_NAMES[i];
            int totalBugs = BUG_IDS[i];
            for (int j = 1; j <= totalBugs; j++) {
                String versionStr = j + "f";
                // Checkout each project
                Defects4jProject.checkoutCommand(projName, j + "f", path);
                // Compile
                Defects4jProject project = new Defects4jProject(projName, new File(path), null, null, null, null, null);
                Defects4jExecutor workingExecutor = new Defects4jExecutor(project);
                String compilationResult = workingExecutor.compile();
                if (!compilationSuccess(compilationResult)) {
                    continue;
                }
                // Get changed class names -> mutation ranges
                List<String> classes = workingExecutor.getClassDiffs();
                List<MutationRange> mutationRanges = convertClassesToMutationRanges(classes);
                List<MutationCommand> commands = mutator.analyse(mutationRanges, project);
                for (MutationCommand command : commands) {
                    Defects4jProject clone = (Defects4jProject) project.cloneToOtherPath(
                            System.getProperty("java.io.tmpdir") + "\\mutation");
                    Defects4jExecutor buggyExecutor = new Defects4jExecutor(clone);
                    mutator.mutate(command, project);
                    compilationResult = buggyExecutor.compile();
                    if (!compilationSuccess(compilationResult)) {
                        continue;
                    }
                    buggyExecutor.test();
                    String[] failingTest;
                    try {
                        failingTest = project.getFailingTests();
                    } catch (IOException e) {
                        e.printStackTrace();
                        continue;
                    }
                    record(projName, versionStr, failingTest, true, command);
                }
            }
        }
    }

    public List<MutationRange> convertClassesToMutationRanges(List<String> classes) {
        List<MutationRange> result = new ArrayList<>();
        for (String clazz : classes) {
            result.add(new MutationRange(clazz, 0, 0));
        }
        return result;
    }

    private boolean compilationSuccess(String output) {
        return !output.toLowerCase().contains("fail");
    }

    /**
     * We want to record
     * Proj name, version
     * Mutation type and location
     * Mutation Score
     * Compilation success
     *
     * @param projectName
     * @param version
     */
    private void record(String projectName, String version, String[] failingTests, boolean compilationSuccess,
                        MutationCommand command) {

    }
}
