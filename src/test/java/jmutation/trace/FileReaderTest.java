package jmutation.trace;

import jmutation.model.trace.Trace;
import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;
import java.util.List;

public class FileReaderTest {
    @Test
    public void readTrace_validTrace_formsCorrectTrace() throws FileNotFoundException {
        String traceFilePath = "./src/test/files/jmutation/trace/traceFile.exec";
        FileReader fileReader = new FileReader(traceFilePath);
        List<Trace> traceList = fileReader.read();
        System.out.println(traceList);
    }
}
