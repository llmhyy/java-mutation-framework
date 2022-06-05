package jmutation.execution;

import jmutation.model.MicrobatConfig;
import jmutation.model.TestCase;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class InstrumentationCommandBuilder {
    private static final List<String> SYSTEM_JARS = List.of("junit", "org.hamcrest.core", "testrunner", "bcel-6.0", "javassist", "instrumentator");

    private static final String AGENT_OPTION_SEPARATOR = "="; // handle in MicrobatConfig?
    private static final String AGENT_PARAMS_SEPARATOR = ",";
    private static final String AGENT_PARAMS_MULTI_VALUE_SEPARATOR = File.pathSeparator;

    private final String dropInsDir;
    private final MicrobatConfig microbatConfig;
    private List<String> classPaths = new ArrayList<>();
    private List<String> externalLibPaths = new ArrayList<>();
    private Optional<String> testClass = Optional.empty();
    private Optional<String> testMethod = Optional.empty();


    public InstrumentationCommandBuilder(MicrobatConfig microbatConfig, String dropInsDir) {
        this.microbatConfig = microbatConfig;
        this.dropInsDir = dropInsDir;
    }

    public void setTestCase(TestCase testCase) {
        this.testClass = Optional.of(testCase.testClass);
        this.testMethod = Optional.of(testCase.simpleName);
    }

    private MicrobatConfig setupMicrobatConfig() {
        // TODO: add all classpath and lib collections to the immutable MicrobatConfig Class.
        return null;
    }

    public String generateCommand() {
        MicrobatConfig agentParams = setupMicrobatConfig();
        agentParams = agentParams.setClassPaths(this.classPaths);
        // TODO: Generate all the parameters to pass to microbat (dumpFile path, recorder type)
        // TODO: retrieving the trace from file
            // TODO: Copy the Trace, TraceNode classes (Breakpoint, Location)
            // TODO: Copy the FileReader
        return agentParams.toString();
    }

    private void generateSystemJars(String dropInsDir) {
        assert SYSTEM_JARS.stream().allMatch(jar -> (new File(dropInsDir, jar + ".jar")).exists());
        SYSTEM_JARS.stream().forEach(jar -> addClassPath(new File(dropInsDir, jar + ".jar")));
    }

    public void addClassPath(File classPath) {
        this.classPaths.add(classPath.getAbsolutePath());
    }

    public void addExternalLibPath(File file) {
        this.externalLibPaths.add(file.getAbsolutePath());
    }
}
