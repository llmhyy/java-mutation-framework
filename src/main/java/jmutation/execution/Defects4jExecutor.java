package jmutation.execution;

import jmutation.model.project.Defects4jProject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Did not extend ProjectExecutor as it is too tightly coupled to microbat
 * TODO: Make ProjectExecutor interface more generic, so that it can handle Defects4j + Microbat based commands. Or rename ProjectExecutor to MicrobatExecutor?.
 */
public class Defects4jExecutor extends Executor {
    public final static String PROPERTY_CLASSES_MODIFIED = "classes.modified";
    public final static String PROPERTY_SRC_DIR = "dir.src.classes";
    public final static String PROPERTY_TARGET_DIR = "dir.bin.classes";
    public final static String PROPERTY_TEST_SRC_DIR = "dir.src.tests";
    public final static String PROPERTY_TEST_TARGET_DIR = "dir.bin.tests";
    private final Defects4jProject project;
    // As the executor runs with project's directory as the working directory, if the proj is not yet checkedout (directory does not exist yet), will throw error
    // executorUtil is to run commands before checkout is executed

    public Defects4jExecutor(Defects4jProject project) {
        super(project.getRoot());
        this.project = project;
    }

    public String compile() {
        return exec(project.compileCommand());
    }

    public List<String> getClassDiffs() {
        String classesModifiedStr = export(PROPERTY_CLASSES_MODIFIED);
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

    public static Defects4jProject checkout(String project, String version, String path) {
        Executor executor = new Executor(new File(System.getProperty("user.dir")));
        executor.exec("java -version");
        executor.exec("mkdir -p " + path);
        executor.exec(Defects4jProject.checkoutCommand(project, version, path));
        executor = new Executor(new File(path));
        String srcFolder = getLineAfterRunning(executor.exec(Defects4jProject.exportCommand(PROPERTY_SRC_DIR)));
        String targetFolder = getLineAfterRunning(executor.exec(Defects4jProject.exportCommand(PROPERTY_TARGET_DIR)));
        String testSrcFolder = getLineAfterRunning(executor.exec(Defects4jProject.exportCommand(PROPERTY_TEST_SRC_DIR)));
        String testTargetFolder = getLineAfterRunning(executor.exec(Defects4jProject.exportCommand(PROPERTY_TEST_TARGET_DIR)));
        return new Defects4jProject(project + "_" + version, new File(path),
                null, srcFolder, testSrcFolder, targetFolder, testTargetFolder);
    }

    public String mutate() {
        return exec(project.mutationCommand());
    }

    private static String getLineAfterRunning(String str) {
        String runningDefects4J = "Running ant";
        String[] lines = str.split(System.lineSeparator());
        boolean afterRunning = false;
        for (String line : lines) {
            if (afterRunning) {
               return line;
            } else if (line.startsWith(runningDefects4J)) {
               afterRunning = true;
            }
        }
        return "";
    }
}
