package jmutation.execution;

import java.util.List;

import jmutation.mutation.MutationRange;
import microbat.model.trace.Trace;
import microbat.model.trace.TraceNode;

public class Coverage {
    private Trace trace;
    
    private List<MutationRange> ranges;

    public Coverage(Trace trace) {
        this.trace = trace;
        
        /**
         * TODO
         * a very simple implementation to be enhanced
         */
        for(int i=0; i<trace.getExecutionList().size(); i++) {
        	TraceNode step = trace.getTraceNode(i+1);
        	
        	MutationRange range = new MutationRange(step.getClassCanonicalName(), step.getLineNumber(), step.getLineNumber()+1);
        	ranges.add(range);
        	break;
        }
    }

	public List<MutationRange> getRanges() {
		return ranges;
	}

	public void setRanges(List<MutationRange> ranges) {
		this.ranges = ranges;
	}

}
