package jmutation.model.project;

import jmutation.parser.ProjectParser;

import java.io.File;

public class ProjectConfig {
    private final Project project;
    private final File dropInsDir;

    /**
     * Dataclass for various project settings, and a wrapper over the Project class
     *
     * @param projectPath
     * @param dropInsDir
     */
    public ProjectConfig(String projectPath, String dropInsDir) {
        File projectRoot = new File(projectPath);
        ProjectParser parser = new ProjectParser(projectRoot);
        this.project = parser.parse();
        this.dropInsDir = new File(dropInsDir);
    }

    public ProjectConfig(ProjectConfig config, Project newProject) {
        this.project = newProject;
        this.dropInsDir = config.dropInsDir;
    }

    public ProjectConfig(Project project) {
        this.project = project;
        dropInsDir = null;
    }

    public String getProjectPath() {
        return getProjectRoot().getAbsolutePath();
    }

    public File getProjectRoot() {
        return project.getRoot();
    }

    public Project getProject() {
        return this.project;
    }

    public String getCompileCommand() {
        return project.compileCommand();
    }

    public String getCleanCommand() {
        return project.cleanCommand();
    }

    public String getPackageCommand() {
        return project.packageCommand();
    }

    public String getDropInsDir() {
        return this.dropInsDir.getAbsolutePath();
    }

    public File getCompiledTestFolder() {
        return this.project.getCompiledTestFolder();
    }

    public File getCompiledClassFolder() {
        return this.project.getCompiledClassFolder();
    }

    public File getCompiledFolder() {
        return this.project.getCompiledFolder();
    }
}
