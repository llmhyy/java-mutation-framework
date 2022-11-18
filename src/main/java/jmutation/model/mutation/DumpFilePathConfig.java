package jmutation.model.mutation;

import java.io.File;

public class DumpFilePathConfig {
    public static final String DEFAULT_DUMP_FILE_DIR = System.getProperty("java.io.tmpdir") + "trace-files" + File.separator;
    public static final String DEFAULT_TRACE_FILE = "trace.exec";
    public static final String DEFAULT_BUGGY_TRACE_FILE = "buggy-trace.exec";
    public static final String DEFAULT_TRACE_W_ASSERTS_FILE = "trace-asserts.exec";
    public static final String DEFAULT_BUGGY_TRACE_W_ASSERTS_FILE = "buggy-trace-asserts.exec";
    public static final String DEFAULT_PRECHECK_FILE = "precheck.exec";
    public static final String DEFAULT_BUGGY_PRECHECK_FILE = "buggy-precheck.exec";
    private final String traceFilePath;
    private final String mutatedTraceFilePath;
    private final String traceWithAssertsFilePath;
    private final String mutatedTraceWithAssertsFilePath;
    private final String precheckFilePath;
    private final String mutatedPrecheckFilePath;

    public DumpFilePathConfig() {
        traceFilePath = DEFAULT_DUMP_FILE_DIR + DEFAULT_TRACE_FILE;
        mutatedPrecheckFilePath = DEFAULT_DUMP_FILE_DIR + DEFAULT_PRECHECK_FILE;
        mutatedTraceFilePath = DEFAULT_DUMP_FILE_DIR + DEFAULT_BUGGY_TRACE_FILE;
        precheckFilePath = DEFAULT_DUMP_FILE_DIR + DEFAULT_BUGGY_PRECHECK_FILE;
        traceWithAssertsFilePath = DEFAULT_DUMP_FILE_DIR + DEFAULT_TRACE_W_ASSERTS_FILE;
        mutatedTraceWithAssertsFilePath = DEFAULT_DUMP_FILE_DIR + DEFAULT_BUGGY_TRACE_W_ASSERTS_FILE;
    }

    public DumpFilePathConfig(String traceFilePath, String mutatedTraceFilePath, String traceWithAssertsFilePath, String mutatedTraceWithAssertsFilePath, String precheckFilePath, String mutatedPrecheckFilePath) {
        this.traceFilePath = traceFilePath;
        this.mutatedTraceFilePath = mutatedTraceFilePath;
        this.traceWithAssertsFilePath = traceWithAssertsFilePath;
        this.mutatedTraceWithAssertsFilePath = mutatedTraceWithAssertsFilePath;
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

    public String getTraceWithAssertsFilePath() {
        return traceWithAssertsFilePath;
    }

    public String getMutatedTraceWithAssertsFilePath() {
        return mutatedTraceWithAssertsFilePath;
    }
}
