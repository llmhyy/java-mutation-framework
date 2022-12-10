package jmutation.mutation.explainable.doc.code.matcher;

import jmutation.mutation.explainable.doc.model.CodeChunk;
import jmutation.mutation.explainable.doc.model.JavaComment;

public abstract class CommentCodeMatcher {
    public abstract CodeChunk match(String focusedComment, JavaComment comment, String targetFilePath);
}
