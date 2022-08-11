package jmutation.model.microbat;

import microbat.model.trace.Trace;

import java.util.List;

public class InstrumentationResult {
    private List<Trace> traces;

    public List<Trace> getTraces() {
        return traces;
    }

    public void setTraces(List<Trace> traces) {
        this.traces = traces;
    }
}
