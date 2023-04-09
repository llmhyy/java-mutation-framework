package jmutation.execution;

import jmutation.model.project.Defects4jProject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Did not extend ProjectExecutor as it is too tightly coupled to microbat
 * TODO: Make ProjectExecutor interface more generic, so that it can handle Defects4j + Microbat based commands. Or restructure class hierarchy/use composition for Executors.
 */
public class Defects4jExecutor extends Executor {
    private final Defects4jProject project;
    // As the executor runs with project's directory as the working directory, if the proj is not yet checkedout (directory does not exist yet), will throw error
    // executorUtil is to run commands before checkout is executed
    private final Executor executorUtil;

    public Defects4jExecutor(Defects4jProject project) {
        super(project.getRoot());
        executorUtil = new Executor(new File(System.getProperty("user.dir")));
        this.project = project;
    }

    public String compile() {
        return exec(project.compileCommand());
    }

    public List<String> getClassDiffs() {
        String classesModifiedQuery = "classes.modified";
        String classesModifiedStr = export(classesModifiedQuery);
        String[] classesModified = classesModifiedStr.split(System.lineSeparator());
        List<String> result = new ArrayList<>();
        boolean isClassName = false;
        for (int idx = 0; idx < classesModified.length; idx++) {
            if (isClassName) {
                result.add(classesModified[idx]);
            }
            if (classesModified[idx].startsWith("Running ant")) {
                isClassName = true;
            }
        }
        return result;
    }

    public String export(String query) {
        return exec(project.exportCommand(query));
    }

    public String test() {
        return exec(project.testCommand());
    }

    public String checkout(String project, String version, String path) {
        executorUtil.exec("mkdir -p " + path);
        return exec(Defects4jProject.checkoutCommand(project, version, path));
    }
}
