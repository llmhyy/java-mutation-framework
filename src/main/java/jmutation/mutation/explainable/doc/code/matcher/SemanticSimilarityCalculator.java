package jmutation.mutation.explainable.doc.code.matcher;

import jmutation.mutation.explainable.doc.model.JavaComment;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;

/**
 * Obtains the most semantically similar ASTNode in an AST given a comment string
 */
public abstract class SemanticSimilarityCalculator extends ASTVisitor {
    protected final String commentStr;
    protected final JavaComment javaComment;
    protected final ASTNode rootNode;
    protected ASTNode resultNode;

    protected SemanticSimilarityCalculator(String commentStr, JavaComment javaComment, ASTNode rootNode) {
        this.commentStr = commentStr;
        this.rootNode = rootNode;
        this.javaComment = javaComment;
    }

    public abstract ASTNode start();
}
