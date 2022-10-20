package jmutation.mutation.semantic.semseed.io;

import jmutation.mutation.semantic.semseed.io.handler.FileHandler;
import jmutation.mutation.semantic.semseed.model.Pattern;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class PatternIOTest {
    @Test
    public void readAll_validPatternFile_readsAllPatterns() throws IOException {
        FileHandler fileHandler = new FileHandler();
        fileHandler.setSource(String.join(File.separator, "src", "test", "files", "jmutation", "mutation", "semantic", "semseed", "io", "patterns.json"));
        PatternIO patternIO = new PatternIO(fileHandler);
        List<Pattern> patternList = patternIO.readAll();
        assertTrue(patternList.size() == 2);
    }
}
