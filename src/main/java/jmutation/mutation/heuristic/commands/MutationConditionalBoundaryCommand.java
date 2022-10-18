package jmutation.mutation.heuristic.commands;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.InfixExpression.Operator;

import java.util.HashMap;
import java.util.Map;

public class MutationConditionalBoundaryCommand extends HeuristicMutationCommand {
    public MutationConditionalBoundaryCommand(ASTNode node) {
        super(node);
    }

    static Map<Operator, Operator> replacementMap = new HashMap<>(){{
        put(Operator.LESS, Operator.LESS_EQUALS);
        put(Operator.LESS_EQUALS, Operator.LESS);
        put(Operator.GREATER, Operator.GREATER_EQUALS);
        put(Operator.GREATER_EQUALS, Operator.GREATER);
    }};

    @Override
    public ASTNode executeMutation(){
        InfixExpression ieNode = (InfixExpression) node;
        InfixExpression.Operator thisOp = ieNode.getOperator();
        InfixExpression.Operator replacementOperator = replacementMap.get(thisOp);
        ieNode.setOperator(replacementOperator);
        return ieNode;
    }

    @Override
    public boolean canExecute() {
        InfixExpression ieNode = (InfixExpression) node;
        if (replacementMap.containsKey(ieNode)) {
            return true;
        }
        return false;
    }
}
