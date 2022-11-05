package jmutation.mutation.heuristic.commands.strong;

import jmutation.model.ast.ASTNodeParentRetriever;
import jmutation.mutation.MutationCommand;
import jmutation.mutation.heuristic.utils.DefaultValueReplacements;
import jmutation.mutation.heuristic.utils.DefaultValues;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.Type;

/**
 * Replace return value with some default value
 */
public class MutationReturnStmtCommand extends MutationCommand {
    public MutationReturnStmtCommand(ASTNode node) {
        super(node);
    }

    @Override
    public ASTNode executeMutation() {
        ReturnStatement returnStatement = (ReturnStatement) node;
        Expression replacement = getReplacementExpression();
        returnStatement.setExpression(replacement);
        return returnStatement;
    }

    /**
     * <pre>
     * try {
     *     return methodCall();
     * } catch (Exception e) {}
     * -&gt;
     * try {
     *     return 0.0;
     * } catch (Exception e) {}
     * </pre>
     * Can lead to compilation errors. Check for those cases.
     *
     * @return
     */
    @Override
    public boolean canExecute() {
        ReturnStatement returnStatement = (ReturnStatement) originalNode;
        Expression expression = returnStatement.getExpression();
        if (expression == null) {
            return false;
        }
        if (expression instanceof MethodInvocation) {
            ASTNodeParentRetriever<TryStatement> tryStatementASTNodeParentRetriever = new ASTNodeParentRetriever<>(TryStatement.class);
            TryStatement tryStatement = tryStatementASTNodeParentRetriever.getParentOfType(originalNode);
            if (tryStatement != null) {
                return false;
            }
        }
        Expression replacement = getReplacementExpression();
        return replacement != null;
    }

    protected Expression getReplacementExpression() {
        ASTNodeParentRetriever<MethodDeclaration> methodDeclarationASTNodeParentRetriever = new ASTNodeParentRetriever<>(MethodDeclaration.class);
        MethodDeclaration methodDeclaration = methodDeclarationASTNodeParentRetriever.getParentOfType(originalNode);
        Type returnType = methodDeclaration.getReturnType2();
        ReturnStatement returnStatement = (ReturnStatement) originalNode;
        Expression expression = returnStatement.getExpression();
        boolean isDefaultExpression = DefaultValues.isDefaultExpression(expression);
        Expression replacement;
        if (isDefaultExpression) {
            replacement = DefaultValueReplacements.getDefaultReplacementExpression(returnType);
        } else {
            replacement = DefaultValues.getDefaultExpression(returnType);
        }
        return replacement;
    }
}
