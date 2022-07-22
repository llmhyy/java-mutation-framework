package jmutation.trace;

import microbat.model.ClassLocation;
import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;
import java.util.Set;

public class FileReaderTest {
    @Test
    public void readTrace_validTrace_formsCorrectTrace() throws FileNotFoundException {
        String traceFilePath = "./src/test/files/jmutation/trace/dumpFile.exec";
        FileReader fileReader = new FileReader(traceFilePath);
        Set<ClassLocation> classLocationSet = fileReader.readPrecheck();
        assert (classLocationSet.size() == 17);
    }
}
