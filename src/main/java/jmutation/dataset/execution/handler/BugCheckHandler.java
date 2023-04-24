package jmutation.dataset.execution.handler;

import jmutation.dataset.execution.Request;
import jmutation.dataset.trace.TraceCreator;

public class BugCheckHandler extends Handler {

    private final TraceCreator traceCreator;

    public BugCheckHandler(Handler next, String repositoryPath, String projectName, int bugId) {
        super(next);
        traceCreator = new TraceCreator(repositoryPath, projectName, bugId);
    }

    public BugCheckHandler(String repositoryPath, String projectName, int bugId) {
        super(new BaseHandler());
        traceCreator = new TraceCreator(repositoryPath, projectName, bugId);
    }

    @Override
    public boolean individualHandler(Request request) {
        // Check if traces are present.
        return traceCreator.isDone();
    }
}
