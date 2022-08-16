package jmutation.mutation.commands;

import jmutation.mutation.utils.ComparisonOperator;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.InfixExpression;

public class MutationComparisonOperatorCommand extends MutationCommand {
    public MutationComparisonOperatorCommand(ASTNode node) {
        super(node);
    }

    @Override
    public ASTNode executeMutation(){
        InfixExpression ieNode = (InfixExpression) node;
        InfixExpression.Operator thisOp = ieNode.getOperator();
        InfixExpression.Operator replacementOperator = ComparisonOperator.getReplacementOperator(thisOp);
        ieNode.setOperator(replacementOperator);
        return ieNode;
    }

    /**
     * Changing the comparison in for loops can lead to runtime exception (Array access)
     * @return
     */
    @Override
    public boolean canExecute() {
        InfixExpression ieNode = (InfixExpression) node;
        ASTNode parent = ieNode.getParent();
        if (parent instanceof ForStatement) {
            return false;
        }
        return true;
    }
}
