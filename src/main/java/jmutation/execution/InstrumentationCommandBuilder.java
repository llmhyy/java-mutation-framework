package jmutation.execution;

import jmutation.constants.ExternalLibrary;
import jmutation.model.MicrobatConfig;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class InstrumentationCommandBuilder {
    private static final List<String> SYSTEM_JARS = List.of("junit", "org.hamcrest.core", "testrunner", "bcel-6.0",
            "javassist", "instrumentator", "junit-platform-console-standalone-1.9.0",
            "org.junit.platform.runner_1.8.1.v20211018-1956", "org.testng_7.4.0.r202105021533");

    private final String dropInsDir;
    private final MicrobatConfig microbatConfig;
    List<String> classPaths = new ArrayList<>();
    private String workingDirectory = "";
    private List<String> externalLibPaths = new ArrayList<>();
    private Optional<String> testClass = Optional.empty();
    private Optional<String> testMethod = Optional.empty();


    public InstrumentationCommandBuilder(MicrobatConfig microbatConfig, String dropInsDir) {
        this.microbatConfig = microbatConfig;
        this.dropInsDir = dropInsDir;
    }

    public void setTestCase(String testClass, String testMethod) {
        this.testClass = Optional.of(testClass);
        this.testMethod = Optional.of(testMethod);
    }

    public String generateCommand() {
        // Project paths are added to classPaths by project executor.
        generateSystemJars(dropInsDir);
        MicrobatConfig updatedMicrobatConfig = microbatConfig.setClassPaths(classPaths);
        if (testClass.isEmpty()) {
            microbatConfig.setLaunchClass("");
        } else {
            updatedMicrobatConfig = updatedMicrobatConfig.setLaunchClass(testClass.get());
        }
        if (!workingDirectory.isEmpty()) {
            updatedMicrobatConfig = updatedMicrobatConfig.setWorkingDir(workingDirectory);
        }
        StringBuilder commandStrBuilder = new StringBuilder();
        commandStrBuilder.append("\"" + updatedMicrobatConfig.getJavaHome() + "\"" + File.separator + "bin" + File.separator + "java");
        File instrumentatorFile = new File(dropInsDir + "/instrumentator.jar");
        String instrumentatorFilePath = instrumentatorFile.getAbsolutePath();
        commandStrBuilder.append(" -Xmx10G -XX:+UseG1GC -ea -noverify -javaagent:" + instrumentatorFilePath + "=");
        commandStrBuilder.append(updatedMicrobatConfig);
        commandStrBuilder.append(" -cp " + updatedMicrobatConfig.getClassPathStr());
        if (testClass.isEmpty() && testMethod.isEmpty()) {
            return commandStrBuilder.toString();
        }
        commandStrBuilder.append(" microbat.evaluation.junit.MicroBatTestRunner " + testClass.get() + " " + testMethod.get());
        return commandStrBuilder.toString();
    }

    private void generateSystemJars(String dropInsDir) {
        for (ExternalLibrary extLib : ExternalLibrary.values()) {
            File extLibFile = new File(dropInsDir, extLib.getName() + ".jar");
            assert extLibFile.exists();
            addClassPath(extLibFile);
        }
    }

    public void addClassPath(File classPath) {
        this.classPaths.add(FilenameUtils.normalize(classPath.getAbsolutePath()));
    }

    public void addExternalLibPath(File file) {
        this.externalLibPaths.add(FilenameUtils.normalize(file.getAbsolutePath()));
    }

    public void setWorkingDirectory(File workingDirectory) {
        this.workingDirectory = FilenameUtils.normalize(workingDirectory.getAbsolutePath());
    }

    public String getDumpFilePath() {
        return microbatConfig.getDumpFilePath();
    }

    public boolean isPrecheck() {
        return microbatConfig.getPrecheck();
    }
}
