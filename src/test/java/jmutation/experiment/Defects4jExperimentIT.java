package jmutation.experiment;

import jmutation.experiment.report.explainable.ExplainableReport;
import jmutation.experiment.report.explainable.ExplainableReportReader;
import jmutation.experiment.report.explainable.ExplainableTrial;
import jmutation.experiment.runner.ExplainableMutationReportRunner;
import jmutation.model.project.Project;
import jmutation.mutation.MutationCommand;
import jmutation.mutation.explainable.ExplainableMutationCommand;
import jmutation.mutation.explainable.ExplainableMutator;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class Defects4jExperimentIT {
    private ExplainableMutationReportRunner explainableMutationReportRunner;
    private static final String REPORT_FILE_NAME = "report.xlsx";
    private ExplainableTrial expectedTrial = new ExplainableTrial("projName", "versionStr",
            "method", "mutatedMethod", "comment", "mutatedComment",
                    "message", "command", new String[] {"failingTest0"}, 2);
    @TempDir
    private Path tmpDir;

    @BeforeEach
    void setUp() throws IOException {
        ExplainableMutator mutator = mock(ExplainableMutator.class);
        when(mutator.mutate(any(MutationCommand.class), any(Project.class))).
                thenAnswer(argument -> argument.getArgument(1)); // Just returns the original project without mutation
        when(mutator.analyse(any(List.class), any(Project.class))).
                thenAnswer(argument -> {
                    List<MutationCommand> result = new ArrayList<>();
                    result.add(null);
                    return result;
                });

        ExplainableTrial.ExplainableTrialFactory explainableTrialFactory =
                mock(ExplainableTrial.ExplainableTrialFactory.class);
        when(explainableTrialFactory.create(any(), any(), any(), any(), any(), anyInt())).thenReturn(expectedTrial);

        File reportFile = tmpDir.resolve(REPORT_FILE_NAME).toFile();
        ExplainableReport report = new ExplainableReport(reportFile);

        explainableMutationReportRunner = new ExplainableMutationReportRunner(new String[] {"Chart"}, new int[] {1},
                mutator, tmpDir.resolve("before-mutation"), explainableTrialFactory, report);
    }

    @Test
    // Disabled on github actions as it requires Defects4j
    @DisabledIf("jmutation.utils.MiscellaneousTestUtils#isRunningInGitHubActions")
    void run_RunOnChart1_CreatesCSV() throws Exception {
        explainableMutationReportRunner.run();
        File reportFile = tmpDir.resolve(REPORT_FILE_NAME).toFile();
        FileInputStream fileInputStream = new FileInputStream(reportFile);
        XSSFWorkbook workbook = new XSSFWorkbook(fileInputStream);
        ExplainableReportReader reportReader = new ExplainableReportReader(workbook);
        Iterator<ExplainableTrial> trials = reportReader.getTrials();
        ExplainableTrial actualTrial = trials.next();
        assertEquals(expectedTrial, actualTrial);
        assertFalse(trials.hasNext());
    }
}