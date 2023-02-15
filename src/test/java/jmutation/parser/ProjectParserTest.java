package jmutation.parser;

import jmutation.model.TestCase;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

    @Test
    void getAllMethod_JUnit4TestCases_GetsThemCorrectly() {
        String fileContent = String.join(System.lineSeparator(),
                "package some.package;",
                "import org.junit.Test;",
                "public class SomeClass {",
                "@Test",
                "public void test() {",
                "}",
                "@Test",
                "public void test1() {",
                "}",
                "}"
        );
        List<TestCase> actualTestCases = ProjectParser.getAllMethod(fileContent);
        List<TestCase> expectedTestCases = new ArrayList<>();
        expectedTestCases.add(new TestCase("test()", 3, 5, "test", "SomeClass", null));
        expectedTestCases.add(new TestCase("test1()", 6, 8, "test1", "SomeClass", null));
        assertEquals(expectedTestCases, actualTestCases);
    }

    @Test
    void getAllMethods_JUnit3TestCases_GetsThemCorrectly() {
        String fileContent = String.join(System.lineSeparator(),
                "package some.package;",
                "import junit.framework.TestCase;",
                "public class SomeClass extends TestCase {",
                "public void setUp() {",
                "}",
                "public void tearDown() {",
                "}",
                "public void test() {",
                "}",
                "void test2() {",
                "}",
                "public void test1() {",
                "}",
                "}"
        );
        List<TestCase> actualTestCases = ProjectParser.getAllMethod(fileContent);
        List<TestCase> expectedTestCases = new ArrayList<>();
        expectedTestCases.add(new TestCase("test()", 7, 8, "test", "SomeClass", null));
        expectedTestCases.add(new TestCase("test1()", 11, 12, "test1", "SomeClass", null));
        assertEquals(expectedTestCases, actualTestCases);
    }

    @Test
    void isJUnit3_JUnit3TestCases_ReturnsTrue() {
        String fileContent = String.join(System.lineSeparator(),
                "package some.package;",
                "import junit.framework.TestCase;",
                "public class SomeClass extends TestCase {",
                "public void setUp() {",
                "}",
                "public void tearDown() {",
                "}",
                "public void test() {",
                "}",
                "void test2() {",
                "}",
                "public void test1() {",
                "}",
                "}"
        );
        CompilationUnit unit = ProjectParser.parseCompilationUnit(fileContent);
        assertTrue(ProjectParser.isJUnit3(unit));
    }

    @Test
    void isJUnit3_JUnit3TestCasesAndNoImports_ReturnsTrue() {
        String fileContent = String.join(System.lineSeparator(),
                "package some.package;",
                "public class SomeClass extends junit.framework.TestCase {",
                "public void setUp() {",
                "}",
                "public void tearDown() {",
                "}",
                "public void test() {",
                "}",
                "void test2() {",
                "}",
                "public void test1() {",
                "}",
                "}"
        );
        CompilationUnit unit = ProjectParser.parseCompilationUnit(fileContent);
        assertTrue(ProjectParser.isJUnit3(unit));
    }

    @Test
    void isJUnit3_JUnit4TestCases_GetsThemCorrectly() {
        String fileContent = String.join(System.lineSeparator(),
                "package some.package;",
                "import org.junit.Test;",
                "public class SomeClass {",
                "@Test",
                "public void test() {",
                "}",
                "@Test",
                "public void test1() {",
                "}",
                "}"
        );
        CompilationUnit unit = ProjectParser.parseCompilationUnit(fileContent);
        assertFalse(ProjectParser.isJUnit3(unit));
    }

    @Test
    void getAllMethods_NotTestFile_ShouldBeEmpty() {
        String fileContent = String.join(System.lineSeparator(),
                "package some.package;",
                "public class SomeClass {",
                "public void test() {",
                "}",
                "public void test1() {",
                "}",
                "}"
        );
        List<TestCase> actualTestCases = ProjectParser.getAllMethod(fileContent);
        assertTrue(actualTestCases.isEmpty());
    }
}
