package jmutation.mutation.explainable.doc.code.matcher;

import jmutation.mutation.MutationTestHelper;
import jmutation.mutation.explainable.doc.model.CodeChunk;
import jmutation.mutation.explainable.doc.model.JavaComment;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SemanticMatcherTest {
    MutationTestHelper helper = new MutationTestHelper();

    String javaFilePath = String.join(File.separator, "src", "test", "files", "jmutation",
            "mutation", "explainable", "doc", "code", "Sample.java");

    @Test
    void match_givenCornerCaseJavaDocAndCode_matchesCorrectly() throws IOException {
        String focusedComment = "corner case: 0 is not allowed";
        JavaComment jComment = new JavaComment(String.join(System.lineSeparator(), "/**",
                "* Some description", "* " + focusedComment, "*/"),
                2, 5, javaFilePath);
        String targetFilePath = javaFilePath;
        SemanticMatcher matcher = new SemanticMatcher();
        CodeChunk chunk = matcher.match(focusedComment, jComment, targetFilePath);
        String documentStr = Files.readString(Path.of(javaFilePath));
        helper.parseDocStr(documentStr);
        MethodDeclaration methodDeclaration = (MethodDeclaration) helper.getBodyDeclarations().get(0);
        IfStatement ifStatement = (IfStatement) methodDeclaration.getBody().statements().get(1);
        CodeChunk expectedChunk = new CodeChunk(javaFilePath, ifStatement);
        assertEquals(expectedChunk, chunk);
    }

    @Test
    void match_givenCornerCaseInlineCommentAndCode_matchesCorrectly() throws IOException {
        String focusedComment = "corner case: 0 is not allowed";
        JavaComment jComment = new JavaComment("// " + focusedComment,
                8, 8, javaFilePath);
        String targetFilePath = javaFilePath;
        SemanticMatcher matcher = new SemanticMatcher();
        CodeChunk chunk = matcher.match(focusedComment, jComment, targetFilePath);
        String documentStr = Files.readString(Path.of(javaFilePath));
        helper.parseDocStr(documentStr);
        MethodDeclaration methodDeclaration = (MethodDeclaration) helper.getBodyDeclarations().get(0);
        IfStatement ifStatement = (IfStatement) methodDeclaration.getBody().statements().get(1);
        CodeChunk expectedChunk = new CodeChunk(javaFilePath, ifStatement);
        assertEquals(expectedChunk, chunk);
    }
}