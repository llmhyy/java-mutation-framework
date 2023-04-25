package jmutation.dataset.execution;

import jmutation.dataset.bug.creator.BuggyProjectCreator;
import jmutation.dataset.bug.model.BuggyProject;
import jmutation.dataset.execution.handler.BugCheckHandler;
import jmutation.dataset.execution.handler.BuggyProjectCreationHandler;
import jmutation.dataset.execution.handler.DeleteBugDirHandler;
import jmutation.dataset.execution.handler.Handler;
import jmutation.dataset.execution.handler.MinimizeHandler;
import jmutation.dataset.execution.handler.TraceCollectionHandler;
import jmutation.dataset.execution.handler.ZipBugDirHandler;

import java.io.File;

public class DataSetCreationRunner implements Runnable {
    private final Handler startHandler;
    private final Handler deleteBugDirHandler;
    private final String CREATED_BUGGY_PROJECT_FILE = "createdBugs.json";

    /**
     * Set up handlers to
     * 1. Create buggy project in repo/projName/bugId/bug
     * 2. Collect working and buggy trace
     * 3. Check if traces were collected successfully
     * 4a. If trace collection failed, delete the dir and return
     * 4b. Else do nothing
     * 5. Minimize repo/projName/bugId/bug dir
     * 6. Zip the repo/projName/bugId dir
     *
     * @param repoPath
     * @param projectName
     * @param bugId
     * @param instrumentationTimeout
     * @param buggyProject
     * @param pathToBugDir
     */
    public DataSetCreationRunner(String repoPath, String projectName, int bugId, int instrumentationTimeout,
                                 BuggyProject buggyProject, String pathToBugDir, String pathToOriginalProj,
                                 int buggySteps, int workingSteps) {

        deleteBugDirHandler = new DeleteBugDirHandler(pathToBugDir);

        ZipBugDirHandler zipBugDirHandler = new ZipBugDirHandler(pathToBugDir);
        MinimizeHandler minimizeHandler = new MinimizeHandler(zipBugDirHandler, repoPath, projectName, bugId);
        BugCheckHandler checkHandler = new BugCheckHandler(minimizeHandler, repoPath, projectName, bugId);
        TraceCollectionHandler traceHandler = new TraceCollectionHandler(checkHandler, repoPath,
                projectName, bugId, instrumentationTimeout, buggySteps, workingSteps);

        BuggyProjectCreationHandler buggyProjectCreationHandler =
                new BuggyProjectCreationHandler(traceHandler, new BuggyProjectCreator(repoPath,
                        pathToOriginalProj, buggyProject,
                        repoPath + File.separator + CREATED_BUGGY_PROJECT_FILE, bugId));
        startHandler = buggyProjectCreationHandler;
    }

    @Override
    public void run() {
        try {
            startHandler.handle(new Request(true));
        } catch (RuntimeException e) {
            e.printStackTrace();
        } finally {
            // Delete the bug dir anyway, since either pass or fail, it is no longer necessary
            deleteBugDirHandler.handle(new Request(true));
        }
    }
}
