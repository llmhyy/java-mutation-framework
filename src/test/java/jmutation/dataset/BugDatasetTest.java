package jmutation.dataset;

import jmutation.dataset.BugDataset.BugData;
import jmutation.dataset.bug.minimize.ProjectMinimizer;
import jmutation.utils.TraceHelper;
import microbat.model.BreakPoint;
import microbat.model.trace.Trace;
import microbat.model.trace.TraceNode;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BugDatasetTest {
    private static final String PROJECT_NAME = "sample";
    private static final String FILE_ROOT = String.join(File.separator, "src", "test", "files", "jmutation", "dataset");
    private static final String REPO_PATH = String.join(File.separator, FILE_ROOT, "sample-bug-repo");
    private static final String UNZIPPED_BUG_1 = String.join(File.separator, REPO_PATH, PROJECT_NAME, "1");
    private static final String ZIPPED_BUG_1 = UNZIPPED_BUG_1 + ".zip";
    private static final String UNZIPPED_BUG_2 = String.join(File.separator, REPO_PATH, PROJECT_NAME, "2");
    private static final String ZIPPED_BUG_2 = UNZIPPED_BUG_2 + ".zip";
    private static final String EXPECTED_BUG_2 = String.join(File.separator, REPO_PATH, PROJECT_NAME,
            "expected-2");
    private BugDataset bugDataset;

    @Test
    void zip_bugDirProvided_zipsCorrectly() throws IOException {
        bugDataset.zip(1);
        assertTrue(new File(ZIPPED_BUG_1).exists());
        assertFalse(new File(UNZIPPED_BUG_1).exists());
    }

    @Test
    void zip_bugDirAndFullPathProvided_zipsCorrectly() throws IOException {
        BugDataset bugDataset = new BugDataset(REPO_PATH + File.separator + PROJECT_NAME);
        bugDataset.zip(1);
        assertTrue(new File(ZIPPED_BUG_1).exists());
        assertFalse(new File(UNZIPPED_BUG_1).exists());
    }

    @Test
    void unzip_zippedBugDirProvided_unzipsCorrectly() throws IOException {
        bugDataset.unzip(2);
        TestUtils.dirsAreEqual(EXPECTED_BUG_2, UNZIPPED_BUG_2);
        assertTrue(new File(ZIPPED_BUG_2).exists());
    }

    @Test
    void getData_bugDirProvidedAndMinimized_getsCorrectData() throws IOException {
        BugData data = bugDataset.getData(1);
        assertTrue(data.getBuggyTrace().size() > 0);
        assertTrue(data.getWorkingTrace().size() > 0);
        assertEquals(6, data.getRootCauseNode());
        assertEquals(PROJECT_NAME, data.getProjectName());
        TestCase expectedTestCase = new TestCase("org.example.SampleTest",
                "test", "org.example.SampleTest#test(),9,13");
        assertEquals(expectedTestCase.testClassName(), data.getTestCase().testClassName());
        assertEquals(expectedTestCase.testMethodName(), data.getTestCase().testMethodName());
        assertEquals(expectedTestCase.toString(), data.getTestCase().toString());
        for (TraceNode node : data.getWorkingTrace().getExecutionList()) {
            BreakPoint breakPoint = node.getBreakPoint();
            assertFalse(breakPoint.getFullJavaFilePath().isEmpty());
        }
    }

    // This one is a system test, we could put it in its own file
    @Test
    void setClassPathsToBreakpoints_traceFilesProvided_allTraceNodesGetsJavaPath() throws IOException {
        BugData data = bugDataset.getData(1);
        ProjectMinimizer minimizer = bugDataset.createMinimizer(1);
        minimizer.maximise();
        TraceHelper.setClassPathsToBreakpoints(data.getBuggyTrace(), new File(data.getBuggyProjectPath()));
        checkTraceHasJavaPaths(data.getBuggyTrace());
        TraceHelper.setClassPathsToBreakpoints(data.getWorkingTrace(), new File(data.getWorkingProjectPath()));
        checkTraceHasJavaPaths(data.getWorkingTrace());
    }

    @Test
    void getData_bugDirMissingTraceFile_throwsFileNotFoundException() {
        assertThrows(IOException.class, () -> bugDataset.getData(3));
    }


    @Test
    void getData_bugDirMissingRootCauseFile_throwsFileNotFoundException() {
        assertThrows(IOException.class, () -> bugDataset.getData(4));
    }

    @Test
    void exists_dirDoesNotExist_returnsFalse() {
        assertFalse(bugDataset.exists(10, false));
    }

    @Test
    void exists_dirExists_returnsTrue() {
        assertTrue(bugDataset.exists(1, false));
    }

    @Test
    void parseRootCauseStr_RootCauseStr_ReturnsCorrectRootCause() {
        String rootCauseStr = "MutationMathOperatorCommand#org.apache.commons.math.analysis.BinaryFunction#lines 37-37#[x + y]";
        BugDataset.RootCause actual = bugDataset.parseRootCauseStr(rootCauseStr);
        BugDataset.RootCause expected = new BugDataset.RootCause(37, 37, "org.apache.commons.math.analysis.BinaryFunction");
        assertEquals(expected, actual);
    }

    @Test
    @DisabledIf("jmutation.dataset.TestUtils#isRunningInGitHubActions")
    void getDataWithTraceCollection_zippedBugDirWithoutTraces_getsData() throws IOException {
        BugData data = bugDataset.getDataWithTraceCollection(5, 1);
        assertTrue(data.getBuggyTrace().size() > 0);
        assertTrue(data.getWorkingTrace().size() > 0);
        assertEquals(6, data.getRootCauseNode());
        assertEquals(PROJECT_NAME, data.getProjectName());
        TestCase expectedTestCase = new TestCase("org.example.SampleTest",
                "test", "org.example.SampleTest#test(),9,13");
        assertEquals(expectedTestCase.testClassName(), data.getTestCase().testClassName());
        assertEquals(expectedTestCase.testMethodName(), data.getTestCase().testMethodName());
        assertEquals(expectedTestCase.toString(), data.getTestCase().toString());
    }

    @BeforeEach
    void beforeEach() throws IOException {
        bugDataset = new BugDataset(REPO_PATH, PROJECT_NAME);
        clean();
    }

    @AfterEach
    void afterEach() throws IOException {
        FileUtils.deleteDirectory(new File(REPO_PATH));
    }

    void clean() throws IOException {
        TestUtils.deleteIfExists(new File(UNZIPPED_BUG_2));
        TestUtils.deleteIfExists(new File(ZIPPED_BUG_1));
        restoreOriginalProjects();
    }

    void restoreOriginalProjects() {
        // clone original into sample-repo
        final String originalPath = String.join(File.separator, FILE_ROOT, "sample-bug-repo-original");
        try {
            FileUtils.deleteDirectory(new File(REPO_PATH));
            FileUtils.copyDirectory(new File(originalPath), new File(REPO_PATH));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void checkTraceHasJavaPaths(Trace trace) {
        List<TraceNode> executionList = trace.getExecutionList();
        for (TraceNode node : executionList) {
            assertFalse(node.getBreakPoint().getFullJavaFilePath().isEmpty());
        }
    }
}
