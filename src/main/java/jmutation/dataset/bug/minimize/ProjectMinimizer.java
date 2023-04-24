package jmutation.dataset.bug.minimize;

import jmutation.dataset.bug.Log;
import jmutation.dataset.bug.minimize.diff.DiffParser;
import jmutation.dataset.bug.minimize.diff.GitWrapper;
import jmutation.dataset.bug.minimize.instruction.Instruction;
import jmutation.dataset.bug.minimize.instruction.Instruction.InstructionType;
import jmutation.dataset.bug.minimize.instruction.Metadata;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ProjectMinimizer {
    public static final String METADATA_FILE_NAME = "metadata.json";
    public static final String METADATA_DIR = "metadata";
    private final Logger logger = Log.createLogger(ProjectMinimizer.class);
    private final String buggyProject;
    private final String relativeBuggyProject;
    private final String workingProject;
    private final String relativeWorkingProject;
    private final String relativeMetadata;
    private final String metadataPath;

    public ProjectMinimizer(String repoPath, String relativeBuggyProject, String relativeWorkingProject,
                            String relativeMetadataPath) {
        this.relativeWorkingProject = relativeWorkingProject;
        this.relativeBuggyProject = relativeBuggyProject;
        this.relativeMetadata = relativeMetadataPath;
        this.buggyProject = repoPath + File.separator + relativeBuggyProject;
        this.workingProject = repoPath + File.separator + relativeWorkingProject;
        this.metadataPath = repoPath + File.separator + relativeMetadataPath;
    }

    public boolean minimize() {
        if (!(new File(workingProject).exists())) {
            logger.warning(workingProject + " does not exist");
            return false;
        }

        if (!(new File(buggyProject).exists())) {
            logger.warning(buggyProject + " does not exist");
            return false;
        }

        logger.info("Minimizing " + buggyProject);
        if (!metaDataExists()) {
            logger.info("Generating metadata for " + buggyProject);
            List<String> diffResult = GitWrapper.getRawDiff(workingProject, buggyProject);
            List<Instruction> instructionList = DiffParser.parse(diffResult, workingProject, buggyProject);
            List<String> filesToAdd = new ArrayList<>();
            for (Instruction instruction : instructionList) {
                if (instruction.instructionType().equals(InstructionType.ADD)) {
                    filesToAdd.add(instruction.pathInTarget());
                }
            }
            copyOverFiles(filesToAdd, metadataPath);
            Metadata metadata = generateMetadata(instructionList);
            MetadataWriter.write(metadataPath + File.separator + METADATA_FILE_NAME, metadata);
        }

        logger.info("Deleting " + buggyProject);
        try {
            FileUtils.deleteDirectory(new File(buggyProject));
        } catch (IOException e) {
            logger.log(Level.WARNING, e.getMessage(), e);
            return false;
        }
        logger.info("Finished minimizing " + buggyProject);
        return true;
    }

    private void copyOverFiles(List<String> filesToCopy, String targetPath) {
        for (String filePath : filesToCopy) {
            try {
                FileUtils.copyFileToDirectory(new File(buggyProject + File.separator + filePath),
                        new File(targetPath + File.separator + getDirectory(filePath)));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private Metadata generateMetadata(List<Instruction> instructionList) {
        return new Metadata(instructionList, relativeWorkingProject, relativeBuggyProject, relativeMetadata);
    }

    private String getDirectory(String file) {
        int lastIdxOfSeparator = file.lastIndexOf(File.separator);
        return file.substring(0, lastIdxOfSeparator);
    }

    public boolean maximise() {
        logger.info("Maximizing " + buggyProject);
        Metadata metadata;
        try {
            metadata = MetadataParser.parse(metadataPath + File.separator + METADATA_FILE_NAME);
        } catch (RuntimeException e) {
            logger.log(Level.WARNING, e.getMessage(), e);
            return false;
        }
        List<Instruction> instructionList = metadata.instructionList();
        try {
            FileUtils.copyDirectory(new File(workingProject), new File(buggyProject));
        } catch (IOException e) {
            logger.log(Level.WARNING, e.getMessage(), e);
            return false;
        }
        for (Instruction instruction : instructionList) {
            applyInstruction(instruction);
        }
        logger.info("Finished maximizing " + buggyProject);
        return true;
    }

    private void applyInstruction(Instruction instruction) {
        String fileInMetaData = metadataPath + File.separator + instruction.pathInMetadata();
        String destInBuggyTarget = buggyProject + File.separator + instruction.pathInTarget();
        try {
            FileUtils.copyFile(new File(fileInMetaData), new File(destInBuggyTarget), false);
        } catch (IOException e) {
            logger.log(Level.WARNING, e.getMessage(), e);
        }
    }

    public boolean metaDataExists() {
        return new File(metadataPath).exists();
    }
}
