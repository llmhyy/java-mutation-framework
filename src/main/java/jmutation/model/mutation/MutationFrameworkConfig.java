package jmutation.model.mutation;

import jmutation.model.MicrobatConfig;
import jmutation.model.TestCase;
import jmutation.mutation.Mutator;
import jmutation.mutation.heuristic.HeuristicMutator;

import java.io.File;

import static jmutation.constants.ResourcesPath.DEFAULT_DROP_INS_DIR;
import static jmutation.constants.ResourcesPath.DEFAULT_RESOURCES_PATH;

public class MutationFrameworkConfig {
    private Mutator mutator = new HeuristicMutator();
    private String projectPath = ""; // For creating project config and microbat config
    private String dropInsPath = String.join(File.separator, DEFAULT_RESOURCES_PATH, DEFAULT_DROP_INS_DIR);
    private String microbatConfigPath;
    private TestCase testCase = null; // Needs to be set by manager
    private MicrobatConfig microbatConfig = MicrobatConfig.defaultConfig();
    private long startSeed = 1;
    private long endSeed = 1;
    private boolean isAutoSeed = false;
    private String mutatedProjectPath = "";
    private boolean toDeleteTraceFile = true;
    private boolean toDeletePrecheckFile = true;
    private DumpFilePathConfig dumpFilePathConfig = new DumpFilePathConfig();


    public MutationFrameworkConfig() {
        String microbatConfigPath = String.join(File.separator, DEFAULT_RESOURCES_PATH,
                "microbatConfig.json");
        if (new File(microbatConfigPath).exists()) {
            microbatConfig = MicrobatConfig.parse(microbatConfigPath);
            this.microbatConfigPath = microbatConfigPath;
        }
    }

    public DumpFilePathConfig getDumpFilePathConfig() {
        return dumpFilePathConfig;
    }

    public void setDumpFilePathConfig(DumpFilePathConfig dumpFilePathConfig) {
        this.dumpFilePathConfig = dumpFilePathConfig;
    }

    public boolean isToDeleteTraceFile() {
        return toDeleteTraceFile;
    }

    public void setToDeleteTraceFile(boolean toDeleteTraceFile) {
        this.toDeleteTraceFile = toDeleteTraceFile;
    }

    public boolean isToDeletePrecheckFile() {
        return toDeletePrecheckFile;
    }

    public void setToDeletePrecheckFile(boolean toDeletePrecheckFile) {
        this.toDeletePrecheckFile = toDeletePrecheckFile;
    }

    public boolean isReady() {
        return !projectPath.isEmpty() && testCase != null;
    }

    public Mutator getMutator() {
        return mutator;
    }

    public void setMutator(Mutator mutator) {
        this.mutator = mutator;
    }

    public String getProjectPath() {
        return projectPath;
    }

    public void setProjectPath(String projectPath) {
        this.projectPath = projectPath;
        microbatConfig = microbatConfig.setWorkingDir(projectPath);
    }

    public String getDropInsPath() {
        return dropInsPath;
    }

    public void setDropInsPath(String dropInsPath) {
        this.dropInsPath = dropInsPath;
    }

    public String getMicrobatConfigPath() {
        return microbatConfigPath;
    }

    public void setMicrobatConfigPath(String microbatConfigPath) {
        this.microbatConfigPath = microbatConfigPath;
        microbatConfig = MicrobatConfig.parse(microbatConfigPath);
        microbatConfig.setWorkingDir(projectPath);
    }

    public TestCase getTestCase() {
        return testCase;
    }

    public void setTestCase(TestCase testCase) {
        this.testCase = testCase;
    }

    public MicrobatConfig getMicrobatConfig() {
        return microbatConfig;
    }

    public void setMicrobatConfig(MicrobatConfig microbatConfig) {
        this.microbatConfig = microbatConfig;
    }

    public long getStartSeed() {
        return startSeed;
    }

    public void setStartSeed(long startSeed) {
        this.startSeed = startSeed;
    }

    public long getEndSeed() {
        return endSeed;
    }

    public void setEndSeed(long endSeed) {
        this.endSeed = endSeed;
    }

    public boolean isAutoSeed() {
        return isAutoSeed;
    }

    public void setAutoSeed(boolean autoSeed) {
        isAutoSeed = autoSeed;
    }

    public String getMutatedProjectPath() {
        return mutatedProjectPath;
    }

    public void setMutatedProjectPath(String mutatedProjectPath) {
        this.mutatedProjectPath = mutatedProjectPath;
    }
}
