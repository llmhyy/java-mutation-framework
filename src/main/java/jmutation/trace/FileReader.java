package jmutation.trace;

import jmutation.model.microbat.PrecheckResult;
import jmutation.model.microbat.InstrumentationResult;
import microbat.model.trace.Trace;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;

public class FileReader {
    TraceInputStream fileStream;

    public FileReader(File traceFile) throws FileNotFoundException {
        fileStream = new TraceInputStream(traceFile);
    }

    public FileReader(String traceFilePath) throws FileNotFoundException {
        this(new File(traceFilePath));
    }

    public PrecheckResult readPrecheck() {
        return fileStream.readPrecheck();
    }

    public InstrumentationResult readInstrumentation() {
         return fileStream.readTraceCollection();
    }

    public Trace readMainTrace() {
        InstrumentationResult instrumentationResult = readInstrumentation();
        List<Trace> traceList = instrumentationResult.getTraces();
        for (Trace trace : traceList) {
            if (trace.isMain()) {
                return trace;
            }
        }
        return null;
    }
}
