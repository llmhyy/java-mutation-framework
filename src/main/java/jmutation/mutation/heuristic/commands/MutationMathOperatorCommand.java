package jmutation.mutation.heuristic.commands;

import jmutation.mutation.MutationCommand;
import jmutation.mutation.heuristic.utils.MathOperator;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.InfixExpression;

/**
 * Mutate operators (+, -, /, etc)
 */
public class MutationMathOperatorCommand extends MutationCommand {
    public MutationMathOperatorCommand(ASTNode node) {
        super(node);
    }

    @Override
    public ASTNode executeMutation() {
        InfixExpression ieNode = (InfixExpression) node;
        InfixExpression.Operator thisOp = ieNode.getOperator();
        InfixExpression.Operator replacementOperator = MathOperator.getReplacementOperator(thisOp);
        ieNode.setOperator(replacementOperator);
        return ieNode;
    }
}
