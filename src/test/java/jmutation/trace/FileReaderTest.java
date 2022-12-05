package jmutation.trace;

import microbat.model.ClassLocation;
import org.junit.jupiter.api.Test;
import tracecollection.model.PrecheckResult;

import java.io.FileNotFoundException;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FileReaderTest {
    @Test
    void readTrace_validTrace_formsCorrectTrace() throws FileNotFoundException {
        String traceFilePath = "./src/test/files/jmutation/trace/precheckFile.exec";
        FileReader fileReader = new FileReader(traceFilePath);
        PrecheckResult precheckResult = fileReader.readPrecheck();
        Set<ClassLocation> classLocationSet = precheckResult.getVisitedClassLocations();
        assertEquals(17, classLocationSet.size());
    }
}
