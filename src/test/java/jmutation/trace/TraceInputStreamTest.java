package jmutation.trace;

import org.junit.jupiter.api.Test;
import tracecollection.model.InstrumentationResult;

import java.io.File;
import java.io.FileNotFoundException;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class TraceInputStreamTest {
    @Test
    public void readTraceCollection_validTraceFile_collectsTraceSuccessfully() throws FileNotFoundException {
        String filePath = "./src/test/files/jmutation/trace/traceFile.exec";
        TraceInputStream traceInputStream = new TraceInputStream(new File(filePath));
        InstrumentationResult instrumentationResult = traceInputStream.readTraceCollection();
        assertTrue(instrumentationResult.getMainTrace() != null);
    }
}
