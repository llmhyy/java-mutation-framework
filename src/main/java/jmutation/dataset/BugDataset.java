package jmutation.dataset;

import jmutation.dataset.bug.creator.BuggyProjectCreator;
import jmutation.dataset.bug.minimize.ProjectMinimizer;
import jmutation.dataset.bug.model.path.MutationFrameworkPathConfiguration;
import jmutation.dataset.bug.model.path.PathConfiguration;
import jmutation.dataset.bug.model.path.PathConfiguration.InstrumentatorFile;
import jmutation.dataset.execution.Request;
import jmutation.dataset.execution.handler.TraceCollectionHandler;
import jmutation.dataset.trace.TraceCreator;
import jmutation.dataset.utils.Zipper;
import microbat.instrumentation.output.RunningInfo;
import microbat.model.BreakPoint;
import microbat.model.trace.Trace;
import microbat.model.trace.TraceNode;
import org.apache.commons.io.FileUtils;
import sav.common.core.SavRtException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static jmutation.utils.TraceHelper.setClassPathsToBreakpoints;

public class BugDataset {
    private final String projectName;
    private final String repoPath;
    private final MutationFrameworkPathConfiguration pathConfig;

    public BugDataset(String repoPath, String projectName) {
        super();
        this.repoPath = repoPath;
        this.projectName = projectName;
        pathConfig = new MutationFrameworkPathConfiguration(repoPath);
    }

    public BugDataset(String pathToProject) {
        super();
        Path path = Path.of(pathToProject);
        projectName = path.getName(path.getNameCount() - 1).toString();
        repoPath = path.getParent().toString();
        pathConfig = new MutationFrameworkPathConfiguration(repoPath);
    }

