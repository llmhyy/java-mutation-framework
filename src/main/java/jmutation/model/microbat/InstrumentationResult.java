package jmutation.model.microbat;

import microbat.model.trace.Trace;

import java.util.List;

public class InstrumentationResult {
    private List<Trace> traces;
    private String programMsg;

    public String getProgramMsg() {
        return programMsg;
    }

    public void setProgramMsg(String programMsg) {
        this.programMsg = programMsg;
    }


    public List<Trace> getTraces() {
        return traces;
    }

    public void setTraces(List<Trace> traces) {
        this.traces = traces;
    }

    public Trace getMainTrace() {
        for (Trace trace : traces) {
            if (trace.isMain()) {
                return trace;
            }
        }
        return null;
    }
}
