package jmutation.mutation.commands;

import org.eclipse.jdt.core.dom.*;

/**
 * Similar to replacing return statement with default values. Except, it checks if the return statement is a primitive value (integer contants, boolean and character literals).
 * The data dependency is already broken, as compared to in MutationReturnStmtCommand, where the mutation may break the data dependencies.
 */
public class MutationReturnStmtLiteralCommand extends MutationReturnStmtCommand {
    public MutationReturnStmtLiteralCommand(ASTNode node) {
        super(node);
    }

    @Override
    public boolean canExecute() {
        ReturnStatement returnStatement = (ReturnStatement) node;
        Expression expression = returnStatement.getExpression();
        if (expression == null) {
            return false;
        }
        LiteralExpressionChecker literalExpressionChecker = new LiteralExpressionChecker();
        if (!literalExpressionChecker.isLiteral(expression)) {
            return false;
        }
        Expression replacement = super.getReplacementExpression();
        if (replacement == null) {
            return false;
        }
        return true;
    }

    private class LiteralExpressionChecker extends ASTVisitor {
        private boolean isLiteral = false;
        private boolean visited = false;

        @Override
        public void preVisit(ASTNode node) {
            isLiteral = false;
        }

        @Override
        public boolean preVisit2(ASTNode node) {
            if (visited) {
                return false;
            }
            return super.preVisit2(node);
        }

        @Override
        public boolean visit(NumberLiteral numberLiteral) {
            isLiteral = true;
            return false;
        }

        @Override
        public boolean visit(CharacterLiteral characterLiteral) {
            isLiteral = true;
            return false;
        }

        @Override
        public boolean visit(BooleanLiteral booleanLiteral) {
            isLiteral = true;
            return false;
        }

        @Override
        public void postVisit(ASTNode node) {
            visited = true;
        }

        public boolean isLiteral(ASTNode node) {
            node.accept(this);
            return isLiteral;
        }
    }

}
