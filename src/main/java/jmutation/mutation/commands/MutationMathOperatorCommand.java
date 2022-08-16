package jmutation.mutation.commands;

import jmutation.mutation.utils.MathOperator;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.InfixExpression;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Mutate operators (+, -, /, etc)
 */
public class MutationMathOperatorCommand extends MutationCommand{
    public MutationMathOperatorCommand(ASTNode node) {
        super(node);
    }

    @Override
    public ASTNode executeMutation(){
        InfixExpression ieNode = (InfixExpression) node;
        InfixExpression.Operator thisOp = ieNode.getOperator();
        InfixExpression.Operator replacementOperator = MathOperator.getReplacementOperator(thisOp);
        ieNode.setOperator(replacementOperator);
        return ieNode;
    }
}
