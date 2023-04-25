package jmutation.dataset.trace;

import jmutation.dataset.bug.model.path.MutationFrameworkPathConfiguration;
import jmutation.dataset.bug.model.path.PathConfiguration;
import jmutation.dataset.bug.model.path.PathConfiguration.InstrumentatorFile;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisabledIf("jmutation.dataset.TestUtils#isRunningInGitHubActions")
class TraceCreatorTest {
    public static final String TEST_FILES_PATH = String.join(File.separator, "src", "test", "files", "jmutation", "dataset", "bug", "creator");
    private static final String PROJECT_NAME = "sample-project";
    private static final String REPO_NAME = "sample-repo";
    private static final String REPO_PATH = String.join(File.separator, TEST_FILES_PATH, REPO_NAME);
    private static final List<String> TRACE_FILE_PATHS = new ArrayList<>();

    static {
        PathConfiguration pathConfig = new MutationFrameworkPathConfiguration(REPO_PATH);
        for (InstrumentatorFile fileType : InstrumentatorFile.values()) {
            if (fileType == InstrumentatorFile.BUGGY_PRECHECK || fileType == InstrumentatorFile.PRECHECK ||
                    fileType == InstrumentatorFile.BUGGY_TRACE_W_ASSERTS || fileType == InstrumentatorFile.TRACE_W_ASSERTS)
                continue;
            TRACE_FILE_PATHS.add(pathConfig.getInstrumentatorFilePath(PROJECT_NAME, Integer.toString(1), fileType));
        }
    }

    private TraceCreator creator;

    @BeforeEach
    void setup() throws IOException {
        creator = new TraceCreator(new File(REPO_PATH).getCanonicalPath(), PROJECT_NAME, 1);
    }

    @AfterEach
    public void afterEach() {
        for (String filePath : TRACE_FILE_PATHS) {
            File file = new File(filePath);
            file.delete();
        }
    }

    @Test
    void run_correctState_createsTraces() {
        creator.run();
        for (String filePath : TRACE_FILE_PATHS) {
            File file = new File(filePath);
            assertTrue(file.exists());
        }
    }

    @Test
    void runTraceCollection_correctState_createsTraces() {
        creator.runTraceCollection(8, 8);
        for (String filePath : TRACE_FILE_PATHS) {
            File file = new File(filePath);
            assertTrue(file.exists());
        }
    }

    @Test
    void isDone_tracesArePresent_returnsTrue() throws IOException {
        TraceCreator creator = new TraceCreator(new File(REPO_PATH).getCanonicalPath(), PROJECT_NAME, 2);
        assertTrue(creator.isDone());
    }

    @Test
    void isDone_tracesNotPresent_returnsFalse() {
        assertFalse(creator.isDone());
    }
}
