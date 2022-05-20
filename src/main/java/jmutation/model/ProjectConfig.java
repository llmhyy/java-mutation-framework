package jmutation.model;

import java.io.File;

public class ProjectConfig {
    private final String projectPath;

    public ProjectConfig(String projectPath) {
        this.projectPath = new File(projectPath).getAbsolutePath();
    }

    public String getProjectPath() {
        return projectPath;
    }
}
