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
        File actualFile = ProjectParser.getFileOfClass(classCanonicalName, root);
        String actualContents = Files.readString(actualFile.toPath());
        File expectedFile = new File(pathToTestProj + "/src/main/java/org/test/Test.java");
        String expectedContents = Files.readString(expectedFile.toPath());
        assertEquals(expectedContents, actualContents);
    }

    @Test
    public void getFileContentsOfClass_classFileExists1_getFileContents() throws IOException {
        String pathToTestProj = "./sample/math_70";
        File root = new File(pathToTestProj);
        String classCanonicalName = "org.apache.commons.math.optimization.SimpleScalarValueChecker";
        File actualFile = ProjectParser.getFileOfClass(classCanonicalName, root);
        String actualContents = Files.readString(actualFile.toPath());
        File expectedFile = new File(pathToTestProj + "/src/main/java/org/apache/commons/math/optimization/SimpleScalarValueChecker.java");
        String expectedContents = Files.readString(expectedFile.toPath());
        assertEquals(expectedContents, actualContents);
    }
}
