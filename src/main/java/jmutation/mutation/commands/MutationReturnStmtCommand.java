package jmutation.mutation.commands;

import jmutation.model.ast.ASTNodeParentRetriever;
import jmutation.mutation.utils.DefaultValueReplacements;
import jmutation.mutation.utils.DefaultValues;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.CharacterLiteral;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.Type;


public class MutationReturnStmtCommand extends MutationCommand {
    public MutationReturnStmtCommand(ASTNode node) {
        super(node);
    }

    @Override
    public ASTNode executeMutation(){
        ReturnStatement returnStatement = (ReturnStatement) node;
        Expression replacement = getReplacementExpression();
        returnStatement.setExpression(replacement);
        return returnStatement;
    }

    /**
     * try {
     *     return methodCall();
     * } catch (Exception e) {}
     * ->
     * try {
     *     return 0.0;
     * } catch (Exception e) {}

     * Can lead to compilation errors. Check for those cases.
     * @return
     */
    @Override
    public boolean canExecute() {
        ReturnStatement returnStatement = (ReturnStatement) node;
        Expression expression = returnStatement.getExpression();
        if (expression == null) {
            return false;
        }
        if (expression instanceof MethodInvocation) {
            ASTNodeParentRetriever<MethodDeclaration> methodDeclarationASTNodeParentRetriever = new ASTNodeParentRetriever<>(MethodDeclaration.class);
            ASTNodeParentRetriever<TryStatement> tryStatementASTNodeParentRetriever = new ASTNodeParentRetriever<>(TryStatement.class);
            TryStatement tryStatement = tryStatementASTNodeParentRetriever.getParentOfType(node);
            if (tryStatement != null) {
                return false;
            }
        }
        Expression replacement = getReplacementExpression();
        if (replacement == null) {
            return false;
        }
        return true;
    }

    private Expression getReplacementExpression() {
        ASTNodeParentRetriever<MethodDeclaration> methodDeclarationASTNodeParentRetriever = new ASTNodeParentRetriever<>(MethodDeclaration.class);
        MethodDeclaration methodDeclaration = methodDeclarationASTNodeParentRetriever.getParentOfType(node);
        Type returnType = methodDeclaration.getReturnType2();
        ReturnStatement returnStatement = (ReturnStatement) node;
        Expression expression = returnStatement.getExpression();
        DefaultExpressionChecker defaultExpressionChecker = new DefaultExpressionChecker();
        boolean isDefaultExpression = defaultExpressionChecker.check(expression);
        Expression replacement;
        if (isDefaultExpression) {
            replacement = DefaultValueReplacements.getDefaultReplacementExpression(returnType);
        } else {
            replacement = DefaultValues.getDefaultExpression(returnType);
        }
        return replacement;
    }

    private class DefaultExpressionChecker extends ASTVisitor {
        private boolean isDefault = false;
        private boolean visited = false;
        @Override
        public void preVisit(ASTNode node) {
            isDefault = false;
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
            String token = numberLiteral.getToken();
            int idxOfDot = token.indexOf('.');
            isDefault = true;
            for (int i = 0; i < token.length(); i++) {
                if (i == idxOfDot) {
                    continue;
                }
                char current = token.charAt(i);
                if (current != '0') {
                    isDefault = false;
                    break;
                }
            }
            return false;
        }
        @Override
        public boolean visit(CharacterLiteral characterLiteral) {
            isDefault = characterLiteral.getEscapedValue().equals('\u0000');
            return false;
        }
        @Override
        public boolean visit(BooleanLiteral booleanLiteral) {
            isDefault = !booleanLiteral.booleanValue();
            return false;
        }

        @Override
        public void postVisit(ASTNode node) {
            visited = true;
        }

        public boolean check(ASTNode node) {
            node.accept(this);
            return isDefault;
        }
    }

}
