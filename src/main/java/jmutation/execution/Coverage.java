package jmutation.execution;

import jmutation.model.MutationRange;
import jmutation.model.TestCase;
import jmutation.utils.RandomSingleton;
import microbat.model.BreakPoint;
import microbat.model.ClassLocation;
import microbat.model.trace.Trace;
import microbat.model.trace.TraceNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

public class Coverage {
    private List<MutationRange> ranges;

    public void formMutationRanges(Set<ClassLocation> classLocationSet, TestCase testCase) {
        Iterator<ClassLocation> classLocationIterator = classLocationSet.iterator();
        Map<String, PriorityQueue<Integer>> classAndLineNumbersMap = new HashMap<>();
        String testCaseClassName = testCase.testClass;
        while (classLocationIterator.hasNext()) {
            ClassLocation classLocation = classLocationIterator.next();
            String canonicalClassName = classLocation.getClassCanonicalName();
            // Do not create mutation range if class name is the test class i.e. don't mutate test class
            if (canonicalClassName.contains(testCaseClassName)) {
                continue;
            }
            int lineNumber = classLocation.getLineNumber();
            addClassNameAndLineNumToMap(classAndLineNumbersMap, canonicalClassName, lineNumber);
        }
        ranges = formMutationRangesFromClassNameLineNumMap(classAndLineNumbersMap);
    }

    public void formMutationRanges(Trace trace, TestCase testCase) {
        List<TraceNode> traceNodes = trace.getExecutionList();
        Map<String, PriorityQueue<Integer>> classAndLineNumbersMap = new HashMap<>();
        String testCaseClassName = testCase.testClass;
        for (TraceNode traceNode : traceNodes) {
            BreakPoint breakPoint = traceNode.getBreakPoint();
            String canonicalClassName = breakPoint.getDeclaringCompilationUnitName();
            // Do not create mutation range if class name is the test class i.e. don't mutate test class
            if (canonicalClassName.equals(testCaseClassName)) {
                continue;
            }
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

    public Map<String, List<MutationRange>> getRangesByClass() {
        Map<String, List<MutationRange>> classToRange = new LinkedHashMap<>();
        for (MutationRange range : ranges) {
            String className = range.getClassName();
            List<MutationRange> rangesForClass;
            if (classToRange.containsKey(className)) {
                rangesForClass = classToRange.get(className);
            } else {
                rangesForClass = new ArrayList<>();
            }
            rangesForClass.add(range);
            classToRange.put(className, rangesForClass);
        }
        return classToRange;
    }

    public void shuffleRanges() {
        ranges = RandomSingleton.getSingleton().shuffle(ranges);
    }
}
