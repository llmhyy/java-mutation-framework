package jmutation.report.runner;

import jmutation.MutationFramework;
import jmutation.model.MicrobatConfig;
import jmutation.model.MutationResult;
import jmutation.model.TestCase;
import jmutation.mutation.MutationCommand;
import jmutation.mutation.heuristic.HeuristicMutator;
import jmutation.mutation.heuristic.parser.StrongMutationParser;
import jmutation.report.MutationReport;
import jmutation.report.MutationTrial;

import java.io.File;
import java.util.List;

public class MutationReportRunner extends ReportRunner {
    public static void main(String[] args) throws Exception {
        MutationReportRunner runner = new MutationReportRunner();
        runner.run();
    }

    public void run() throws Exception {
        String projectPath = String.join(File.separator, "sample", "math_70");
        MutationFramework mutationFramework = new MutationFramework();
        mutationFramework.setProjectPath(projectPath);
        mutationFramework.setMutator(new HeuristicMutator(new StrongMutationParser()));
        MicrobatConfig microbatConfig = MicrobatConfig.defaultConfig(projectPath);
        microbatConfig = microbatConfig.setJavaHome("C:\\Program Files\\Java\\jre1.8.0_341");
        microbatConfig = microbatConfig.setStepLimit(1000000);
        mutationFramework.setMicrobatConfig(microbatConfig);
        List<TestCase> testCaseList = mutationFramework.getTestCases();
        MutationReport report = new MutationReport(new File("C:\\Users\\bchenghi\\Desktop\\mutation-report.xlsx"));
        boolean skip = true;
        String testCaseName = "org.apache.commons.math.optimization.linear.SimplexSolverTest#testMath272(),31,46";
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
                    if (commandId < 13 && testCase.toString().equals(testCaseName)) {
                        commandId++;
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
}
