package jmutation.execution;

import jmutation.model.ExecutionResult;
import jmutation.model.MicrobatConfig;
import jmutation.model.ProjectConfig;
import jmutation.model.TestCase;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ProjectExecutor extends Executor {
    private final ProjectConfig projectConfig;
    private final MicrobatConfig microbatConfig;
    private boolean compiled = false;
    public ProjectExecutor(MicrobatConfig microbatConfig, ProjectConfig proj) {
        super(proj.getProjectRoot());
        this.projectConfig = proj;
        this.microbatConfig = microbatConfig;
    }

    /**
     * Compiles the entire project, including test classes
     * @return stdout as String
     */
    public String compile() {
        return exec(projectConfig.getCompileCommand());
    }

    public ExecutionResult exec(TestCase testCase) {
        if (!compiled) {
            ExecutionResult out = new ExecutionResult(compile());
            if (!out.isSuccessful()) {
                return out;
            }
        }

        // include microbat details to instrument run
        InstrumentationCommandBuilder ib = new InstrumentationCommandBuilder(microbatConfig, projectConfig.getDropInsDir());
        ib.setTestCase(testCase); // set class and method name
        ib.addClassPath(projectConfig.getCompiledTestFolder()); // add target/test-classes
        ib.addClassPath(projectConfig.getCompiledClassFolder()); // add target/classes

        findJars().stream().forEach(file -> { // add jar files
            ib.addClassPath(file);
            ib.addExternalLibPath(file);
        });

        return instrumentationExec(ib);
    }

    public List<File> findJars() {
        return walk(projectConfig.getCompiledFolder());
    }

    private static List<File> walk(File start) {
            File[] list = start.listFiles();
            List<File> jarFiles = new ArrayList<>();

            for (File f : list) {
                if (f.isDirectory()) {
                    jarFiles.addAll(walk(f));
                } else {
                    if (f.getName().contains(".jar")) {
                        jarFiles.add(f);
                    }
                }
            }
            return jarFiles;
    }

    private ExecutionResult instrumentationExec(InstrumentationCommandBuilder instrumentationCommandBuilder) {
        return new ExecutionResult("");
    }
}