    public static void main(String[] args) throws IOException {
        // These 4 variables can be modified based on usage
        int largestBugId = 17426;
        String repoPath = "E:\\david";
        String projName = "math_70";
        int traceCollectionTimeoutSeconds = 60;

        BugDataset bugdataset = new BugDataset(repoPath + "\\" + projName);
        PathConfiguration pathConfig = new MutationFrameworkPathConfiguration(repoPath);
        for (int i = 1; i <= largestBugId; i++) {
            if (bugdataset.exists(i, false)) { // Check the bug's directory exists
                try {
                    new TraceCollectionHandler(repoPath, projName, i, traceCollectionTimeoutSeconds,
                            0, 0).handle(new Request(true));
                    BugData data = bugdataset.getData(i);
                    System.out.println(data);
                    bugdataset.deleteInstrumentationFiles(pathConfig, projName, String.valueOf(i));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void deleteInstrumentationFiles(PathConfiguration pathConfig, String projName, String bugIdString)
            throws IOException {
        String buggyTracePath =
                pathConfig.getInstrumentatorFilePath(projName, bugIdString, InstrumentatorFile.BUGGY_TRACE);
        Files.deleteIfExists(Path.of(buggyTracePath));
        String fixedTracePath = pathConfig.getInstrumentatorFilePath(projName, bugIdString, InstrumentatorFile.TRACE);
        Files.deleteIfExists(Path.of(fixedTracePath));
        String fixedTraceExcludesPath =
                pathConfig.getInstrumentatorFilePath(projName, bugIdString, InstrumentatorFile.TRACE_EXCLUDES);
        Files.deleteIfExists(Path.of(fixedTraceExcludesPath));
        String buggyTraceExcludesPath =
                pathConfig.getInstrumentatorFilePath(projName, bugIdString, InstrumentatorFile.BUGGY_EXCLUDES);
        Files.deleteIfExists(Path.of(buggyTraceExcludesPath));
    }

    // Create bug id ranges based on specified size.
    private List<int[]> createBugRanges(long gbSize, int largestBugId) throws IOException {
        List<int[]> result = new ArrayList<>();
        double oneGb = Math.pow(2, 30);
        double currSize = 0;
        int minBugId = 1;
        for (int i = 1; i <= largestBugId; i++) {
            if (new File(pathConfig.getBugPath(projectName, Integer.toString(i)) + ".zip").exists()) {
                currSize += Files.size(Path.of(
                        pathConfig.getBugPath(projectName, Integer.toString(i)) + ".zip")) / oneGb;
                if (currSize >= gbSize) {
                    result.add(new int[]{minBugId, i});
                    currSize = 0;
                    minBugId = i + 1;
                }
            }
        }
        result.add(new int[]{minBugId, largestBugId});
        return result;
    }

    public boolean exists(int bugId, boolean isZipped) {
        String path = pathConfig.getBugPath(projectName, Integer.toString(bugId));
        if (isZipped) path += ".zip";
        return new File(path).exists();
    }

    public String getBugIdPath(int bugId) {
        return pathConfig.getBugPath(projectName, Integer.toString(bugId));
    }

    public void zip(int bugId) throws IOException {
        String pathToBug = pathConfig.getBugPath(projectName, Integer.toString(bugId));
        Zipper.zip(pathToBug);
        FileUtils.deleteDirectory(new File(pathToBug));
    }

    public void unzip(int bugId) throws IOException {
        String pathToBug = pathConfig.getBugPath(projectName, Integer.toString(bugId));
        Zipper.unzip(pathToBug + ".zip", pathConfig.getRepoPath() + File.separator + projectName);
        new File(pathToBug + ".zip").delete();
    }

    public BugData getData(int bugId) throws IOException {
        String bugIdStr = Integer.toString(bugId);
        String pathToBug = pathConfig.getBugPath(projectName, bugIdStr);
        String pathToBuggyTrace = pathConfig.getInstrumentatorFilePath(projectName,
                bugIdStr, InstrumentatorFile.BUGGY_TRACE);
        String pathToWorkingTrace = pathConfig.getInstrumentatorFilePath(projectName,
                bugIdStr, InstrumentatorFile.TRACE);
        String pathToRootCauseFile = String.join(File.separator, pathToBug, BuggyProjectCreator.ROOTCAUSE_FILE_NAME);
        String pathToTestCaseFile = String.join(File.separator, pathToBug, BuggyProjectCreator.TESTCASE_FILE_NAME);
        Trace buggyTrace;
        Trace workingTrace;
        try {
            buggyTrace = RunningInfo.readFromFile(pathToBuggyTrace).getMainTrace();
            workingTrace = RunningInfo.readFromFile(pathToWorkingTrace).getMainTrace();
            File buggyProjectRoot = new File(pathConfig.getBuggyPath(projectName, bugIdStr));
            if (buggyProjectRoot.exists()) {
                setClassPathsToBreakpoints(buggyTrace, buggyProjectRoot);
            }
            setClassPathsToBreakpoints(workingTrace, new File(pathConfig.getFixPath(projectName, bugIdStr)));
        } catch (SavRtException e) {
            throw new IOException(e);
        }
        try {
            RootCause rootCause = parseRootCauseStr(Files.readString(Path.of(pathToRootCauseFile)));
            return new BugData(getRootCauseNodeOrder(rootCause, workingTrace), buggyTrace, workingTrace,
                    Files.readString(Path.of(pathToTestCaseFile)), projectName,
                    pathConfig.getFixPath(projectName, bugIdStr),
                    pathConfig.getBuggyPath(projectName, bugIdStr));
        } catch (NoSuchFileException e) {
            throw new IOException(e);
        }
    }

    /**
     * Gets bug data from a given bug Id dir
     *
     * @param bugId
     * @param timeout Timeout for trace collection in minutes
     * @return
     * @throws IOException
     */
    public BugData getDataWithTraceCollection(int bugId, int timeout) throws IOException {
        TraceCreator traceCreator = new TraceCreator(new File(repoPath).getCanonicalPath(), projectName, bugId, timeout);
        traceCreator.run();
        return getData(bugId);
    }

    private int getRootCauseNodeOrder(RootCause rootCause, Trace workingTrace) {
        List<TraceNode> executionList = workingTrace.getExecutionList();
        for (TraceNode traceNode : executionList) {
            BreakPoint breakPoint = traceNode.getBreakPoint();
            int lineNum = breakPoint.getLineNumber();
            String classCanonicalName = breakPoint.getDeclaringCompilationUnitName();
            if (classCanonicalName.equals(rootCause.className) && lineNum <= rootCause.endLineNum &&
                    lineNum >= rootCause.startLineNum) {
                return traceNode.getOrder();
            }
        }
        return -1;
    }

    public ProjectMinimizer createMinimizer(int bugId) {
        String bugIdStr = Integer.toString(bugId);
        return new ProjectMinimizer(repoPath, pathConfig.getRelativeBuggyPath(projectName, bugIdStr),
                pathConfig.getRelativeFixPath(projectName, bugIdStr), pathConfig.getRelativeMetadataPath(projectName, bugIdStr));
    }

    public void moveTraceFiles(int bugId) {
        String bugIdStr = Integer.toString(bugId);
        String pathToBuggyTrace = pathConfig.getInstrumentatorFilePath(projectName,
                bugIdStr, InstrumentatorFile.BUGGY_TRACE);
        String pathToWorkingTrace = pathConfig.getInstrumentatorFilePath(projectName,
                bugIdStr, InstrumentatorFile.TRACE);
        File buggyTraceFile = new File(pathToBuggyTrace);
        if (buggyTraceFile.exists()) {
            buggyTraceFile.renameTo(new File("D:\\chenghin\\NUS\\math_70-traces\\" +
                    bugIdStr + "\\" + InstrumentatorFile.getFileName(InstrumentatorFile.BUGGY_TRACE)));
        }
        File workingTraceFile = new File(pathToWorkingTrace);
        if (workingTraceFile.exists()) {
            workingTraceFile.renameTo(new File("D:\\chenghin\\NUS\\math_70-traces\\" +
                    bugIdStr + "\\" + InstrumentatorFile.getFileName(InstrumentatorFile.TRACE)));
        }
    }

    public RootCause parseRootCauseStr(String rootCauseStr) {
        // Example: MutationMathOperatorCommand#org.apache.commons.math.analysis.BinaryFunction#lines 37-37#[x + y]
        List<Integer> poundSymbolLocations = new ArrayList<>();
        for (int i = 0; i < rootCauseStr.length(); i++) {
            char c = rootCauseStr.charAt(i);
            if (c == '#') {
                poundSymbolLocations.add(i);
            }
        }
        String className = rootCauseStr.substring(poundSymbolLocations.get(0) + 1, poundSymbolLocations.get(1));
        String lineNumStr = rootCauseStr.substring(poundSymbolLocations.get(1) + 1, poundSymbolLocations.get(2));
        int idxOfDashInLineNumStr = lineNumStr.indexOf("-");
        String startLineStr = lineNumStr.substring(6, idxOfDashInLineNumStr); // skip the "lines" part
        int startLineNum = Integer.parseInt(startLineStr);
        String endLineStr = lineNumStr.substring(idxOfDashInLineNumStr + 1);
        int endLineNum = Integer.parseInt(endLineStr);
        return new RootCause(startLineNum, endLineNum, className);
    }

    public static class BugData {
        private final int rootCauseNode;
        private final Trace buggyTrace;
        private final Trace workingTrace;
        private final TestCase testCase;
        private final String projectName;

        private final String workingProjectPath;
        private final String buggyProjectPath;

        public BugData(int rootCauseNode, Trace buggyTrace, Trace workingTrace, String testCaseStr,
                       String projectName, String workingProjectPath, String buggyProjectPath) {
            this.rootCauseNode = rootCauseNode;
            this.buggyTrace = buggyTrace;
            this.workingTrace = workingTrace;
            this.testCase = formTestCase(testCaseStr);
            this.projectName = projectName;
            this.workingProjectPath = workingProjectPath;
            this.buggyProjectPath = buggyProjectPath;
        }

        public int getRootCauseNode() {
            return rootCauseNode;
        }

        public Trace getBuggyTrace() {
            return buggyTrace;
        }

        public Trace getWorkingTrace() {
            return workingTrace;
        }

        public TestCase getTestCase() {
            return testCase;
        }

        public String getProjectName() {
            return projectName;
        }

        public String getWorkingProjectPath() {
            return workingProjectPath;
        }

        public String getBuggyProjectPath() {
            return buggyProjectPath;
        }

        private TestCase formTestCase(String testCaseStr) {
            // Example: org.apache.commons.math.analysis.ComposableFunctionTest#testComposition(),54,102
            int idxOfPound = testCaseStr.indexOf("#");
            String testClassName = testCaseStr.substring(0, idxOfPound);
            String testMethodName = testCaseStr.substring(idxOfPound + 1, testCaseStr.indexOf("("));
            return new TestCase(testClassName, testMethodName, testCaseStr);
        }

        @Override
        public String toString() {
            return "BugData{" +
                    "rootCauseNode=" + rootCauseNode +
                    ", buggyTrace=" + buggyTrace +
                    ", workingTrace=" + workingTrace +
                    ", testCase=" + testCase +
                    ", projectName='" + projectName + '\'' +
                    ", workingProjectPath='" + workingProjectPath + '\'' +
                    ", buggyProjectPath='" + buggyProjectPath + '\'' +
                    '}';
        }
    }

    public static class RootCause {
        private final int startLineNum;
        private final int endLineNum;
        private final String className;

        public RootCause(int startLineNum, int endLineNum, String className) {
            this.startLineNum = startLineNum;
            this.endLineNum = endLineNum;
            this.className = className;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            RootCause rootCause = (RootCause) o;
            return startLineNum == rootCause.startLineNum && endLineNum == rootCause.endLineNum && Objects.equals(className, rootCause.className);
        }

        @Override
        public int hashCode() {
            return Objects.hash(startLineNum, endLineNum, className);
        }
    }
}
