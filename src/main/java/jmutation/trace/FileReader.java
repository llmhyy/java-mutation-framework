package jmutation.trace;

import microbat.model.ClassLocation;
import microbat.model.trace.Trace;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Set;

public class FileReader {
    TraceInputStream fileStream;

    public FileReader(File traceFile) throws FileNotFoundException {
        fileStream = new TraceInputStream(traceFile);
    }

    public FileReader(String traceFilePath) throws FileNotFoundException {
        this(new File(traceFilePath));
    }

    public Set<ClassLocation> readPrecheck() {
        return fileStream.readClassLocations();
    }

    public Trace readTrace() {
        return fileStream.readTrace();
    }
}
