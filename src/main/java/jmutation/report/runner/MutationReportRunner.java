package jmutation.report.runner;

import jmutation.MutationFramework;
import jmutation.MutationFramework.MutationFrameworkBuilder;
import jmutation.model.MicrobatConfig;
import jmutation.model.PrecheckExecutionResult;
import jmutation.model.TestCase;
import jmutation.model.mutation.MutationFrameworkConfig;
import jmutation.model.mutation.MutationFrameworkConfig.MutationFrameworkConfigBuilder;
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
        MutationReport report = new MutationReport(new File("C:\\Users\\bchenghi\\Desktop\\mutation-report.xlsx"));
        MutationFrameworkConfig configuration = buildMutationFrameworkConfig();
        MutationFramework mutationFramework = buildMutationFramework(configuration);
        List<TestCase> testCaseList = mutationFramework.getTestCases();
        for (TestCase testCase : testCaseList) {
            mutationFramework.setTestCase(testCase);
            try {
                PrecheckExecutionResult precheckExecutionResult = mutationFramework.runPrecheck();
                List<MutationCommand> commands = mutationFramework.analyse(precheckExecutionResult.getCoverage());
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

    private MutationFramework buildMutationFramework(MutationFrameworkConfig config) {
        MutationFrameworkBuilder builder = new MutationFrameworkBuilder(config);
        return builder.build();
    }

    private MutationFrameworkConfig buildMutationFrameworkConfig() {
        String projectPath = String.join(File.separator, "sample", "math_70");
        MutationFrameworkConfigBuilder configurationBuilder = new MutationFrameworkConfigBuilder();
        configurationBuilder.setProjectPath(projectPath);
        configurationBuilder.setMutator(new HeuristicMutator(new StrongMutationParser()));
        MicrobatConfig microbatConfig = MicrobatConfig.defaultConfig(projectPath);
        microbatConfig = microbatConfig.setJavaHome("C:\\Program Files\\Java\\jre1.8.0_351");
        microbatConfig = microbatConfig.setStepLimit(10000000);
        configurationBuilder.setMicrobatConfig(microbatConfig);
        return configurationBuilder.build();
    }
}
