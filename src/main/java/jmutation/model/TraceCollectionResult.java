package jmutation.model;

import microbat.instrumentation.output.RunningInfo;
import microbat.model.trace.Trace;

public class TraceCollectionResult extends ExecutionResult {
    private RunningInfo instrumentationResult;

    public TraceCollectionResult(String consoleOut, RunningInfo instrumentationResult) {
        super(consoleOut);
        this.instrumentationResult = instrumentationResult;
    }

    public RunningInfo getInstrumentationResult() {
        return instrumentationResult;
    }

    public void setInstrumentationResult(RunningInfo instrumentationResult) {
        this.instrumentationResult = instrumentationResult;
    }

    public Trace getTrace() {
        return instrumentationResult.getMainTrace();
    }
}
