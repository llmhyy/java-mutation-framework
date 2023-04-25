package jmutation.dataset.execution;

import jmutation.dataset.execution.handler.BugCheckHandler;
import jmutation.dataset.execution.handler.Handler;
import jmutation.dataset.execution.handler.MaximizeHandler;
import jmutation.dataset.execution.handler.MinimizeHandler;
import jmutation.dataset.execution.handler.PrecheckTraceCollectionHandler;

public class MinimizeRunner implements Runnable {
    private final Handler startHandler;
    private final MinimizeHandler minimizeHandler;

    public MinimizeRunner(String repoPath, String projectName, int bugId, int instrumentationTimeout) {
        // check if is done (both trace and label) -> maximise -> do precheck & trace + label collection (check if is done).
        // Minimize regardless of failure
        PrecheckTraceCollectionHandler traceHandler = new PrecheckTraceCollectionHandler(repoPath, projectName, bugId,
                instrumentationTimeout);
        MaximizeHandler maximizeHandler = new MaximizeHandler(traceHandler, repoPath, projectName, bugId);
        BugCheckHandler checkHandler = new BugCheckHandler(maximizeHandler, repoPath, projectName, bugId);
        startHandler = checkHandler;
        minimizeHandler = new MinimizeHandler(repoPath, projectName, bugId);
    }

    @Override
    public void run() {
//        startHandler.handle(new Request(true));
        minimizeHandler.handle(new Request(true));
    }
}
