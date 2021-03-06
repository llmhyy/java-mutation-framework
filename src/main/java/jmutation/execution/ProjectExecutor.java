package jmutation.execution;

import jmutation.model.ExecutionResult;
import jmutation.model.MicrobatConfig;
import jmutation.model.ProjectConfig;
import jmutation.model.TestCase;
import jmutation.parser.ProjectParser;
import jmutation.trace.FileReader;
import microbat.model.BreakPoint;
import microbat.model.trace.Trace;
import microbat.model.trace.TraceNode;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
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

    private static List<File> walk(File start) {
        File[] list = start.listFiles();
        if (list == null) {
            return new ArrayList<>();
        }

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

    /**
     * Compiles the entire project, including test classes
     *
     * @return stdout as String
     */
    public String compile() {
        return exec(projectConfig.getCompileCommand());
    }

    public String packageProj() {
        return exec(projectConfig.getPackageCommand());
    }

    public ExecutionResult exec(TestCase testCase) {
        if (!compiled) {
            ExecutionResult out = new ExecutionResult(compile());
            if (!out.isSuccessful()) {
                return out;
            }
        }
        String dumpFilePath = microbatConfig.getDumpFilePath();
        try {
            File microbatDumpFile = new File(dumpFilePath);
            boolean dumpFileCreated = microbatDumpFile.createNewFile();
            if (dumpFileCreated) {
                System.out.println("New dump file created at " + dumpFilePath);
            }
        } catch (IOException e) {
            throw new RuntimeException("Could not create dump file at " + dumpFilePath);
        }
        // include microbat details to instrument run
        InstrumentationCommandBuilder ib = new InstrumentationCommandBuilder(microbatConfig, projectConfig.getDropInsDir());
        ib.setTestCase(testCase); // set class and method name
        ib.addClassPath(projectConfig.getCompiledTestFolder()); // add target/test-classes
        ib.addClassPath(projectConfig.getCompiledClassFolder()); // add target/classes
        ib.setWorkingDirectory(projectConfig.getProjectRoot());

        findJars().stream().forEach(file -> { // add jar files
            ib.addClassPath(file);
            ib.addExternalLibPath(file);
        });

        return instrumentationExec(ib);
    }

    public List<File> findJars() {
        return walk(projectConfig.getCompiledFolder());
    }

    private ExecutionResult instrumentationExec(InstrumentationCommandBuilder instrumentationCommandBuilder) {
        String commandStr = instrumentationCommandBuilder.generateCommand();
        String executionResultStr = exec(commandStr);
        String traceFilePath = instrumentationCommandBuilder.getTraceFilePath();
        FileReader traceFileReader;
        try {
            traceFileReader = new FileReader(traceFilePath);
        } catch (FileNotFoundException e) {
            throw new RuntimeException("File " + traceFilePath + " not found");
        }
        Trace trace = traceFileReader.readTrace();
        setClassPathsToBreakpoints(trace);
        Coverage coverage = new Coverage(trace);
        ExecutionResult executionResult = new ExecutionResult(executionResultStr);
        executionResult.setCoverage(coverage);
        return executionResult;
    }

    private void setClassPathsToBreakpoints(Trace trace) {
        List<TraceNode> executionList = trace.getExecutionList();
        for (TraceNode traceNode : executionList) {
            BreakPoint breakPoint = traceNode.getBreakPoint();
            File breakPointFile = ProjectParser.getFileOfClass(breakPoint.getClassCanonicalName(), projectConfig.getProjectRoot());
            String breakPointFilePath = breakPointFile.getAbsolutePath();
            breakPoint.setFullJavaFilePath(breakPointFilePath);
        }
    }
}
