package jmutation.execution;

import java.util.*;

import jmutation.mutation.MutationRange;
import microbat.model.ClassLocation;

public class Coverage {
    private Set<ClassLocation> classLocationSet;
    
    private List<MutationRange> ranges;

    public Coverage(Set<ClassLocation> classLocationSet) {
        this.classLocationSet = classLocationSet;
		Iterator<ClassLocation> classLocationIterator = classLocationSet.iterator();
        ranges = new ArrayList<>();
		Map<String, PriorityQueue<Integer>> classAndLineNumbersMap = new HashMap<>();
		while(classLocationIterator.hasNext()) {
        	ClassLocation classLocation = classLocationIterator.next();
			String canonicalClassName = classLocation.getClassCanonicalName();
			int lineNumber = classLocation.getLineNumber();
			if (!classAndLineNumbersMap.containsKey(canonicalClassName)) {
				PriorityQueue<Integer> lineNumberQueue = new PriorityQueue<>();
				lineNumberQueue.add(lineNumber);
				classAndLineNumbersMap.put(canonicalClassName, lineNumberQueue);
			} else {
				PriorityQueue<Integer> existingLineNumQueue = classAndLineNumbersMap.get(canonicalClassName);
				existingLineNumQueue.add(lineNumber);
			}
        }
		Iterator<Map.Entry<String, PriorityQueue<Integer>>> classAndLineNumberIterator = classAndLineNumbersMap.entrySet().iterator();
		while (classAndLineNumberIterator.hasNext()) {
			Map.Entry<String, PriorityQueue<Integer>> classLineNumbersEntry = classAndLineNumberIterator.next();
			String classCanonicalName = classLineNumbersEntry.getKey();
			PriorityQueue<Integer> lineNumQueue = classLineNumbersEntry.getValue();
			Iterator<Integer> lineNumIterator = lineNumQueue.iterator();
			int previousLineNum = -1;
			int startLineNum = -1;
			int lineNumber = -1;
			while (lineNumIterator.hasNext()) {
				lineNumber = lineNumIterator.next();
				if (previousLineNum == -1) {
					previousLineNum = lineNumber;
					startLineNum = lineNumber;
					continue;
				}
				if (Math.abs(previousLineNum - lineNumber) > 1) {
					MutationRange range = new MutationRange(classCanonicalName, startLineNum, lineNumber);
					ranges.add(range);
					previousLineNum = lineNumber;
					startLineNum = lineNumber;
					continue;
				}
				previousLineNum = lineNumber;
			}
			MutationRange range = new MutationRange(classCanonicalName, startLineNum, lineNumber);
			ranges.add(range);
		}
    }

	public List<MutationRange> getRanges() {
		return ranges;
	}

	public void setRanges(List<MutationRange> ranges) {
		this.ranges = ranges;
	}

}
