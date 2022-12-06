package jmutation.mutation.explainable.doc.model;

import org.eclipse.jdt.core.dom.ASTNode;

import java.util.Objects;

public class CodeChunk {
    private final String filePath;
    private final ASTNode node;

    public CodeChunk(String filePath, ASTNode node) {
        this.filePath = filePath;
        this.node = node;
    }

    public String getFilePath() {
        return filePath;
    }

    public ASTNode getNode() {
        return node;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CodeChunk codeChunk = (CodeChunk) o;
        return filePath.equals(codeChunk.filePath) && node.toString().equals(codeChunk.node.toString());
    }

    @Override
    public int hashCode() {
        return Objects.hash(filePath, node.toString());
    }
}
