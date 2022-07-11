package jmutation.mutation.commands;

import jmutation.mutation.MathOperator;
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
        Set<InfixExpression.Operator> mathOperators = MathOperator.getOperatorSet();
        List<InfixExpression.Operator> mathOperatorsLs = new ArrayList<>(mathOperators);
        Collections.shuffle(mathOperatorsLs);
        InfixExpression ieNode = (InfixExpression) node;
        InfixExpression.Operator thisOp = ieNode.getOperator();
        for (int i = 0; i < mathOperatorsLs.size(); i++) {
            InfixExpression.Operator randomOp = mathOperatorsLs.get(i);
            if (randomOp != thisOp) {
                ieNode.setOperator(randomOp);
                break;
            }
        }
        return ieNode;
    }
}
