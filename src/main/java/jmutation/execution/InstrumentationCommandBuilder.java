package jmutation.execution;

import jmutation.model.MicrobatConfig;
import jmutation.model.TestCase;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class InstrumentationCommandBuilder {
    private static final List<String> SYSTEM_JARS = List.of("junit", "org.hamcrest.core", "testrunner", "bcel-6.0", "javassist", "instrumentator");

    private final String dropInsDir;
    private final MicrobatConfig microbatConfig;
    List<String> classPaths = new ArrayList<>();
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

    /**
     * Sets up microbat config from command line instead of json file.
     * @return Microbat configuration object
     */
    private MicrobatConfig setupMicrobatConfig() {
        // TODO: add all classpath and lib collections to the immutable MicrobatConfig Class.
        String tmpDir = System.getProperty("java.io.tmpdir");
        String dumpFilePath = tmpDir + File.pathSeparator + "trace.exec";
        MicrobatConfig updatedMicrobatConfig = microbatConfig.setDumpFilePath(dumpFilePath);
        updatedMicrobatConfig = updatedMicrobatConfig.setTraceRecorder("FILE");
        updatedMicrobatConfig = updatedMicrobatConfig.setClassPaths(this.classPaths);
        return updatedMicrobatConfig;
    }

    public String generateCommand() {
        // Project paths are added to classPaths by project executor.
        generateSystemJars(dropInsDir);
        MicrobatConfig updatedMicrobatConfig = microbatConfig.setClassPaths(classPaths);
        StringBuilder commandStrBuilder = new StringBuilder();
        commandStrBuilder.append(updatedMicrobatConfig.getJavaHome() + File.separator + "bin" + File.separator + "java");
        File instrumentatorFile = new File("lib/instrumentator.jar");
        String instrumentatorFilePath = instrumentatorFile.getAbsolutePath();
        commandStrBuilder.append(" -Xmx10G -XX:+UseG1GC -ea -noverify -javaagent:" + instrumentatorFilePath + "=");
        commandStrBuilder.append(updatedMicrobatConfig);
        commandStrBuilder.append(" -cp " + updatedMicrobatConfig.getClassPathStr());
        if (!testClass.isEmpty() && !testMethod.isEmpty()) {
            commandStrBuilder.append(" microbat.evaluation.junit.MicroBatTestRunner " + testClass.get() + " " + testMethod.get());
        }
        return commandStrBuilder.toString();
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

    public String getTraceFilePath() {
        return microbatConfig.getDumpFilePath();
    }
}
