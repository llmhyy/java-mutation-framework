package jmutation.parser;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ProjectParserTest {
    @Test
    public void getFileContentsOfClass_classFileExists_getFileContents() throws IOException {
        String pathToTestProj = "./src/test/files/jmutation/parser";
        File root = new File(pathToTestProj);
        String classCanonicalName = "org.test.Test$3";
        String actualContents = ProjectParser.getFileContentsOfClass(classCanonicalName, root);
        File actualFile = new File(pathToTestProj + "/src/main/java/org/test/Test.java");
        String expectedContents = Files.readString(actualFile.toPath());
        assertEquals(expectedContents, actualContents);
    }
}
