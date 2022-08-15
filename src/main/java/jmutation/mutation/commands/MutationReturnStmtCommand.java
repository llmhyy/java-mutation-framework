package jmutation.mutation.commands;

import jmutation.model.ast.ASTNodeParentRetriever;
import jmutation.mutation.utils.DefaultValues;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
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
     * Or
     * public void method() throws Exception {
     *     return methodCall();
     * }
     * ->
     * public void method() throws Exception {
     *     return 0.0;
     * }
     * Both can lead to compilation errors. Check for those cases.
     * @return
     */
    @Override
    public boolean canExecute() {
        ReturnStatement returnStatement = (ReturnStatement) node;
        Expression expression = returnStatement.getExpression();
        if (expression instanceof MethodInvocation) {
            ASTNodeParentRetriever<MethodDeclaration> methodDeclarationASTNodeParentRetriever = new ASTNodeParentRetriever<>(MethodDeclaration.class);
            MethodDeclaration methodDeclaration = methodDeclarationASTNodeParentRetriever.getParentOfType(node);
            ASTNodeParentRetriever<TryStatement> tryStatementASTNodeParentRetriever = new ASTNodeParentRetriever<>(TryStatement.class);
            TryStatement tryStatement = tryStatementASTNodeParentRetriever.getParentOfType(node);
            if (!methodDeclaration.thrownExceptionTypes().isEmpty() || tryStatement != null) {
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
        Expression replacement = DefaultValues.getDefaultExpression(returnType);
        return replacement;
    }

}
