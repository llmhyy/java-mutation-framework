package jmutation.mutation.commands;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.WhileStatement;

import java.util.List;

public class MutationWhileLoopToIfCommand extends MutationCommand {

    public MutationWhileLoopToIfCommand(ASTNode node) {
        super(node);
    }

    @Override
    public ASTNode executeMutation() {
        WhileStatement whileStatement = (WhileStatement) node;
        Block parent = (Block) whileStatement.getParent();
        List<Statement> stmtLs = parent.statements();
        int idxOfWhile = stmtLs.indexOf(whileStatement);

        IfStatement ifStmt = ast.newIfStatement();
        Expression expression = whileStatement.getExpression();
        Statement body = whileStatement.getBody();
        Expression expCopy = (Expression) ASTNode.copySubtree(expression.getAST(), expression);
        Statement bodyCopy = (Statement) ASTNode.copySubtree(body.getAST(), body);

        ifStmt.setExpression(expCopy);
        ifStmt.setThenStatement(bodyCopy);
        stmtLs.set(idxOfWhile, ifStmt);

        return ifStmt;
    }
}
