package jmutation.dataset.execution.handler;

import jmutation.dataset.execution.Request;
import jmutation.dataset.trace.TraceCreator;

public class PrecheckTraceCollectionHandler extends Handler {
    private final TraceCreator traceCreator;

    public PrecheckTraceCollectionHandler(String repositoryPath, String projectName, int bugId, int timeout) {
        super(new BaseHandler());
        traceCreator = new TraceCreator(repositoryPath, projectName, bugId, timeout);
    }

    public PrecheckTraceCollectionHandler(Handler next, String repoPath, String projectName, int bugId, int timeout) {
        super(next);
        traceCreator = new TraceCreator(repoPath, projectName, bugId, timeout);
    }

    @Override
    public boolean individualHandler(Request request) {
        // do trace collection for working and buggy
        try {
            traceCreator.run();
        } catch (RuntimeException e) {
            e.printStackTrace();
        } finally {
            return true;
        }
    }
}
