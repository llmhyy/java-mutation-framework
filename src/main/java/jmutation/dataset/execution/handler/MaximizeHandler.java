package jmutation.dataset.execution.handler;

import jmutation.dataset.bug.minimize.ProjectMinimizer;
import jmutation.dataset.bug.model.path.MutationFrameworkPathConfiguration;
import jmutation.dataset.bug.model.path.PathConfiguration;
import jmutation.dataset.execution.Request;

import java.io.File;

import static jmutation.dataset.constants.FileNames.BUGGY_PROJECT_DIR;
import static jmutation.dataset.constants.FileNames.WORKING_PROJECT_DIR;

public class MaximizeHandler extends Handler {
    private final ProjectMinimizer minimizer;

    public MaximizeHandler(Handler next, String repositoryPath, String projectName, int bugId) {
        super(next);
        minimizer = createMinimizer(repositoryPath, projectName, bugId);
    }

    static ProjectMinimizer createMinimizer(String repositoryPath, String projectName, int bugId) {
        PathConfiguration pathConfiguration = new MutationFrameworkPathConfiguration(repositoryPath);
        String metadataPath = pathConfiguration.getRelativeMetadataPath(projectName, Integer.toString(bugId));
        return new ProjectMinimizer(repositoryPath, String.join(File.separator,
                projectName, Integer.toString(bugId), BUGGY_PROJECT_DIR),
                String.join(File.separator, projectName, WORKING_PROJECT_DIR),
                metadataPath);
    }

    @Override
    public boolean individualHandler(Request request) {
        return minimizer.maximise();
    }
}
