package jmutation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;

import java.io.File;

class MutationFrameworkSystemTest {
    @Test
    // Disable in Github actions as it requires Microbat configuration (microbatConfig.json) to be correct. (JAVA_HOME path)
    @DisabledIf("jmutation.utils.MiscellaneousTestUtils#isRunningInGitHubActions")
    void main_sampleProject_mutatesTheProject() {
        String pathToProject = String.join(File.separator, "sample", "math_70");
        Main.main(new String[]{
                        "-projectPath",
                        pathToProject,
                        "-testCase",
                        "org.apache.commons.math.analysis.BinaryFunctionTest#testAdd"
                }
        );
    }
}
