package jmutation.parser;

import jmutation.compile.ProjectCompiler;
import jmutation.model.Project;
import jmutation.model.ProjectConfig;
import jmutation.model.TestCase;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

/**
 * Given a maven or gradle project path, we parse it into a project
 *
 * @author Yun Lin
 *
 */
public class ProjectParser {
	private final ProjectConfig projectConfig;
	private Project project;
	private final File root;
	public ProjectParser(ProjectConfig projectConfig) {
		this.projectConfig = projectConfig;
		root = new File(projectConfig.getProjectPath());
	}

	public Project parse() {
		if (project == null) {
			// traverse project differently depending on project type ?
			this.project = new Project(root, walk(root));
		}
		return this.project;
	}

	private List<TestCase> walk(File start) {
		File[] list = start.listFiles();
		List<TestCase> testCases = new ArrayList<>();

		for (File f : list) {
			if (f.isDirectory()) {
				testCases.addAll(walk(f));
			} else {
				// 1. use detection regminer detection logic
				try {
					String fileContent = Files.readString(f.toPath());
					if (isTestSuite(fileContent)) {
						// get walk code and retrieve all methods
						// 2. use regminer jdt ast Visitor code from regminer to identify testcases
						testCases.addAll(ProjectCompiler.getAllMethod(fileContent));
						System.out.print(f.getAbsolutePath());
						System.out.print(" => ");
						System.out.println(testCases);
					}
				} catch (IOException e) {
					System.out.print("Unable to open file at ");
					System.out.println(f.getAbsolutePath());
				}
			}
		}
		return testCases;
	}

	private boolean isTestSuite(String code) {
		return code.contains("junit") || code.contains("@Test");
	}
}
