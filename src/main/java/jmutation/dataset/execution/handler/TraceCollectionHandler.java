package jmutation.dataset.execution.handler;

import jmutation.dataset.execution.Request;
import jmutation.dataset.trace.TraceCreator;

public class TraceCollectionHandler extends Handler {
    private final TraceCreator traceCreator;
    private final int buggySteps;
    private final int workingSteps;

    public TraceCollectionHandler(String repositoryPath, String projectName, int bugId, int timeout,
                                  int buggySteps, int workingSteps) {
        super(new BaseHandler());
        traceCreator = new TraceCreator(repositoryPath, projectName, bugId, timeout);
        this.buggySteps = buggySteps;
        this.workingSteps = workingSteps;
    }

    public TraceCollectionHandler(Handler next, String repoPath, String projectName, int bugId, int timeout,
                                  int buggySteps, int workingSteps) {
        super(next);
        traceCreator = new TraceCreator(repoPath, projectName, bugId, timeout);
        this.buggySteps = buggySteps;
        this.workingSteps = workingSteps;
    }

    @Override
    public boolean individualHandler(Request request) {
        // do trace collection for working and buggy
        try {
            traceCreator.runTraceCollection(workingSteps, buggySteps);
        } catch (RuntimeException e) {
            e.printStackTrace();
        } finally {
            return true;
        }
    }
}
