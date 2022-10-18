package jmutation.mutation.heuristic;

import org.eclipse.jdt.core.dom.*;

/**
 * Returns how likely a given ASTNode should be mutated, if a mutation for the node exists
 */
public class MutationProbabilityCalculator extends ASTVisitor {
    private double probability = 1;

    // Default probability of mutation for all nodes
    @Override
    public void preVisit(ASTNode node) {
        probability = 1;
    }

    @Override
    public boolean visit(InfixExpression node) {
        probability = 0.5;
        return false;
    }

    @Override
    public boolean visit(WhileStatement node) {
        probability = 0.5;
        return false;
    }

    @Override
    public boolean visit(ForStatement node) {
        probability = 0.5;
        return false;
    }

    @Override
    public boolean visit(Block node) {
        probability = 0.1;
        return false;
    }

    @Override
    public boolean visit(IfStatement node) {
        probability = 0.5;
        return false;
    }

    @Override
    public boolean visit(ReturnStatement node) {
        probability = 0.5;
        return false;
    }

    @Override
    public boolean visit(VariableDeclarationStatement node) {
        probability = 0.5;
        return false;
    }

    public double getProbability(ASTNode node) {
        node.accept(this);
        return probability;
    }
}
