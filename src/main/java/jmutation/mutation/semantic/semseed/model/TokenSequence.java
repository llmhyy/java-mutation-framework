package jmutation.mutation.semantic.semseed.model;

import org.eclipse.jdt.core.dom.ASTNode;

import java.util.List;

public class TokenSequence {
    private List<String> abstractTokens;
    private List<String> concreteTokens;
    private ASTNode node;

    public TokenSequence(List<String> abstractTokens, List<String> concreteTokens, ASTNode node) {
        this.abstractTokens = abstractTokens;
        this.concreteTokens = concreteTokens;
        this.node = node;
    }

    public List<String> getAbstractTokens() {
        return abstractTokens;
    }

    public List<String> getConcreteTokens() {
        return concreteTokens;
    }

    public ASTNode getNode() {
        return node;
    }
}
