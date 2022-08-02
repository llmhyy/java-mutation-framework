package jmutation.execution;

import jmutation.mutation.MutationRange;
import microbat.model.ClassLocation;
import microbat.model.BreakPoint;
import microbat.model.trace.Trace;
import microbat.model.trace.TraceNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

public class Coverage {
    private List<MutationRange> ranges;

    private Trace trace;

    public Coverage(Set<ClassLocation> classLocationSet) {
        Iterator<ClassLocation> classLocationIterator = classLocationSet.iterator();
        Map<String, PriorityQueue<Integer>> classAndLineNumbersMap = new HashMap<>();
        while (classLocationIterator.hasNext()) {
            ClassLocation classLocation = classLocationIterator.next();
            String canonicalClassName = classLocation.getDeclaringCompilationUnitName();
            int lineNumber = classLocation.getLineNumber();
            addClassNameAndLineNumToMap(classAndLineNumbersMap, canonicalClassName, lineNumber);
        }
        ranges = formMutationRangesFromClassNameLineNumMap(classAndLineNumbersMap);
    }

    public Coverage(Trace trace) {
        this.trace = trace;
        List<TraceNode> traceNodes = trace.getExecutionList();
        Map<String, PriorityQueue<Integer>> classAndLineNumbersMap = new HashMap<>();
        for (TraceNode traceNode : traceNodes) {
            BreakPoint breakPoint = traceNode.getBreakPoint();
            String canonicalClassName = breakPoint.getDeclaringCompilationUnitName();
            int lineNumber = breakPoint.getLineNumber();
            addClassNameAndLineNumToMap(classAndLineNumbersMap, canonicalClassName, lineNumber);
        }
        ranges = formMutationRangesFromClassNameLineNumMap(classAndLineNumbersMap);
    }

    private void addClassNameAndLineNumToMap(Map<String, PriorityQueue<Integer>> classAndLineNumbersMap, String className, int lineNumber) {
        if (!classAndLineNumbersMap.containsKey(className)) {
            PriorityQueue<Integer> lineNumberQueue = new PriorityQueue<>();
            lineNumberQueue.add(lineNumber);
            classAndLineNumbersMap.put(className, lineNumberQueue);
        } else {
            PriorityQueue<Integer> existingLineNumQueue = classAndLineNumbersMap.get(className);
            existingLineNumQueue.add(lineNumber);
        }
    }

    private List<MutationRange> formMutationRangesFromClassNameLineNumMap(Map<String, PriorityQueue<Integer>> classAndLineNumbersMap) {
        List<MutationRange> mutationRanges = new ArrayList<>();
        Iterator<Map.Entry<String, PriorityQueue<Integer>>> classAndLineNumberIterator = classAndLineNumbersMap.entrySet().iterator();
        while (classAndLineNumberIterator.hasNext()) {
            Map.Entry<String, PriorityQueue<Integer>> classLineNumbersEntry = classAndLineNumberIterator.next();
            String classCanonicalName = classLineNumbersEntry.getKey();
            PriorityQueue<Integer> lineNumQueue = classLineNumbersEntry.getValue();
            int previousLineNum = -1;
            int startLineNum = -1;
            int lineNumber = -1;
            while (!lineNumQueue.isEmpty()) {
                lineNumber = lineNumQueue.remove();
                if (previousLineNum == -1) {
                    previousLineNum = lineNumber;
                    startLineNum = lineNumber;
                    continue;
                }
                if (Math.abs(previousLineNum - lineNumber) > 1) {
                    MutationRange range = new MutationRange(classCanonicalName, startLineNum, previousLineNum);
                    mutationRanges.add(range);
                    previousLineNum = lineNumber;
                    startLineNum = lineNumber;
                    continue;
                }
                previousLineNum = lineNumber;
            }
            MutationRange range = new MutationRange(classCanonicalName, startLineNum, lineNumber);
            mutationRanges.add(range);
        }
        return mutationRanges;
    }

    public List<MutationRange> getRanges() {
        return ranges;
    }

    public void setRanges(List<MutationRange> ranges) {
        this.ranges = ranges;
    }

    public Trace getTrace() {
        return trace;
    }
}
