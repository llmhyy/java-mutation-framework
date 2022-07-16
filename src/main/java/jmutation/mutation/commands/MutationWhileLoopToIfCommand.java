package jmutation.mutation.commands;

import org.eclipse.jdt.core.dom.*;

import java.util.List;

public class MutationLoopToCondCommand extends MutationCommand {

    public MutationLoopToCondCommand(ASTNode node) {
        super(node);
    }

    @Override
    public ASTNode executeMutation(){
        WhileStatement whileStatement = (WhileStatement) node;
        Block parent = (Block) whileStatement.getParent();
        List<Statement> stmtLs = parent.statements();
        int idxOfWhile = stmtLs.indexOf(whileStatement);

        AST ast = whileStatement.getAST();
        IfStatement ifStmt = ast.newIfStatement();
        Expression expression = whileStatement.getExpression();
        Statement body = whileStatement.getBody();
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
