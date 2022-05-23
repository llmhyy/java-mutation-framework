package jmutation.parser;

import jmutation.model.Project;
import jmutation.model.ProjectConfig;

import java.io.File;

/**
 * Given a maven or gradle project path, we parse it into a project
 *
 * @author Yun Lin
 *
 */
public class ProjectParser {
	private final ProjectConfig projectConfig;
	private Project project;
	public ProjectParser(ProjectConfig projectConfig) {
		this.projectConfig = projectConfig;
	}

	public Project parse() {
		if (project == null) {
			File projectDir = new File(projectConfig.getProjectPath());
			// traverse project differently depending on project type ?
			this.project = new Project(projectDir);
		}
		return this.project;
	}
}
