package jmutation.dataset.bug.model.project;

import jmutation.model.TestCase;

import java.util.List;

public abstract class DatasetProject {
    protected String projectPath;

    protected DatasetProject(String projectPath) {
        this.projectPath = projectPath;
    }

    public abstract List<TestCase> getFailingTests();
}
