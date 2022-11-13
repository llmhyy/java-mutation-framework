package jmutation.model.mutation;

import java.io.File;

public class DumpFilePathConfig {
    public static final String DEFAULT_DUMP_FILE_PATH = System.getProperty("java.io.tmpdir") + File.separator + "dumpFile.exec";
    private final String traceFilePath;
    private final String mutatedTraceFilePath;
    private final String precheckFilePath;
    private final String mutatedPrecheckFilePath;

    public DumpFilePathConfig() {
        traceFilePath = DEFAULT_DUMP_FILE_PATH;
        mutatedPrecheckFilePath = DEFAULT_DUMP_FILE_PATH;
        mutatedTraceFilePath = DEFAULT_DUMP_FILE_PATH;
        precheckFilePath = DEFAULT_DUMP_FILE_PATH;
    }

    public DumpFilePathConfig(String traceFilePath, String mutatedTraceFilePath, String precheckFilePath,
                              String mutatedPrecheckFilePath) {
        this.traceFilePath = traceFilePath;
        this.mutatedTraceFilePath = mutatedTraceFilePath;
        this.precheckFilePath = precheckFilePath;
        this.mutatedPrecheckFilePath = mutatedPrecheckFilePath;
    }

    public String getTraceFilePath() {
        return traceFilePath;
    }

    public String getMutatedTraceFilePath() {
        return mutatedTraceFilePath;
    }

    public String getPrecheckFilePath() {
        return precheckFilePath;
    }

    public String getMutatedPrecheckFilePath() {
        return mutatedPrecheckFilePath;
    }
}
