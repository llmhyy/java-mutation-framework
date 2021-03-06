package jmutation.mutation;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.WhileStatement;

public class MutationProbabilityCalculator extends ASTVisitor {
    private double probability = 0;

    @Override
    public void preVisit(ASTNode node) {
        probability = 0;
    }

    @Override
    public boolean visit(InfixExpression node) {
        probability = 0.9;
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
        probability = 0.1;
        return false;
    }

    @Override
    public boolean visit(IfStatement node) {
        probability = 0.9;
        return false;
    }

    public double getProbability(ASTNode node) {
        node.accept(this);
        return probability;
    }
}
