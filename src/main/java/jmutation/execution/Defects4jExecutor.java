package jmutation.execution;

import jmutation.model.project.Defects4jProject;

import java.util.List;

/**
 * Did not extend ProjectExecutor as it is too tightly coupled to microbat
 * TODO: Make ProjectExecutor interface more generic, so that it can handle Defects4j + Microbat based commands. Or restructure class hierarchy/use composition for Executors.
 */
public class Defects4jExecutor extends Executor {
    private final Defects4jProject project;

    public Defects4jExecutor(Defects4jProject project) {
        super(project.getRoot());
        this.project = project;
    }

    public String compile() {
        return exec(project.compileCommand());
    }

    public List<String> getClassDiffs() {
        String classesModifiedStr = export("classes.modified");
        String[] classesModified = classesModifiedStr.split(System.lineSeparator());
        return List.of(classesModified);
    }

    public String export(String query) {
        return exec(project.exportCommand(query));
    }

    public String test() {
        return exec(project.testCommand());
    }

    public String checkout(String project, String version, String path) {
        return exec(Defects4jProject.checkoutCommand(project, version, path));
    }
}
