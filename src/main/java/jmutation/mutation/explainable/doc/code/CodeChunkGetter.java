package jmutation.mutation.explainable.doc.code;

import jmutation.mutation.explainable.doc.model.CodeChunk;
import jmutation.mutation.explainable.doc.model.JavaComment;

import java.util.ArrayList;
import java.util.List;

/**
 * Given a code comment, it will obtain the corresponding code chunk
 */
public class CodeChunkGetter {
    private final CommentFocus focus;
    private final CommentCodeMatcher matcher;

    public CodeChunkGetter(CommentFocus focus, CommentCodeMatcher matcher) {
        this.focus = focus;
        this.matcher = matcher;
    }

    public List<CodeChunk> get(JavaComment comment, String filePath) {
        List<String> focusedComments = focus.focus(comment);
        List<CodeChunk> result = new ArrayList<>();
        for (String focusedComment : focusedComments) {
            CodeChunk chunk = matcher.match(focusedComment, comment, filePath);
            if (chunk == null) {
                continue;
            }
            result.add(chunk);
        }
        return result;
    }
}
