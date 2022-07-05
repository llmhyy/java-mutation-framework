package jmutation.execution;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import jmutation.mutation.MutationRange;
import microbat.model.ClassLocation;

public class Coverage {
    private Set<ClassLocation> classLocationSet;
    
    private List<MutationRange> ranges;

    public Coverage(Set<ClassLocation> classLocationSet) {
        this.classLocationSet = classLocationSet;
		Iterator<ClassLocation> classLocationIterator = classLocationSet.iterator();
        ranges = new ArrayList<>();
        /**
         * TODO
         * a very simple implementation to be enhanced
         */
		while(classLocationIterator.hasNext()) {
        	ClassLocation classLocation = classLocationIterator.next();
        	MutationRange range = new MutationRange(classLocation.getClassCanonicalName(), classLocation.getLineNumber(), classLocation.getLineNumber()+1);
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
