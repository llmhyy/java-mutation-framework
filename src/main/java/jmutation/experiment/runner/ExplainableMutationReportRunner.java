package jmutation.experiment.runner;

import jmutation.execution.Defects4jExecutor;
import jmutation.experiment.report.explainable.ExplainableReport;
import jmutation.experiment.report.explainable.ExplainableTrial;
import jmutation.model.mutation.MutationRange;
import jmutation.model.project.Defects4jProject;
import jmutation.mutation.MutationCommand;
import jmutation.mutation.explainable.ExplainableMutationCommand;
import jmutation.mutation.explainable.ExplainableMutator;
import jmutation.mutation.explainable.client.Client;
import jmutation.mutation.explainable.client.ExplainableMutationClient;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class ExplainableMutationReportRunner extends ReportRunner {
    private final String[] projectNames;
    private final int[] bugIds;
    private static final Path PATH_TO_EXP_OUTPUT = Path.of(".", "reports"); // Assume working dir is root
    private static final Path PATH_TO_MODELS = Path.of(".", "models");
    private static final Path PATH_TO_CODE_MODEL = PATH_TO_MODELS.resolve(Path.of("code-generation", "saved_models", "checkpoint-best-score", "pytorch_model.bin"));
    private static final Path PATH_TO_COMMENT_MODEL = PATH_TO_MODELS.resolve(Path.of("codet5_base_all_lr5_bs32_src64_trg64_pat5_e10", "checkpoint-best-bleu", "pytorch_mode.bin")); // Assume working dir is root
    private final ExplainableMutator mutator;
    private final Path pathToCloneDefects4J;
    private final ExplainableTrial.ExplainableTrialFactory explainableTrialFactory;
    private final ExplainableReport report;

    public ExplainableMutationReportRunner(String[] defects4jProjects, int[] defects4jBugIds,
                                           ExplainableMutator mutator, Path pathToCloneDefects4J,
                                           ExplainableTrial.ExplainableTrialFactory explainableTrialFactory,
                                           ExplainableReport report) {
        this.projectNames = defects4jProjects;
        this.bugIds = defects4jBugIds;
        this.mutator = mutator;
        this.pathToCloneDefects4J = pathToCloneDefects4J;
        this.explainableTrialFactory = explainableTrialFactory;
        this.report = report;
    }

    public static void main(String[] args) throws IOException {
        String[] projects = {"Chart"};
        int[] bugs = {26};
        ExplainableMutationClient explainableMutationClient = new ExplainableMutationClient(
                new Client("localhost", 8080));
        Path path = Path.of("C:", "Users", "bchenghi", "Desktop", "mutation-experiment"); // Change based on system
        ExplainableTrial.ExplainableTrialFactory trialBuilder = new ExplainableTrial.ExplainableTrialFactory();
        ExplainableReport report = new ExplainableReport(new File("./report.xlsx"));
        ExplainableMutationReportRunner experiment = new ExplainableMutationReportRunner(projects, bugs,
                new ExplainableMutator(explainableMutationClient), path, trialBuilder, report);
        experiment.run();
    }

    public void run() throws IOException {
        for (int i = 0; i < projectNames.length; i++) {
            String projName = projectNames[i];
            int totalBugs = bugIds[i];
            for (int j = 1; j <= totalBugs; j++) {
                String versionStr = j + "f";
                // Checkout each project
                // Compile
                Defects4jProject project = new Defects4jProject(projName, pathToCloneDefects4J.toFile(),
                        null, null,
                        null, null, null);
                Defects4jExecutor workingProjectExecutor = Defects4jExecutor.checkout(projName, versionStr, pathToCloneDefects4J.toString());
                String compilationResult = workingProjectExecutor.compile();
                if (!compilationSuccess(compilationResult)) {
                    continue;
                }

                // Get changed class names -> mutation ranges (here we could extract into a method?)
                List<String> classes = workingProjectExecutor.getClassDiffs();
                List<MutationRange> mutationRanges = convertClassesToMutationRanges(classes);
                List<MutationCommand> commands = mutator.analyse(mutationRanges, project);
                // For each command, mutate and record the result (compilation failure, mutation score, etc)
                for (MutationCommand command : commands) {
                    Defects4jProject clone = (Defects4jProject) project.cloneToOtherPath(
                            System.getProperty("java.io.tmpdir") + File.separator + "mutation");
                    mutateAndRecordResult(report, command, clone, projName, versionStr);
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

    public void mutateAndRecordResult(ExplainableReport report, MutationCommand command,
                                      Defects4jProject projectToMutate,
                                      String projName, String versionStr) throws IOException {
        Defects4jExecutor buggyProjectExecutor = new Defects4jExecutor(projectToMutate);
        mutator.mutate(command, projectToMutate);
        String compilationResult = buggyProjectExecutor.compile();
        ExplainableMutationCommand explainableMutationCommand = (ExplainableMutationCommand) command;
        if (!compilationSuccess(compilationResult)) {
            ExplainableTrial trial = explainableTrialFactory.create(projName,
                    versionStr, explainableMutationCommand, "COMPILATION_FAILED",
                    new String[]{}, 0);
            report.record(trial);
            return;
        }
        buggyProjectExecutor.test();
        String[] failingTest;
        int totalTests;
        try {
            failingTest = projectToMutate.getFailingTests();
            totalTests = projectToMutate.getAllTests().length;
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        ExplainableTrial trial = explainableTrialFactory.create(projName,
                versionStr, explainableMutationCommand, "",
                failingTest, totalTests);
        report.record(trial);
    }
}