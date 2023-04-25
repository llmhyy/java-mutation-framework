package jmutation.dataset.execution;

import jmutation.dataset.bug.creator.BuggyProjectCreator;
import jmutation.dataset.bug.model.BuggyProject;
import jmutation.dataset.execution.handler.BuggyProjectCreationHandler;
import jmutation.dataset.execution.handler.DeleteBugDirHandler;
import jmutation.dataset.execution.handler.Handler;
import jmutation.dataset.execution.handler.MinimizeHandler;
import jmutation.dataset.execution.handler.ZipBugDirHandler;

import java.io.File;

import static jmutation.dataset.constants.FileNames.CREATED_BUGGY_PROJECT_FILE;

public class DatasetWithoutTracesCreationRunner implements Runnable {
    private final Handler deleteBugDirHandler;
    private final Handler startHandler;

    public DatasetWithoutTracesCreationRunner(String repoPath, String projectName, int bugId,
                                              BuggyProject buggyProject, String pathToBugDir, String pathToOriginalProj, int timeout) {

        deleteBugDirHandler = new DeleteBugDirHandler(pathToBugDir);
        ZipBugDirHandler zipBugDirHandler = new ZipBugDirHandler(pathToBugDir);
        MinimizeHandler minimizeHandler = new MinimizeHandler(zipBugDirHandler, repoPath, projectName, bugId);
        BuggyProjectCreationHandler buggyProjectCreationHandler =
                new BuggyProjectCreationHandler(minimizeHandler, new BuggyProjectCreator(repoPath,
                        pathToOriginalProj, buggyProject,
                        String.join(File.separator, repoPath, projectName, CREATED_BUGGY_PROJECT_FILE),
                        bugId, timeout));
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
