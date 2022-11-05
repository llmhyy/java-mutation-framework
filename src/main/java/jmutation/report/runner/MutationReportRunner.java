package jmutation.report.runner;

import jmutation.MutationFramework;
import jmutation.model.MicrobatConfig;
import jmutation.model.MutationResult;
import jmutation.model.TestCase;
import jmutation.mutation.commands.MutationCommand;
import jmutation.report.MutationReport;
import jmutation.report.MutationTrial;

import java.io.File;
import java.util.List;

public class MutationReportRunner extends ReportRunner {
    public static void main(String[] args) throws Exception {
        MutationReportRunner runner = new MutationReportRunner();
        //runner.run();
        runner.runAllPossible();
    }

    /**
     * Runs mutations on a range of seeds, and reports the number of unique mutations achieved
     *
     * @throws Exception
     */
    public void run() throws Exception {
        // TODO: Implement running framework, collecting results/catching exceptions
        // Create mutationTrial
        // Write each trial to an excel file
        String projectPath = String.join(File.separator, "sample", "math_70");
        MutationFramework mutationFramework = new MutationFramework();
        mutationFramework.setProjectPath(projectPath);
        MicrobatConfig microbatConfig = MicrobatConfig.defaultConfig(projectPath);
        microbatConfig = microbatConfig.setJavaHome("C:\\Program Files\\Java\\jre1.8.0_341");
        mutationFramework.setMicrobatConfig(microbatConfig);
        List<TestCase> testCaseList = mutationFramework.getTestCases();
        MutationReport report = new MutationReport(new File("reports/mutation-report.xlsx"));
//        int numOfAttempts = 1;
//        for (TestCase testCase : testCaseList) {
//            mutationFramework.setTestCase(testCase);
//            MutationTrial trial = new MutationTrial("math_70", testCase, new ArrayList<>(), new ArrayList<>());
//            for (int seed = 0; seed < numOfAttempts; seed++) {
//                mutationFramework.setSeed(seed);
//                MutationResult result = mutationFramework.mutate();
//                if (result.getMutatedPrecheckExecutionResult().testCasePassed()) {
//                    continue;
//                }
//                updateTrial(trial, result);
//            }
//            report.record(trial);
//        }
    }

    public void runAllPossible() throws Exception {
        String projectPath = String.join(File.separator, "sample", "math_70");
        MutationFramework mutationFramework = new MutationFramework();
        mutationFramework.setProjectPath(projectPath);
        MicrobatConfig microbatConfig = MicrobatConfig.defaultConfig(projectPath);
        microbatConfig = microbatConfig.setJavaHome("C:\\Program Files\\Java\\jre1.8.0_341");
        microbatConfig = microbatConfig.setStepLimit(1000000);
        mutationFramework.setMicrobatConfig(microbatConfig);
        List<TestCase> testCaseList = mutationFramework.getTestCases();
        MutationReport report = new MutationReport(new File("C:\\Users\\bchenghi\\Desktop\\mutation-report.xlsx"));
        boolean skip = true;
        String testCaseName = "org.apache.commons.math.analysis.interpolation.SmoothingPolynomialBicubicSplineInterpolatorTest#testPreconditions(),30,81";
        for (TestCase testCase : testCaseList) {
            if (testCase.toString().equals(testCaseName)) {
                skip = false;
            }
            if (skip) {
                continue;
            }
            mutationFramework.setTestCase(testCase);
            try {
                List<MutationCommand> commands = mutationFramework.analyse();
                int commandId = 0;
                for (MutationCommand command : commands) {
                    if (commandId < 5 && testCase.toString().equals(testCaseName)) {
                        continue;
                    }
                    MutationResult result = mutationFramework.mutate(command);
                    report.record(new MutationTrial("math_70", testCase,
                            command, result.getMutatedPrecheckExecutionResult().getPrecheckResult().getProgramMessage()));
                    commandId++;
                }
            } catch (RuntimeException e) {
                report.record(new MutationTrial("math_70", testCase,
                        null, e.toString()));
            }
        }
    }
//
//    private void updateTrial(MutationTrial trial, MutationResult result) {
//        trial.getMutationCommands().addAll(result.getMutationCommandList());
//    }
}
