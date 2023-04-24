package jmutation.dataset.bug.model.path;

import java.io.File;

import static jmutation.dataset.bug.minimize.ProjectMinimizer.METADATA_DIR;
import static jmutation.dataset.bug.minimize.ProjectMinimizer.METADATA_FILE_NAME;

/**
 * Obtains paths to buggy and working project from dataset for mutation framework
 */
public class MutationFrameworkPathConfiguration extends PathConfiguration {
    private static final String BUGGY_DIR = "bug";
    private static final String WORKING_DIR = "fix";
    private static final String STORAGE_FILE = "createdBugs.json";
    private static final String LABEL_FILE = "label.json";

    public MutationFrameworkPathConfiguration(String repoPath) {
        super(repoPath);
    }

    @Override
    public String getBugPath(String projectName, String bugId) {
        return String.join(File.separator, repoPath, projectName, bugId);
    }

    @Override
    public String getBuggyPath(String projectName, String bugId) {
        return String.join(File.separator, repoPath, projectName, bugId, BUGGY_DIR);
    }

    @Override
    public String getFixPath(String projectName, String bugId) {
        return String.join(File.separator, repoPath, projectName, WORKING_DIR);
    }

    @Override
    public String getRelativeBuggyPath(String projectName, String bugId) {
        return String.join(File.separator, projectName, bugId, BUGGY_DIR);
    }

    @Override
    public String getRelativeFixPath(String projectName, String bugId) {
        return String.join(File.separator, projectName, WORKING_DIR);
    }

    @Override
    public String getMetadataPath(String projectName, String bugId) {
        return String.join(File.separator, repoPath, projectName, bugId, METADATA_DIR);
    }

    @Override
    public String getRelativeMetadataPath(String projectName, String bugId) {
        return String.join(File.separator, projectName, bugId, METADATA_DIR);
    }

    @Override
    public String getRestorationInstructionsPath(String projectName, String bugId) {
        return String.join(File.separator, getMetadataPath(projectName, bugId), METADATA_FILE_NAME);
    }

    @Override
    public String getRelativeRestorationInstructionsPath(String projectName, String bugId) {
        return String.join(File.separator, projectName, bugId, METADATA_DIR, METADATA_FILE_NAME);
    }

    @Override
    public String getLabelPath(String projectName, String bugId) {
        return String.join(File.separator, getBugPath(projectName, bugId), LABEL_FILE);
    }

    @Override
    public String getRelativeLabelPath(String projectName, String bugId) {
        return String.join(File.separator, projectName, bugId, LABEL_FILE);
    }

    @Override
    public String getStoragePath(String projectName) {
        return String.join(File.separator, repoPath, projectName, STORAGE_FILE);
    }

    @Override
    public String getInstrumentatorFilePath(String projectName, String bugId, InstrumentatorFile fileType) {
        String fileName = InstrumentatorFile.getFileName(fileType);
        return String.join(File.separator, getBugPath(projectName, bugId), fileName);
    }
}
