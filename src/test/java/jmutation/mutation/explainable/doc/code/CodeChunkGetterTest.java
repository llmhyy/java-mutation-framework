package jmutation.mutation.explainable.doc.code;

import jmutation.mutation.explainable.doc.model.CodeChunk;
import jmutation.mutation.explainable.doc.model.JavaComment;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CodeChunkGetterTest {
    @Test
    void obtain_javaDocCommentWithCornerCase_obtainsCornerCaseChunk() {
        String javaFilePath = String.join(File.separator, "src", "test", "files", "jmutation", "mutation", "explainable", "doc", "code", "Sample.java");
        CodeChunkGetter getter = new CodeChunkGetter(new CornerCaseCommentFocus(), new SemanticMatcher());
        JavaComment comment = new JavaComment(String.join(System.lineSeparator(),
                "/** ",
                "     * This is a description",
                "     * corner case: 0 is not allowed ",
                "     */"),
                2, 5, javaFilePath);
        List<CodeChunk> chunks = getter.get(comment, javaFilePath);
        List<CodeChunk> expectedChunks = new ArrayList<>();
        int idx = 0;
        for (CodeChunk chunk : chunks) {
            assertEquals(expectedChunks.get(idx), chunk);
            idx++;
        }
    }
}
