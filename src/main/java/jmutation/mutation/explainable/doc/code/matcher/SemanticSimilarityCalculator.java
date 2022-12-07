package jmutation.mutation.explainable.doc.code.matcher;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;

public abstract class SemanticSimilarityCalculator extends ASTVisitor {
    protected final String commentStr;
    protected final ASTNode rootNode;
    protected ASTNode resultNode;

    public SemanticSimilarityCalculator(String commentStr, ASTNode rootNode) {
        this.commentStr = commentStr;
        this.rootNode = rootNode;
    }

    public abstract ASTNode start();
}
