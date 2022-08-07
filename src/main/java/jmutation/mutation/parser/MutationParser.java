package jmutation.mutation.parser;

import jmutation.mutation.ComparisonOperator;
import jmutation.mutation.MathOperator;
import jmutation.mutation.commands.MutationBlockRemovalCommand;
import jmutation.mutation.commands.MutationCommand;
import jmutation.mutation.commands.MutationComparisonOperatorCommand;
import jmutation.mutation.commands.MutationForLoopToIfCommand;
import jmutation.mutation.commands.MutationIfCondToTrueCommand;
import jmutation.mutation.commands.MutationMathOperatorCommand;
import jmutation.mutation.commands.MutationWhileLoopToIfCommand;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.InfixExpression.Operator;
import org.eclipse.jdt.core.dom.WhileStatement;

import java.util.Set;

public class MutationParser extends ASTVisitor {
    MutationCommand command;

    private static boolean isMathOperator(InfixExpression e) {
        Set<Operator> mathOperators = MathOperator.getOperatorSet();
        InfixExpression.Operator currentOp = e.getOperator();
        return mathOperators.contains(currentOp);
    }

    private static boolean isComparisonOperator(InfixExpression e) {
        Set<Operator> comparisonOperators = ComparisonOperator.getOperatorSet();
        InfixExpression.Operator currentOp = e.getOperator();
        return comparisonOperators.contains(currentOp);
    }

    public MutationCommand parse(ASTNode node) {
        node.accept(this);
        return command;
    }

    @Override
    public void preVisit(ASTNode node) {
        command = null;
    }

    @Override
    public boolean visit(InfixExpression node) {
        if (isMathOperator(node)) {
            command = new MutationMathOperatorCommand(node);
        }
        if (isComparisonOperator(node)) {
            command = new MutationComparisonOperatorCommand(node);
        }
        return false;
    }

    @Override
    public boolean visit(WhileStatement node) {
        command = new MutationWhileLoopToIfCommand(node);
        return false;
    }

    @Override
    public boolean visit(ForStatement node) {
        command = new MutationForLoopToIfCommand(node);
        return false;
    }

    @Override
    public boolean visit(Block node) {
        command = new MutationBlockRemovalCommand(node);
        return false;
    }

    @Override
    public boolean visit(IfStatement node) {
        command = new MutationIfCondToTrueCommand(node);
        return false;
    }
}
