package jmutation.mutation.heuristic.commands;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.Statement;

import java.util.List;

/**
 * Converts for loop to if statement.
 * <pre>
 * for (int i = 0; i &lt; n; i++) {
 *     body
 * }
 *
 * becomes
 *
 * if (true) {
 *     int i = 0;
 *     if (i &lt; n) {body}
 * }
 * </pre>
 */
public class MutationForLoopToIfCommand extends HeuristicMutationCommand {

    public MutationForLoopToIfCommand(ASTNode node) {
        super(node);
    }

    @Override
    public ASTNode executeMutation() {
        ForStatement forStatement = (ForStatement) node;
        Block parent = (Block) forStatement.getParent();
        List<Statement> stmtLs = parent.statements();
        int idxOfWhile = stmtLs.indexOf(forStatement);

        IfStatement wrapperIfStmt = ast.newIfStatement();
        Expression trueExpression = ast.newBooleanLiteral(true);
        Block wrapperIfStmtBlock = ast.newBlock();
        IfStatement ifStmt = ast.newIfStatement();
        Expression expression = forStatement.getExpression();
        List<Expression> initializers = forStatement.initializers();

        Statement body = forStatement.getBody();
        Expression expCopy = (Expression) ASTNode.copySubtree(expression.getAST(), expression);
        Statement bodyCopy = (Statement) ASTNode.copySubtree(body.getAST(), body);

        try {
            ifStmt.setExpression(expCopy);
            ifStmt.setThenStatement(bodyCopy);
            wrapperIfStmt.setExpression(trueExpression);
            wrapperIfStmt.setThenStatement(wrapperIfStmtBlock);
            stmtLs.set(idxOfWhile, wrapperIfStmt);
            List<Statement> wrapperStmtLs = wrapperIfStmtBlock.statements();
            for (Expression initializer : initializers) {
                Expression initializerCopy = (Expression) ASTNode.copySubtree(initializer.getAST(), initializer);
                ExpressionStatement expressionStatement = ast.newExpressionStatement(initializerCopy);
                wrapperStmtLs.add(expressionStatement);
            }
            wrapperStmtLs.add(ifStmt);
        } catch (Exception e) {
            System.out.println(e);
        }
        node = wrapperIfStmt;
        return wrapperIfStmt;
    }
}
