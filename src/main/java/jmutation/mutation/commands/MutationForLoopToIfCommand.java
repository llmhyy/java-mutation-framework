package jmutation.mutation.commands;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.Statement;

import javax.swing.plaf.nimbus.State;
import java.util.List;

public class MutationForLoopToIfCommand extends MutationCommand {

    public MutationForLoopToIfCommand(ASTNode node) {
        super(node);
    }

    @Override
    public ASTNode executeMutation() {
        ForStatement forStatement = (ForStatement) node;
        Block parent = (Block) forStatement.getParent();
        List<Statement> stmtLs = parent.statements();
        int idxOfWhile = stmtLs.indexOf(forStatement);

        IfStatement ifStmt = ast.newIfStatement();
        Expression expression = forStatement.getExpression();
        List<Expression> initializers = forStatement.initializers();

        Statement body = forStatement.getBody();
        Expression expCopy = (Expression) ASTNode.copySubtree(expression.getAST(), expression);
        Statement bodyCopy = (Statement) ASTNode.copySubtree(body.getAST(), body);

        try {
            ifStmt.setExpression(expCopy);
            ifStmt.setThenStatement(bodyCopy);
            stmtLs.set(idxOfWhile, ifStmt);
            for (int i = initializers.size() - 1; i >= 0; i--) {
                Expression initializer = initializers.get(i);
                initializer.delete();
                ExpressionStatement expressionStatement = ast.newExpressionStatement(initializer);
                stmtLs.add(idxOfWhile, expressionStatement);
            }
        } catch (Exception e) {
            System.out.println(e);
        }
        node = ifStmt;
        return ifStmt;
    }
}
