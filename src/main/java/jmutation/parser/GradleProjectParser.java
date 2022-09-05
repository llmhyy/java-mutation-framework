package jmutation.parser;

import jmutation.constants.GradleConstants;
import org.gradle.tooling.GradleConnector;
import org.gradle.tooling.ProjectConnection;
import org.gradle.tooling.model.GradleProject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static jmutation.constants.GradleConstants.RELATIVE_COMPILATION_FOLDER_FROM_BUILD;

class GradleProjectParser extends ProjectInfoParser {
    GradleProject project;
    public GradleProjectParser(File root) {
        super(root);
        ProjectConnection connection = GradleConnector.newConnector()
            .forProjectDirectory(root)
            .connect();
        project = connection.getModel(GradleProject.class);
    }

    @Override
    String getProjectName() {
        String projName = project.getName();
        if (projName == null) {
            return super.getProjectName();
        }
        return projName;
    }

    String getSrcFolderPath() {
        return GradleConstants.SRC_FOLDER;
    }

    String getTestFolderPath() {
        return GradleConstants.TEST_FOLDER;
    }

    String getCompiledSrcFolderPath() {
        return getCompilationFolderPath() + File.separator +
                GradleConstants.COMPILED_CLASS_FOLDER;
    }

    String getCompiledTestFolderPath() {
        return getCompilationFolderPath() + File.separator +
                GradleConstants.TEST_CLASS_FOLDER;
    }

    String getCompilationFolderPath() {
        File buildDirectory = project.getBuildDirectory();
        if (buildDirectory != null) {
            Path relativePathToBuildFolder = Paths.get(getRoot().getAbsolutePath()).relativize(Paths.get(buildDirectory.getPath()));
            return "./" + relativePathToBuildFolder + File.separator + RELATIVE_COMPILATION_FOLDER_FROM_BUILD;
        }
        return GradleConstants.COMPILATION_FOLDER;
    }
}
