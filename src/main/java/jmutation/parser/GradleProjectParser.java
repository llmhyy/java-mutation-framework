package jmutation.parser;

import jmutation.constants.GradleConstants;
import org.gradle.api.Project;

import java.io.File;

class GradleProjectParser extends ProjectInfoParser {
    Project gradleProject;
    public GradleProjectParser(File root) {
        super(root);
    }
    String getSrcFolderPath() {
        return GradleConstants.SRC_FOLDER;
    }

    String getTestFolderPath() {
        return GradleConstants.TEST_FOLDER;
    }

    String getCompiledSrcFolderPath() {
        return GradleConstants.COMPILATION_FOLDER + File.separator +
                GradleConstants.COMPILED_CLASS_FOLDER;
    }

    String getCompiledTestFolderPath() {
        return GradleConstants.COMPILATION_FOLDER + File.separator +
                GradleConstants.TEST_CLASS_FOLDER;
    }
}
