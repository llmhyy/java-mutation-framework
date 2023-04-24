package jmutation.dataset.trace;

import jmutation.dataset.bug.Log;
import jmutation.dataset.bug.model.path.MutationFrameworkPathConfiguration;
import jmutation.dataset.bug.model.path.PathConfiguration;
import jmutation.dataset.bug.model.path.PathConfiguration.InstrumentatorFile;
import jmutation.dataset.bug.model.project.DatasetProject;
import jmutation.dataset.bug.model.project.MutationFrameworkDatasetProject;
import jmutation.model.PrecheckExecutionResult;
import jmutation.model.TestCase;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;

public class TraceCreator implements Runnable {
    private final Logger logger = Log.createLogger(TraceCreator.class);
    private final String projectName;
    private final int bugId;
    private final PathConfiguration pathConfig;
    private final int instrumentationTimeout;

    public TraceCreator(String repositoryPath, String projectName, int bugId) {
        this.projectName = projectName;
        this.bugId = bugId;
        pathConfig = new MutationFrameworkPathConfiguration(repositoryPath);
        instrumentationTimeout = 0;
    }

    public TraceCreator(String repositoryPath, String projectName, int bugId, int timeout) {
        this.projectName = projectName;
        this.bugId = bugId;
        pathConfig = new MutationFrameworkPathConfiguration(repositoryPath);
        instrumentationTimeout = timeout;
    }

    public void run() {
        if (isDone()) return;
        try {
            runPrecheckAndTraceCollection();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
    }

    private void runPrecheckAndTraceCollection() throws TimeoutException {
        // Get the path to buggy
        // Get names of the trace files
        String buggyPath = pathConfig.getBuggyPath(projectName, Integer.toString(bugId));
        String workingPath = pathConfig.getFixPath(projectName, Integer.toString(bugId));
        // Get test case file
        DatasetProject project = new MutationFrameworkDatasetProject(buggyPath);
        TestCase testCase = project.getFailingTests().get(0);
        TraceCollector workingTraceCollector = createTraceCollector(false, workingPath, testCase);
        logger.info(bugId + " : start working trace collection");
        Optional<PrecheckExecutionResult> precheckExecutionResultOptional = workingTraceCollector.executePrecheck();
        if (precheckExecutionResultOptional.isEmpty()) {
            logger.severe("Working precheck for project " + projectName + " " +
                    testCase + " for bugId " + bugId + " failed");
            return;
        }
        workingTraceCollector.executeTraceCollection(precheckExecutionResultOptional.get().getTotalSteps());
        logger.info(bugId + " : end working trace collection");
        TraceCollector buggyTraceCollector = createTraceCollector(true, buggyPath, testCase);
        logger.info(bugId + " : start buggy trace collection");
        Optional<PrecheckExecutionResult> buggyPrecheckExecutionResultOptional = buggyTraceCollector.executePrecheck();
        if (buggyPrecheckExecutionResultOptional.isEmpty()) {
            logger.severe("Buggy precheck for project " + projectName + " " +
                    testCase + " for bugId " + bugId + " failed");
            return;
        }
        buggyTraceCollector.executeTraceCollection(buggyPrecheckExecutionResultOptional.get().getTotalSteps());
        logger.info(bugId + " : end buggy trace collection");
    }

    public void runTraceCollection(int workingSteps, int buggySteps) {
        try {
            String buggyPath = pathConfig.getBuggyPath(projectName, Integer.toString(bugId));
            String workingPath = pathConfig.getFixPath(projectName, Integer.toString(bugId));
            // Get test case file
            DatasetProject project = new MutationFrameworkDatasetProject(buggyPath);
            TestCase testCase = project.getFailingTests().get(0);
            TraceCollector workingTraceCollector = createTraceCollector(false, workingPath, testCase);
            logger.info(bugId + " : start working trace collection");
            workingTraceCollector.executeTraceCollection(workingSteps);
            logger.info(bugId + " : end working trace collection");
            TraceCollector buggyTraceCollector = createTraceCollector(true, buggyPath, testCase);
            logger.info(bugId + " : start buggy trace collection");
            buggyTraceCollector.executeTraceCollection(buggySteps);
            logger.info(bugId + " : end buggy trace collection");
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
    }

    private TraceCollector createTraceCollector(boolean isBuggy, String pathToProject, TestCase testCase) {
        String bugId = Integer.toString(this.bugId);
        if (isBuggy) {
            return new TraceCollector(pathToProject, testCase,
                    pathConfig.getInstrumentatorFilePath(projectName, bugId, InstrumentatorFile.BUGGY_PRECHECK),
                    pathConfig.getInstrumentatorFilePath(projectName, bugId, InstrumentatorFile.BUGGY_TRACE),
                    null,
                    instrumentationTimeout);
        }
        return new TraceCollector(pathToProject, testCase,
                pathConfig.getInstrumentatorFilePath(projectName, bugId, InstrumentatorFile.PRECHECK),
                pathConfig.getInstrumentatorFilePath(projectName, bugId, InstrumentatorFile.TRACE),
                null,
                instrumentationTimeout);
    }


    public boolean isDone() {
        String bugId = Integer.toString(this.bugId);
        List<String> filePaths = new ArrayList<>();
        for (InstrumentatorFile fileType : InstrumentatorFile.values()) {
            if (fileType.equals(InstrumentatorFile.PRECHECK) || fileType.equals(InstrumentatorFile.BUGGY_PRECHECK) ||
                    fileType.equals(InstrumentatorFile.BUGGY_TRACE_W_ASSERTS) || fileType.equals(InstrumentatorFile.TRACE_W_ASSERTS))
                continue;
            filePaths.add(pathConfig.getInstrumentatorFilePath(projectName, bugId, fileType));
        }
        for (String filePath : filePaths) {
            File file = new File(filePath);
            if (!file.exists()) {
                return false;
            }
        }
        return true;
    }
}
