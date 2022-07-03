package jmutation.trace;

import java.io.FileNotFoundException;

import org.junit.jupiter.api.Test;

import microbat.model.trace.Trace;

public class FileReaderTest {
    @Test
    public void readTrace_validTrace_formsCorrectTrace() throws FileNotFoundException {
        String traceFilePath = "./src/test/files/jmutation/trace/traceFile.exec";
        FileReader fileReader = new FileReader(traceFilePath);
        Trace traceList = fileReader.read();
        System.out.println(traceList);
    }
}
