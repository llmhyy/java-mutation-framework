package jmutation.experiment;

import jmutation.experiment.runner.ExplainableMutationReportRunner;
import jmutation.model.project.Project;
import jmutation.mutation.MutationCommand;
import jmutation.mutation.explainable.ExplainableMutator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class Defects4jExperimentIT {
    private ExplainableMutationReportRunner explainableMutationReportRunner;
    @TempDir
    private Path tmpDir;

    @BeforeEach
    void setUp() {
        ExplainableMutator mutator = mock(ExplainableMutator.class);
        when(mutator.mutate(any(MutationCommand.class), any(Project.class))).
                thenAnswer(argument -> argument.getArgument(1)); // Just returns the original project without mutation
        explainableMutationReportRunner = new ExplainableMutationReportRunner(new String[] {"Chart"}, new int[] {1}, mutator, tmpDir);
    }

    @Test
    // Disabled as it requires Defects4j
    @DisabledIf("jmutation.utils.MiscellaneousTestUtils#isRunningInGitHubActions")
    void run_RunOnChart1_CreatesCSV() throws Exception {
        explainableMutationReportRunner.run();

    }
}