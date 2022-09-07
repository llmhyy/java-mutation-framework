package jmutation.model;

import microbat.model.trace.Trace;
import tracecollection.model.InstrumentationResult;

public class TraceCollectionResult extends ExecutionResult {
    private InstrumentationResult instrumentationResult;

    public TraceCollectionResult(String consoleOut, InstrumentationResult instrumentationResult) {
        super(consoleOut);
        this.instrumentationResult = instrumentationResult;
    }

    public InstrumentationResult getInstrumentationResult() {
        return instrumentationResult;
    }

    public void setInstrumentationResult(InstrumentationResult instrumentationResult) {
        this.instrumentationResult = instrumentationResult;
    }

    public Trace getTrace() {
        return instrumentationResult.getMainTrace();
    }
}
