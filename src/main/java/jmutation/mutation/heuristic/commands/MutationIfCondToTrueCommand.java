package jmutation.mutation.heuristic.commands;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IfStatement;

public class MutationIfCondToTrueCommand extends HeuristicMutationCommand {

    public MutationIfCondToTrueCommand(ASTNode node) {
        super(node);
    }

    @Override
    public ASTNode executeMutation() {
        IfStatement ifStatement = (IfStatement) node;
        Expression newExpression = ast.newBooleanLiteral(true);
        ifStatement.setExpression(newExpression);
        return ifStatement;
    }
}
