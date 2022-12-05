package jmutation.mutation.explainable.doc.model;

import java.util.Objects;

public class JavaComment {
    private final String comment;
    private final int startLine;
    private final int endLine;
    private final String filePath;

    public JavaComment(String comment, int startLine, int endLine, String filePath) {
        this.comment = comment;
        this.startLine = startLine;
        this.endLine = endLine;
        this.filePath = filePath;
    }

    public String getComment() {
        return comment;
    }

    public int getStartLine() {
        return startLine;
    }

    public int getEndLine() {
        return endLine;
    }

    public String getFilePath() {
        return filePath;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JavaComment that = (JavaComment) o;
        return startLine == that.startLine && endLine == that.endLine && comment.equals(that.comment) && filePath.equals(that.filePath);
    }

    @Override
    public int hashCode() {
        return Objects.hash(comment, startLine, endLine, filePath);
    }
}
