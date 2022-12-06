package jmutation.mutation.explainable.doc.code;

import jmutation.mutation.explainable.doc.model.JavaComment;

import java.util.List;

public abstract class CommentFocus {
    public abstract List<String> focus(JavaComment comment);
}
