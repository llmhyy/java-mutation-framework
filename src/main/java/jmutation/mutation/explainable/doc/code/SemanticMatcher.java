package jmutation.mutation.explainable.doc.code;

import jmutation.mutation.explainable.doc.model.CodeChunk;
import jmutation.mutation.explainable.doc.model.JavaComment;

public class SemanticMatcher extends CommentCodeMatcher {
    @Override
    public CodeChunk match(String focusedComment, JavaComment comment, String filePath) {
        // Find code segment with semantically similar meaning.
        // 1. get method code
        // 2. Obtain semantic similarity of each line (Iterator), and store the line with the highest similarity
        // 3. Create code chunk from the line (ASTVisitor)

        // Different cases it should handle?
        // 1. removing if statement
        // 2. Removing condition from if statement
        return null;
    }
}
