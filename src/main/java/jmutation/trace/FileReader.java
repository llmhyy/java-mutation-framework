package jmutation.trace;

import microbat.model.ClassLocation;
import microbat.model.trace.Trace;

import java.io.*;
import java.util.Set;

public class FileReader {
    TraceInputStream fileStream;
    public FileReader(File traceFile) throws FileNotFoundException {
        fileStream = new TraceInputStream(traceFile);
    }

    public FileReader(String traceFilePath) throws FileNotFoundException {
        this(new File(traceFilePath));
    }

    public Set<ClassLocation> read() {
        return fileStream.readClassLocations();
    }
}
