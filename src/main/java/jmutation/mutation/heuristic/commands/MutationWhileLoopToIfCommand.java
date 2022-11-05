package jmutation.mutation.heuristic.commands;

import jmutation.mutation.MutationCommand;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.WhileStatement;

public class MutationWhileLoopToIfCommand extends MutationCommand {

    public MutationWhileLoopToIfCommand(ASTNode node) {
        super(node);
    }

    @Override
    public ASTNode executeMutation() {
        WhileStatement whileStatement = (WhileStatement) node;

        IfStatement ifStmt = ast.newIfStatement();
        Expression expression = whileStatement.getExpression();
        Statement body = whileStatement.getBody();
        Expression expCopy = (Expression) ASTNode.copySubtree(expression.getAST(), expression);
        Statement bodyCopy = (Statement) ASTNode.copySubtree(body.getAST(), body);

        ifStmt.setExpression(expCopy);
        ifStmt.setThenStatement(bodyCopy);

        node = ifStmt;

        return ifStmt;
    }
}
