package jmutation.mutation.commands;

import jmutation.mutation.utils.ComparisonOperator;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.InfixExpression.Operator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class MutationComparisonOperatorCommand extends MutationCommand {
    public MutationComparisonOperatorCommand(ASTNode node) {
        super(node);
    }

    @Override
    public ASTNode executeMutation(){
        Set<Operator> comparisonOperators = ComparisonOperator.getOperatorSet();
        List<Operator> comparisonOperatorsLs = new ArrayList<>(comparisonOperators);
        Collections.shuffle(comparisonOperatorsLs);
        InfixExpression ieNode = (InfixExpression) node;
        InfixExpression.Operator thisOp = ieNode.getOperator();
        for (int i = 0; i < comparisonOperatorsLs.size(); i++) {
            InfixExpression.Operator randomOp = comparisonOperatorsLs.get(i);
            if (randomOp != thisOp) {
                ieNode.setOperator(randomOp);
                break;
            }
        }
        return ieNode;
    }
}
