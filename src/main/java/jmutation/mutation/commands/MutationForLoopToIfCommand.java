package jmutation.mutation.commands;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.Statement;

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

        AST ast = forStatement.getAST();
        IfStatement ifStmt = ast.newIfStatement();
        Expression expression = forStatement.getExpression();
        Statement body = forStatement.getBody();
        Expression expCopy = (Expression) ASTNode.copySubtree(expression.getAST(), expression);
        Statement bodyCopy = (Statement) ASTNode.copySubtree(body.getAST(), body);

        try {
            ifStmt.setExpression(expCopy);
            ifStmt.setThenStatement(bodyCopy);
            stmtLs.set(idxOfWhile, ifStmt);
        } catch (Exception e) {
            System.out.println(e);
        }
        return ifStmt;
    }
}
