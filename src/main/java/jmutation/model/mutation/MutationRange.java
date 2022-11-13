package jmutation.model.mutation;

public class MutationRange {
    private String className;
    private int startLine;
    private int endLine;

    public MutationRange(String className, int startLine, int endLine) {
        super();
        this.className = className;
        this.startLine = startLine;
        this.endLine = endLine;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public int getStartLine() {
        return startLine;
    }

    public void setStartLine(int startLine) {
        this.startLine = startLine;
    }

    public int getEndLine() {
        return endLine;
    }

    public void setEndLine(int endLine) {
        this.endLine = endLine;
    }
}
