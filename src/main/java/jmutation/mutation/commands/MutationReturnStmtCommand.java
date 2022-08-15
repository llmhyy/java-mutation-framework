package jmutation.mutation.commands;

import jmutation.mutation.utils.DefaultValues;
import jmutation.mutation.utils.MutationHelper;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.ReturnStatement;
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

    @Override
    public boolean canExecute() {
        ReturnStatement returnStatement = (ReturnStatement) node;
        Expression expression = returnStatement.getExpression();
        if (expression instanceof MethodInvocation) {
            return false;
        }
        Expression replacement = getReplacementExpression();
        if (replacement == null) {
            return false;
        }
        return true;
    }

    private Expression getReplacementExpression() {
        MethodDeclaration methodDeclaration = MutationHelper.getMethodDeclarationParent(node);
        Type returnType = methodDeclaration.getReturnType2();
        Expression replacement = DefaultValues.getDefaultExpression(returnType);
        return replacement;
    }

}
