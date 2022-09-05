package jmutation.trace;

import tracecollection.model.PrecheckResult;
import microbat.model.ClassLocation;
import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;
import java.util.Set;

public class FileReaderTest {
    @Test
    public void readTrace_validTrace_formsCorrectTrace() throws FileNotFoundException {
        String traceFilePath = "./src/test/files/jmutation/trace/precheckFile.exec";
        FileReader fileReader = new FileReader(traceFilePath);
        PrecheckResult precheckResult = fileReader.readPrecheck();
        Set<ClassLocation> classLocationSet = precheckResult.getVisitedClassLocations();
        assert (classLocationSet.size() == 17);
    }
}
