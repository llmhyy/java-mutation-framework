package jmutation.mutation.explainable.doc.code;

import jmutation.mutation.MutationTestHelper;
import jmutation.mutation.explainable.doc.code.matcher.SemanticMatcher;
import jmutation.mutation.explainable.doc.model.CodeChunk;
import jmutation.mutation.explainable.doc.model.JavaComment;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CodeChunkGetterTest {
    MutationTestHelper helper = new MutationTestHelper();

    @Test
    void obtain_javaDocCommentWithCornerCase_obtainsCornerCaseChunk() throws IOException {
        String javaFilePath = String.join(File.separator, "src", "test", "files", "jmutation", "mutation", "explainable", "doc", "code", "Sample.java");
        CodeChunkGetter getter = new CodeChunkGetter(new CornerCaseCommentFocus(), new SemanticMatcher());
        JavaComment comment = new JavaComment(String.join(System.lineSeparator(),
                "/** ",
                "     * This is a description",
                "     * corner case: 0 is not allowed ",
                "     */"),
                2, 5, javaFilePath);
        List<CodeChunk> chunks = getter.get(comment, javaFilePath);
        String documentStr = Files.readString(Path.of(javaFilePath));
        helper.parseDocStr(documentStr);
        MethodDeclaration methodDeclaration = (MethodDeclaration) helper.getBodyDeclarations().get(0);
        IfStatement ifStatement = (IfStatement) methodDeclaration.getBody().statements().get(1);
        List<CodeChunk> expectedChunks = new ArrayList<>();
        expectedChunks.add(new CodeChunk(javaFilePath, ifStatement));
        int idx = 0;
        for (CodeChunk chunk : chunks) {
            assertEquals(expectedChunks.get(idx), chunk);
            idx++;
        }
    }
}
