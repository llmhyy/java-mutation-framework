package jmutation.mutation.heuristic.commands;

import jmutation.mutation.MutationCommand;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.InfixExpression.Operator;

import java.util.HashMap;
import java.util.Map;

public class MutationConditionalNegationCommand extends MutationCommand {
    static Map<Operator, Operator> replacementMap = new HashMap<>() {{
        put(Operator.LESS, Operator.GREATER);
        put(Operator.GREATER, Operator.LESS);
        put(Operator.LESS_EQUALS, Operator.GREATER_EQUALS);
        put(Operator.GREATER_EQUALS, Operator.LESS_EQUALS);
        put(Operator.EQUALS, Operator.NOT_EQUALS);
        put(Operator.NOT_EQUALS, Operator.EQUALS);
    }};

    public MutationConditionalNegationCommand(ASTNode node) {
        super(node);
    }

    @Override
    public ASTNode executeMutation() {
        InfixExpression ieNode = (InfixExpression) node;
        InfixExpression.Operator thisOp = ieNode.getOperator();
        InfixExpression.Operator replacementOperator = replacementMap.get(thisOp);
        ieNode.setOperator(replacementOperator);
        return ieNode;
    }

    @Override
    public boolean canExecute() {
        InfixExpression ieNode = (InfixExpression) node;
        Operator op = ieNode.getOperator();
        if (replacementMap.containsKey(op)) {
            return true;
        }
        return false;
    }
}
