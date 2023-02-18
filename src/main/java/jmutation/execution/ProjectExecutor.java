package jmutation.execution;

import jmutation.execution.output.MicrobatOutputHandler.MicrobatOutputHandlerBuilder;
import jmutation.execution.output.OutputHandler.OutputHandlerBuilder;
import jmutation.model.MicrobatConfig;
import jmutation.model.PrecheckExecutionResult;
import jmutation.model.TestCase;
import jmutation.model.TraceCollectionResult;
import jmutation.model.project.ProjectConfig;
import microbat.instrumentation.output.RunningInfo;
import microbat.instrumentation.precheck.PrecheckInfo;
import org.codehaus.plexus.util.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeoutException;

import static jmutation.utils.TraceHelper.setClassPathsToBreakpoints;
import static jmutation.utils.TraceHelper.setClassPathsToClassLocations;

/**
 * Set up and calls commands on the project (compile, instrumentation, etc)
 */
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

    public ProjectExecutor setMicrobatConfig(MicrobatConfig microbatConfig) {
        return new ProjectExecutor(microbatConfig, projectConfig);
    }

    /**
     * Compiles the entire project, including test classes
     *
     * @return stdout as String
     */
    public String compile() {
        return exec(projectConfig.getCompileCommand());
    }

    public String setupDependencies() {
        return exec(projectConfig.getSetupExternalDependenciesCommand());
    }

    public String clean() {
        return exec(projectConfig.getCleanCommand());
    }

    public String packageProj() {
        return exec(projectConfig.getPackageCommand());
    }

    public TraceCollectionResult exec(TestCase testCase) {
        return exec(testCase, true);
    }

    public TraceCollectionResult exec(TestCase testCase, boolean shouldDeleteDumpFile) {
        try {
            return exec(testCase, shouldDeleteDumpFile, 0, false);
        } catch (TimeoutException e) {
            // Should not happen
            return null;
        }
    }

    public TraceCollectionResult exec(TestCase testCase, boolean shouldDeleteDumpFile, int timeout,
                                      boolean isDebugMode) throws TimeoutException {
        if (!compiled) {
            TraceCollectionResult out = new TraceCollectionResult(compile(), null);
            setupDependencies();
            if (!out.isSuccessful()) {
                return out;
            }
            compiled = true;
        }

        InstrumentationCommandBuilder ib = setUpForInstrumentation(testCase, false);
        ib.setDebugMode(isDebugMode);
        TraceCollectionResult result = instrumentationExec(ib, timeout);

        if (shouldDeleteDumpFile) {
            deleteDumpFile();
        }

        return result;
    }

    public List<File> findJars() {
        return walk(projectConfig.getCompiledFolder());
    }

    public PrecheckExecutionResult execPrecheck(TestCase testCase, boolean shouldDeleteDumpFile, int timeout,
                                                boolean isDebugMode)
            throws TimeoutException {
        if (!compiled) {
            PrecheckExecutionResult out = new PrecheckExecutionResult(compile(), null);
            setupDependencies();
            if (!out.isSuccessful()) {
                return out;
            }
            compiled = true;
        }
        InstrumentationCommandBuilder ib = setUpForInstrumentation(testCase, true);
        ib.setDebugMode(isDebugMode);
        PrecheckExecutionResult result = precheckExec(ib, testCase, timeout);
        if (shouldDeleteDumpFile) {
            deleteDumpFile();
        }
        return result;
    }

    private TraceCollectionResult instrumentationExec(InstrumentationCommandBuilder instrumentationCommandBuilder,
                                                      int timeout) throws TimeoutException {
        String commandStr = instrumentationCommandBuilder.generateCommand();
        setOutputHandlerBuilder(new MicrobatOutputHandlerBuilder());
        String executionResultStr = exec(commandStr, timeout);
        setOutputHandlerBuilder(new OutputHandlerBuilder());
        String dumpFilePath = instrumentationCommandBuilder.getDumpFilePath();
        RunningInfo runningInfo = RunningInfo.readFromFile(dumpFilePath);
        setClassPathsToBreakpoints(runningInfo.getMainTrace(), projectConfig.getProjectRoot());
        TraceCollectionResult executionResult = new TraceCollectionResult(executionResultStr, runningInfo);
        executionResult.setInstrumentationResult(runningInfo);
        return executionResult;
    }

    private PrecheckExecutionResult precheckExec(InstrumentationCommandBuilder instrumentationCommandBuilder,
                                                 TestCase testCase, int timeout) throws TimeoutException {
        String commandStr = instrumentationCommandBuilder.generateCommand();
        setOutputHandlerBuilder(new MicrobatOutputHandlerBuilder());
        String executionResultStr = exec(commandStr, timeout);
        setOutputHandlerBuilder(new OutputHandlerBuilder());
        String dumpFilePath = instrumentationCommandBuilder.getDumpFilePath();
        PrecheckInfo precheckInfo = PrecheckInfo.readFromFile(dumpFilePath);
        setClassPathsToClassLocations(precheckInfo.getVisitedLocs(), projectConfig.getProjectRoot());
        Coverage coverage = new Coverage();
        coverage.formMutationRanges(precheckInfo.getVisitedLocs(), testCase);
        PrecheckExecutionResult executionResult = new PrecheckExecutionResult(executionResultStr, precheckInfo);
        executionResult.setCoverage(coverage);
        return executionResult;
    }


    public InstrumentationCommandBuilder setUpForInstrumentation(TestCase testCase, boolean isPrecheck) {
        MicrobatConfig updatedMicrobatConfig = microbatConfig.setPrecheck(isPrecheck);
        String dumpFilePath = updatedMicrobatConfig.getDumpFilePath();
        File microbatDumpFile = new File(dumpFilePath);
        if (!microbatDumpFile.exists()) {
            try {
                File parentDir = microbatDumpFile.getParentFile();
                if (parentDir != null && !parentDir.exists()) {
                    parentDir.mkdirs();
                }
                boolean dumpFileCreated = microbatDumpFile.createNewFile();
                if (dumpFileCreated) {
                    System.out.println("New dump file created at " + dumpFilePath);
                }
            } catch (IOException e) {
                throw new RuntimeException("Could not create dump file at " + dumpFilePath);
            }
        }

        InstrumentationCommandBuilder ib = new InstrumentationCommandBuilder(updatedMicrobatConfig, projectConfig.getDropInsDir());
        List<File> externalJars = findJars();
        Set<String> aggregatedJarClassPaths = aggregateFileLocations(externalJars);

        for (String aggregatedJarClassPath : aggregatedJarClassPaths) {
            ib.addClassPath(aggregatedJarClassPath);
        }

        // include microbat details to instrument run
        ib.setTestCase(testCase.testClass, testCase.simpleName); // set class and method name
        ib.addClassPath(projectConfig.getCompiledTestFolder()); // add target/test-classes
        ib.addClassPath(projectConfig.getCompiledClassFolder()); // add target/classes
        ib.setWorkingDirectory(projectConfig.getProjectRoot());


        return ib;
    }

    private void deleteDumpFile() {
        File dumpFile = new File(microbatConfig.getDumpFilePath());
        if (dumpFile.exists()) {
            dumpFile.delete();
        }
        File excludesInfoFile = getExcludesInfoFile(microbatConfig.getDumpFilePath());
        if (excludesInfoFile.exists()) {
            excludesInfoFile.delete();
        }
    }

    private File getExcludesInfoFile(String traceFile) {
        String extension = FileUtils.getExtension(traceFile);
        String filePrefix = traceFile.substring(0, traceFile.length() - extension.length() - 1);
        return new File(filePrefix + "_excludes.info");
    }

    public Set<String> aggregateFileLocations(List<File> files) {
        Set<String> setOfParentDirs = new HashSet<>();
        for (File file : files) {
            try {
                String parentPath = String.join(File.separator, file.getParentFile().getCanonicalPath(), "*");
                if (setOfParentDirs.contains(parentPath)) {
                    continue;
                }
                setOfParentDirs.add(parentPath);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return setOfParentDirs;
    }
}
