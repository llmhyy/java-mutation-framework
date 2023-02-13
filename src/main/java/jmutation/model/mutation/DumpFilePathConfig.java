package jmutation.model.mutation;

import java.io.File;
import java.util.Objects;

public class DumpFilePathConfig {
    public static final String DEFAULT_DUMP_FILE_DIR = System.getProperty("java.io.tmpdir") + "trace-files" + File.separator;
    public static final String DEFAULT_TRACE_FILE = "trace.exec";
    public static final String DEFAULT_BUGGY_TRACE_FILE = "buggy-trace.exec";
    public static final String DEFAULT_TRACE_W_ASSERTS_FILE = "trace-asserts.exec";
    public static final String DEFAULT_BUGGY_TRACE_W_ASSERTS_FILE = "buggy-trace-asserts.exec";
    public static final String DEFAULT_PRECHECK_FILE = "precheck.exec";
    public static final String DEFAULT_BUGGY_PRECHECK_FILE = "buggy-precheck.exec";
    private String traceFilePath;
    private String mutatedTraceFilePath;
    private String traceWithAssertsFilePath;
    private String mutatedTraceWithAssertsFilePath;
    private String precheckFilePath;
    private String mutatedPrecheckFilePath;

    public DumpFilePathConfig() {
        traceFilePath = DEFAULT_DUMP_FILE_DIR + DEFAULT_TRACE_FILE;
        mutatedPrecheckFilePath = DEFAULT_DUMP_FILE_DIR + DEFAULT_BUGGY_PRECHECK_FILE;
        mutatedTraceFilePath = DEFAULT_DUMP_FILE_DIR + DEFAULT_BUGGY_TRACE_FILE;
        precheckFilePath = DEFAULT_DUMP_FILE_DIR + DEFAULT_PRECHECK_FILE;
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

    public void setTraceFilePath(String traceFilePath) {
        this.traceFilePath = traceFilePath;
    }

    public String getMutatedTraceFilePath() {
        return mutatedTraceFilePath;
    }

    public void setMutatedTraceFilePath(String mutatedTraceFilePath) {
        this.mutatedTraceFilePath = mutatedTraceFilePath;
    }

    public String getPrecheckFilePath() {
        return precheckFilePath;
    }

    public void setPrecheckFilePath(String precheckFilePath) {
        this.precheckFilePath = precheckFilePath;
    }

    public String getMutatedPrecheckFilePath() {
        return mutatedPrecheckFilePath;
    }

    public void setMutatedPrecheckFilePath(String mutatedPrecheckFilePath) {
        this.mutatedPrecheckFilePath = mutatedPrecheckFilePath;
    }

    public String getTraceWithAssertsFilePath() {
        return traceWithAssertsFilePath;
    }

    public void setTraceWithAssertsFilePath(String traceWithAssertsFilePath) {
        this.traceWithAssertsFilePath = traceWithAssertsFilePath;
    }

    public String getMutatedTraceWithAssertsFilePath() {
        return mutatedTraceWithAssertsFilePath;
    }

    public void setMutatedTraceWithAssertsFilePath(String mutatedTraceWithAssertsFilePath) {
        this.mutatedTraceWithAssertsFilePath = mutatedTraceWithAssertsFilePath;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DumpFilePathConfig that = (DumpFilePathConfig) o;
        return Objects.equals(traceFilePath, that.traceFilePath) &&
                Objects.equals(mutatedTraceFilePath, that.mutatedTraceFilePath) &&
                Objects.equals(traceWithAssertsFilePath, that.traceWithAssertsFilePath) &&
                Objects.equals(mutatedTraceWithAssertsFilePath, that.mutatedTraceWithAssertsFilePath) &&
                Objects.equals(precheckFilePath, that.precheckFilePath) &&
                Objects.equals(mutatedPrecheckFilePath, that.mutatedPrecheckFilePath);
    }

    @Override
    public int hashCode() {
        return Objects.hash(traceFilePath, mutatedTraceFilePath, traceWithAssertsFilePath,
                mutatedTraceWithAssertsFilePath, precheckFilePath, mutatedPrecheckFilePath);
    }
}
