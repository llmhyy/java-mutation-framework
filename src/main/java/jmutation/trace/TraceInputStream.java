package jmutation.trace;

import microbat.model.BreakPoint;
import microbat.model.ClassLocation;
import microbat.model.ControlScope;
import microbat.model.SourceScope;
import microbat.model.trace.Trace;
import microbat.model.trace.TraceNode;
import microbat.model.value.VarValue;
import microbat.util.ByteConverterUtil;
import microbat.util.FileUtils;
import tracecollection.model.InstrumentationResult;
import tracecollection.model.PrecheckResult;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TraceInputStream extends DataInputStream {

    private static final String PRECHECK_HEADER = "Precheck";
    private static final String TRACE_HEADER = "TracingResult";
    private final String traceExecFolder;

    TraceInputStream(File traceFile) throws FileNotFoundException {
        super(new FileInputStream(traceFile));
        traceExecFolder = traceFile.getParent();
    }

    public PrecheckResult readPrecheck() {
        try {
            String header = readString();
            if (!PRECHECK_HEADER.equals(header)) {
                throw new RuntimeException("Invalid Precheck file result!");
            }
            PrecheckResult precheckResult = new PrecheckResult();
            precheckResult.setProgramMessage(readString());
            precheckResult.setThreadNumber(readVarInt());
            precheckResult.setOverLong(readBoolean());
            precheckResult.setTotalSteps(readVarInt());
            int exceedingMethodsSize = readVarInt();
            for (int i = 0; i < exceedingMethodsSize; i++) {
                readString();
            }
            int locationsSize = readVarInt();
            Set<ClassLocation> visitedLocs = new HashSet<>(locationsSize);
            for (int i = 0; i < locationsSize; i++) {
                String className = readString();
                String methodSignature = readString();
                int lineNumber = readInt();
                ClassLocation loc = new ClassLocation(className, methodSignature, lineNumber);
                visitedLocs.add(loc);
            }
            precheckResult.setVisitedClassLocations(visitedLocs);
            int loadedClassesSize = readVarInt();
            List<String> loadedClasses = new ArrayList<>(loadedClassesSize);
            for (int i = 0; i < loadedClassesSize; i++) {
                loadedClasses.add(readString());
            }
            return precheckResult;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public InstrumentationResult readTraceCollection() {
        try {
            String header = readString();
            String programMsg;
            int expectedSteps = 0;
            int collectedSteps = 0;
            InstrumentationResult result = new InstrumentationResult();
            if (TRACE_HEADER.equals(header)) {
                programMsg = readString();
                expectedSteps = readInt();
                collectedSteps = readInt();
            } else {
                programMsg = header; // for compatible reason with old version. TO BE REMOVED.
            }
            result.setProgramMsg(programMsg);
            int traceNo = readVarInt();
            if (traceNo == 0) {
                return null;
            }
            List<Trace> traces = new ArrayList<>();
            for (int i = 0; i < traceNo; i++) {
                Trace trace = new Trace(null);
                readString(); // projectName
                readString(); // projectVersion
                readString(); // launchClass
                readString(); // launchMethod
                boolean isMainTrace = readBoolean();
                trace.setMain(isMainTrace);
                trace.setThreadName(readString());
                trace.setThreadId(Long.parseLong(readString()));
                trace.setIncludedLibraryClasses(readFilterInfo());
                trace.setExcludedLibraryClasses(readFilterInfo());
                List<BreakPoint> locationList = readLocations();
                trace.setExecutionList(readSteps(trace, locationList));
                traces.add(trace);
            }
            result.setTraces(traces);
            return result;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private List<String> readFilterInfo() throws IOException {
        boolean inFile = readBoolean();
        if (inFile) {
            if (traceExecFolder == null) {
                throw new IllegalArgumentException("missing define traceExecFolder!");
            }
            String fileName = readString();
            String filePath = FileUtils.getFilePath(traceExecFolder, fileName);
            return FileUtils.readLines(filePath);
        } else {
            return readSerializableList();
        }
    }

    private String readString() throws IOException {
        int len = readVarInt();
        if (len == -1) {
            return null;
        } else if (len == 0) {
            return "";
        } else {
            byte[] bytes = new byte[len];
            readFully(bytes);
            return new String(bytes);
        }
    }

    private byte[] readByteArray() throws IOException {
        int len = readVarInt();
        if (len == -1) {
            return null;
        } else if (len == 0) {
            return new byte[0];
        } else {
            byte[] bytes = new byte[len];
            readFully(bytes);
            return bytes;
        }
    }

    private int readVarInt() throws IOException {
        final int value = 0xFF & readByte();
        if ((value & 0x80) == 0) {
            return value;
        }
        return (value & 0x7F) | (readVarInt() << 7);
    }

    private List<Integer> readListInt() throws IOException {
        int size = readVarInt();
        if (size == -1) {
            return null;
        }
        List<Integer> list = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            list.add(readVarInt());
        }
        return list;
    }

    private List<String> readListString() throws IOException {
        int size = readVarInt();
        if (size == -1) {
            return null;
        }
        List<String> list = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            list.add(readString());
        }
        return list;
    }

    private List<BreakPoint> readLocations() throws IOException {
        int bkpTotal = readVarInt();
        int numOfClasses = readVarInt();
        List<BreakPoint> allLocs = new ArrayList<>(bkpTotal);
        for (int i = 0; i < numOfClasses; i++) {
            int lines = readVarInt();
            if (lines <= 0) {
                continue;
            }
            String declaringCompilationUnitName = readString();
            for (int j = 0; j < lines; j++) {
                BreakPoint loc = readLocation(declaringCompilationUnitName);
                allLocs.add(loc);
            }
        }
        return allLocs;
    }

    private BreakPoint readLocation(String declaringCompilationUnitName) throws IOException {
        String classCanonicalName = readString();
        String methodSig = readString();
        int lineNo = readVarInt();
        boolean isConditional = readBoolean();
        boolean isBranch = readBoolean();
        boolean isReturnStatement = readBoolean();
        BreakPoint location = new BreakPoint(classCanonicalName, declaringCompilationUnitName, methodSig, lineNo);
        location.setConditional(isConditional);
        location.setBranch(isBranch);
        location.setReturnStatement(isReturnStatement);
        location.setControlScope(readControlScope());
        location.setLoopScope(readLoopScope());
        return location;
    }

    private ControlScope readControlScope() throws IOException {
        int rangeSize = readVarInt();
        if (rangeSize == 0) {
            return null;
        }
        ControlScope scope = new ControlScope();
        scope.setLoop(readBoolean());
        for (int i = 0; i < rangeSize; i++) {
            ClassLocation controlLoc = new ClassLocation(readString(), null, readVarInt());
            scope.addLocation(controlLoc);
        }
        return scope;
    }

    private SourceScope readLoopScope() throws IOException {
        int size = readVarInt();
        if (size == 0) {
            return null;
        }
        String className = readString();
        int startLine = readVarInt();
        int endLine = readVarInt();
        return new SourceScope(className, startLine, endLine);
    }

    private List<TraceNode> readSteps(Trace trace, List<BreakPoint> locationList) throws IOException {
        int size = readVarInt();
        List<TraceNode> allSteps = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            TraceNode node = new TraceNode(null, null, i + 1, trace, "");
            allSteps.add(node);
        }
        for (int i = 0; i < size; i++) {
            TraceNode step = allSteps.get(i);
            step.setBreakPoint(locationList.get(readVarInt()));
            step.setTimestamp(readLong());
            TraceNode controlDominator = readNode(allSteps);
            step.setControlDominator(controlDominator);
            if (controlDominator != null) {
                controlDominator.addControlDominatee(step);
            }
            // step_in
            TraceNode stepIn = readNode(allSteps);
            step.setStepInNext(stepIn);
            if (stepIn != null) {
                stepIn.setStepInPrevious(step);
            }
            // step_over
            TraceNode stepOver = readNode(allSteps);
            step.setStepOverNext(stepOver);
            if (stepOver != null) {
                stepOver.setStepOverPrevious(step);
            }
            // invocation_parent
            TraceNode invocationParent = readNode(allSteps);
            step.setInvocationParent(invocationParent);
            if (invocationParent != null) {
                invocationParent.addInvocationChild(step);
            }
            // loop_parent
            TraceNode loopParent = readNode(allSteps);
            step.setLoopParent(loopParent);
            if (loopParent != null) {
                loopParent.addLoopChild(step);
            }
            step.setException(readBoolean());
            step.setBytecode(readString());
            step.setInvokingMethod(readString());
        }
        readRWVarValues(allSteps, false);
        readRWVarValues(allSteps, true);
        return allSteps;
    }

    private TraceNode readNode(List<TraceNode> allSteps) throws IOException {
        int nodeOrder = readVarInt();
        if (nodeOrder <= 0) {
            return null;
        }
        return allSteps.get(nodeOrder - 1);
    }

    private void readRWVarValues(List<TraceNode> allSteps, boolean isWrittenVar) throws IOException {
        int i = 0;
        while (i < allSteps.size()) {
            List<List<VarValue>> varsCol = readSerializableList();
            for (List<VarValue> vars : varsCol) {
                if (isWrittenVar) {
                    allSteps.get(i++).setWrittenVariables(vars);
                } else {
                    allSteps.get(i++).setReadVariables(vars);
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    protected <T> List<T> readSerializableList() throws IOException {
        int size = readVarInt();
        if (size == 0) {
            return new ArrayList<>(0);
        }
        byte[] bytes = readByteArray();
        if (bytes == null || bytes.length == 0) {
            return new ArrayList<>(0);
        }
        List<T> list;
        try {
            list = (List<T>) ByteConverterUtil.convertFromBytes(bytes);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            throw new IOException(e);
        }
        return list;
    }
}
