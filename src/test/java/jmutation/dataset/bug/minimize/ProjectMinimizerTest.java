package jmutation.dataset.bug.minimize;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProjectMinimizerTest {
    private final String FILE_ROOT = String.join(File.separator, "src", "test", "files", "jmutation", "dataset", "bug", "minimize");
    private ProjectMinimizer minimizer;

    @AfterEach
    public void afterEach() {
        restoreOriginalProjects();
    }

    @Test
    void minimize_maximizedProject_minimizesCorrectly() {
        final String repoPath = String.join(File.separator, FILE_ROOT, "sample-repo");
        final String relativeBuggyProject = String.join(File.separator, "sample-project", "1", "bug");
        final String relativeWorkingProject = String.join(File.separator, "sample-project", "fix");
        final String relativeMetadata = String.join(File.separator, "sample-project", "1", "metadata");
        minimizer = new ProjectMinimizer(repoPath, relativeBuggyProject, relativeWorkingProject,
                relativeMetadata);
        minimizer.minimize();
        // Check no more bug directory
        // Check has modified file directory
        // Read json file, and check its contents
        assertFalse(new File(String.join(File.separator, repoPath, relativeBuggyProject)).exists());
        final String metadataJsonPath = String.join(File.separator, repoPath, relativeMetadata, "metadata.json");
        assertTrue(new File(metadataJsonPath).exists());
        final String modifiedFilePath = String.join(File.separator, repoPath, relativeMetadata, "src", "main", "java", "sample", "Sample.java");
        assertTrue(new File(modifiedFilePath).exists());
    }

    @Test
    void minimize_hasMetadataAndProject_doesNotRegenerateMetadata() {
        final String repoPath = String.join(File.separator, FILE_ROOT, "sample-repo");
        final String relativeBuggyProject = String.join(File.separator, "sample-project", "2", "bug");
        final String relativeWorkingProject = String.join(File.separator, "sample-project", "fix");
        final String relativeMetadata = String.join(File.separator, "sample-project", "2", "metadata");
        final File metadataJsonFile = new File(String.join(File.separator, repoPath, relativeMetadata, "metadata.json"));
        final File changedFile = new File(String.join(File.separator, repoPath, relativeMetadata, "src", "main", "java", "sample", "Sample.java"));
        long expectedMetadataJsonModified = metadataJsonFile.lastModified();
        long expectedChangedFileModified = changedFile.lastModified();
        minimizer = new ProjectMinimizer(repoPath, relativeBuggyProject, relativeWorkingProject,
                relativeMetadata);
        assertTrue(minimizer.metaDataExists());
        minimizer.minimize();
        assertEquals(expectedMetadataJsonModified, metadataJsonFile.lastModified());
        assertEquals(expectedChangedFileModified, changedFile.lastModified());
    }

    void restoreOriginalProjects() {
        // clone original into sample-repo
        final String originalPath = String.join(File.separator, FILE_ROOT, "sample-repo-original");
        final String modifiedPath = String.join(File.separator, FILE_ROOT, "sample-repo");
        try {
            FileUtils.deleteDirectory(new File(modifiedPath));
            FileUtils.copyDirectory(new File(originalPath), new File(modifiedPath));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
