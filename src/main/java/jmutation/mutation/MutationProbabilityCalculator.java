package jmutation.mutation;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.WhileStatement;

public class MutationProbabilityCalculator extends ASTVisitor {
    private double probability = 1;

    // Default probability of mutation for all nodes
    @Override
    public void preVisit(ASTNode node) {
        probability = 1;
    }

    @Override
    public boolean visit(InfixExpression node) {
        probability = 0.95;
        return false;
    }

    @Override
    public boolean visit(WhileStatement node) {
        probability = 0.9;
        return false;
    }

    @Override
    public boolean visit(ForStatement node) {
        probability = 0.9;
        return false;
    }

    @Override
    public boolean visit(Block node) {
        probability = 0;
        return false;
    }

    @Override
    public boolean visit(IfStatement node) {
        probability = 0.9;
        return false;
    }

    @Override
    public boolean visit(ReturnStatement node) {
        // Not good for debugging testing as it breaks connection between input and output
        probability = 0;
        return false;
    }

    public double getProbability(ASTNode node) {
        node.accept(this);
        return probability;
    }
}
