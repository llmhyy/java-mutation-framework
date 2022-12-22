package jmutation.report.runner;

import jmutation.MutationFramework;
import jmutation.model.MicrobatConfig;
import jmutation.model.TestCase;
import jmutation.model.mutation.MutationFrameworkConfig;
import jmutation.model.mutation.MutationResult;
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
        MutationFrameworkConfig configuration = new MutationFrameworkConfig();
        MutationFramework mutationFramework = new MutationFramework();
        mutationFramework.setConfig(configuration);
        configuration.setProjectPath(projectPath);
        configuration.setMutator(new HeuristicMutator(new StrongMutationParser()));
        MicrobatConfig microbatConfig = MicrobatConfig.defaultConfig(projectPath);
        microbatConfig = microbatConfig.setJavaHome("C:\\Program Files\\Java\\jre1.8.0_341");
        microbatConfig = microbatConfig.setStepLimit(10000000);
        configuration.setMicrobatConfig(microbatConfig);
        List<TestCase> testCaseList = mutationFramework.getTestCases();
        MutationReport report = new MutationReport(new File("C:\\Users\\Chenghin\\Desktop\\mutation-report.xlsx"));
        for (TestCase testCase : testCaseList) {
            configuration.setTestCase(testCase);
            try {
                List<MutationCommand> commands = mutationFramework.analyse();
                for (MutationCommand command : commands) {
                    MutationResult result = mutationFramework.mutate(command);
                    report.record(new MutationTrial("math_70", testCase,
                            command,
                            result.getFixedPrecheckExecutionResult().getPrecheckInfo().getProgramMsg(),
                            result.getMutatedPrecheckExecutionResult().getPrecheckInfo().getProgramMsg(),
                            result.getFixedPrecheckExecutionResult().getTotalSteps(),
                            result.getMutatedPrecheckExecutionResult().getPrecheckInfo().getStepTotal()));
                }
            } catch (RuntimeException e) {
                report.record(new MutationTrial("math_70", testCase,
                        null, e.toString(), "", 0, 0));
            }
        }
    }
}
