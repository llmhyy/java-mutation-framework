package jmutation.model.mutation;

import jmutation.model.MicrobatConfig;
import jmutation.model.TestCase;
import jmutation.mutation.Mutator;
import jmutation.mutation.heuristic.HeuristicMutator;

import java.io.File;

import static jmutation.constants.ResourcesPath.DEFAULT_DROP_INS_DIR;
import static jmutation.constants.ResourcesPath.DEFAULT_RESOURCES_PATH;

/**
 * Configuration object for MutationFramework.
 * Use the builder to create the object.
 * All fields are immutable, except test case.
 * This enables the mutation framework to loop through test cases, and mutate them one at a time.
 */
public class MutationFrameworkConfig {
    private final Mutator mutator;
    private final String projectPath;
    private final String dropInsPath;
    private final MicrobatConfig microbatConfig;
    private final long startSeed;
    private final long endSeed;
    private final boolean isAutoSeed;
    private final String mutatedProjectPath;
    private final boolean toDeleteTraceFile;
    private final boolean toDeletePrecheckFile;
    private final DumpFilePathConfig dumpFilePathConfig;
    private final int instrumentationTimeout;
    private TestCase testCase; // Needs to be set by manager

    public MutationFrameworkConfig(Mutator mutator, String projectPath, String dropInsPath,
                                   MicrobatConfig microbatConfig, long startSeed, long endSeed,
                                   boolean isAutoSeed, String mutatedProjectPath, boolean toDeleteTraceFile,
                                   boolean toDeletePrecheckFile, DumpFilePathConfig dumpFilePathConfig,
                                   int instrumentationTimeout) {
        this.mutator = mutator;
        this.projectPath = projectPath;
        this.dropInsPath = dropInsPath;
        this.microbatConfig = microbatConfig;
        this.startSeed = startSeed;
        this.endSeed = endSeed;
        this.isAutoSeed = isAutoSeed;
        this.mutatedProjectPath = mutatedProjectPath;
        this.toDeleteTraceFile = toDeleteTraceFile;
        this.toDeletePrecheckFile = toDeletePrecheckFile;
        this.dumpFilePathConfig = dumpFilePathConfig;
        this.instrumentationTimeout = instrumentationTimeout;
    }

    public Mutator getMutator() {
        return mutator;
    }

    public String getProjectPath() {
        return projectPath;
    }

    public String getDropInsPath() {
        return dropInsPath;
    }

    public MicrobatConfig getMicrobatConfig() {
        return microbatConfig;
    }

    public long getStartSeed() {
        return startSeed;
    }

    public long getEndSeed() {
        return endSeed;
    }

    public boolean isAutoSeed() {
        return isAutoSeed;
    }

    public String getMutatedProjectPath() {
        return mutatedProjectPath;
    }

    public boolean isToDeleteTraceFile() {
        return toDeleteTraceFile;
    }

    public boolean isToDeletePrecheckFile() {
        return toDeletePrecheckFile;
    }

    public DumpFilePathConfig getDumpFilePathConfig() {
        return dumpFilePathConfig;
    }

    public int getInstrumentationTimeout() {
        return instrumentationTimeout;
    }

    public TestCase getTestCase() {
        return testCase;
    }

    public void setTestCase(TestCase testCase) {
        this.testCase = testCase;
    }

    /**
     * Most fields have default values, except projectPath which must be set explicitly
     */
    public static class MutationFrameworkConfigBuilder {
        private Mutator mutator = new HeuristicMutator();
        private String projectPath = ""; // For creating project config and microbat config
        private String dropInsPath = String.join(File.separator, DEFAULT_RESOURCES_PATH, DEFAULT_DROP_INS_DIR);
        private TestCase testCase = null; // Needs to be set by manager
        private MicrobatConfig microbatConfig = MicrobatConfig.defaultConfig();
        private long startSeed = 1;
        private long endSeed = 1;
        private boolean isAutoSeed = false;
        private String mutatedProjectPath = System.getProperty("java.io.tmpdir") + File.separator + "mutation";
        private boolean toDeleteTraceFile = true;
        private boolean toDeletePrecheckFile = true;
        private DumpFilePathConfig dumpFilePathConfig = new DumpFilePathConfig();
        private int instrumentationTimeout = 0;


        public MutationFrameworkConfigBuilder() {
            String microbatConfigPath = String.join(File.separator, DEFAULT_RESOURCES_PATH,
                    "microbatConfig.json");
            if (new File(microbatConfigPath).exists()) {
                microbatConfig = MicrobatConfig.parse(microbatConfigPath);
            }
        }

        public void setDumpFilePathConfig(DumpFilePathConfig dumpFilePathConfig) {
            this.dumpFilePathConfig = dumpFilePathConfig;
        }


        public void setToDeleteTraceFile(boolean toDeleteTraceFile) {
            this.toDeleteTraceFile = toDeleteTraceFile;
        }

        public void setToDeletePrecheckFile(boolean toDeletePrecheckFile) {
            this.toDeletePrecheckFile = toDeletePrecheckFile;
        }


        public void setMutator(Mutator mutator) {
            this.mutator = mutator;
        }


        public void setProjectPath(String projectPath) {
            this.projectPath = projectPath;
            microbatConfig = microbatConfig.setWorkingDir(projectPath);
        }

        public void setDropInsPath(String dropInsPath) {
            this.dropInsPath = dropInsPath;
        }

        public void setMicrobatConfigPath(String microbatConfigPath) {
            microbatConfig = MicrobatConfig.parse(microbatConfigPath);
            microbatConfig.setWorkingDir(projectPath);
        }

        public void setTestCase(TestCase testCase) {
            this.testCase = testCase;
        }

        public void setMicrobatConfig(MicrobatConfig microbatConfig) {
            this.microbatConfig = microbatConfig;
        }

        public void setStartSeed(long startSeed) {
            this.startSeed = startSeed;
        }


        public void setEndSeed(long endSeed) {
            this.endSeed = endSeed;
        }

        public void setAutoSeed(boolean autoSeed) {
            isAutoSeed = autoSeed;
        }

        public void setMutatedProjectPath(String mutatedProjectPath) {
            this.mutatedProjectPath = mutatedProjectPath;
        }

        public void setInstrumentationTimeout(int instrumentationTimeout) {
            this.instrumentationTimeout = instrumentationTimeout;
        }

        public MutationFrameworkConfig build() {
            if (isReady()) {
                return new MutationFrameworkConfig(mutator, projectPath, dropInsPath,
                        microbatConfig, startSeed, endSeed,
                        isAutoSeed, mutatedProjectPath, toDeleteTraceFile,
                        toDeletePrecheckFile, dumpFilePathConfig,
                        instrumentationTimeout);
            }
            throw new IllegalStateException("Configuration Builder is not in valid state");
        }

        private boolean isReady() {
            return !projectPath.isEmpty() &&
                    !mutatedProjectPath.isEmpty() &&
                    microbatConfig != null &&
                    !dropInsPath.isEmpty() &&
                    mutator != null &&
                    dumpFilePathConfig != null;
        }
    }
}
