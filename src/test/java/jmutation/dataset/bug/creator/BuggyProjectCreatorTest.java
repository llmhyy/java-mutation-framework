package jmutation.dataset.bug.creator;

import jmutation.model.mutation.DumpFilePathConfig;
import jmutation.model.mutation.MutationFrameworkConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;

import static jmutation.dataset.constants.FileNames.BUGGY_PROJECT_DIR;
import static org.junit.jupiter.api.Assertions.assertEquals;

class BuggyProjectCreatorTest {
    private BuggyProjectCreator buggyProjectCreator;

    @BeforeEach
    void setup() {
        buggyProjectCreator = new BuggyProjectCreator(
                "repoPath", "projectPath", null, "storagePath", 0);
    }

    @Test
    void createMutationFrameworkConfig_ProvidedMutatedProjPath_SetsDumpFilePathsCorrectly() {
        String mutatedProjPath = "mutatedProjPath";
        MutationFrameworkConfig mutationFrameworkConfig =
                buggyProjectCreator.createMutationFrameworkConfig(mutatedProjPath);
        assertEquals(getExpectedDumpFilePathConfig(mutatedProjPath), mutationFrameworkConfig.getDumpFilePathConfig());
        assertEquals("projectPath", mutationFrameworkConfig.getProjectPath());
        assertEquals(mutatedProjPath + File.separator + BUGGY_PROJECT_DIR, mutationFrameworkConfig.getMutatedProjectPath());
    }

    @Test
    void createDumpFilePathConfig_ProvidedMutatedProjPath_SetsDumpFilePathsCorrectly() {
        String mutatedProjPath = "mutatedProjPath";
        DumpFilePathConfig actualDumpFilePathConfig = buggyProjectCreator.createDumpFilePathConfig(mutatedProjPath);
        DumpFilePathConfig expectedDumpFilePathConfig = getExpectedDumpFilePathConfig(mutatedProjPath);
        assertEquals(expectedDumpFilePathConfig, actualDumpFilePathConfig);
    }

    private DumpFilePathConfig getExpectedDumpFilePathConfig(String mutatedProjPath) {
        DumpFilePathConfig expectedDumpFilePathConfig = new DumpFilePathConfig();
        mutatedProjPath += File.separator;
        expectedDumpFilePathConfig.setMutatedPrecheckFilePath(mutatedProjPath +
                DumpFilePathConfig.DEFAULT_BUGGY_PRECHECK_FILE);
        expectedDumpFilePathConfig.setMutatedTraceFilePath(mutatedProjPath +
                DumpFilePathConfig.DEFAULT_BUGGY_TRACE_FILE);
        expectedDumpFilePathConfig.setTraceFilePath(mutatedProjPath +
                DumpFilePathConfig.DEFAULT_TRACE_FILE);
        expectedDumpFilePathConfig.setPrecheckFilePath(mutatedProjPath +
                DumpFilePathConfig.DEFAULT_PRECHECK_FILE);
        return expectedDumpFilePathConfig;
    }
}