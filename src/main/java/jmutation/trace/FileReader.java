package jmutation.trace;

import microbat.model.trace.Trace;

import java.io.*;

public class FileReader {
    TraceInputStream fileStream;
    public FileReader(File traceFile) throws FileNotFoundException {
        fileStream = new TraceInputStream(traceFile);
    }

    public FileReader(String traceFilePath) throws FileNotFoundException {
        this(new File(traceFilePath));
    }

    public Trace read() {
        try {
            return fileStream.readTrace();
        } catch (IOException e) {
            throw new RuntimeException("Issue reading trace from trace stream");
        }
    }
}
