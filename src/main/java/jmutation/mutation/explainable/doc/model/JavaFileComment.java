package jmutation.mutation.explainable.doc.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class JavaFileComment {
    private final List<JavaComment> comments = new ArrayList<>();
    private final String filePath;

    public JavaFileComment(String filePath) {
        this.filePath = filePath;
    }

    public void addComment(JavaComment comment) {
        this.comments.add(comment);
    }

    public List<JavaComment> getComments() {
        return comments;
    }

    public String getFilePath() {
        return filePath;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JavaFileComment that = (JavaFileComment) o;
        return comments.equals(that.comments);
    }

    @Override
    public int hashCode() {
        return Objects.hash(comments);
    }
}
