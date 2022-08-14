package jmutation.mutation.commands;

import jmutation.mutation.utils.DefaultValues;
import jmutation.mutation.utils.MutationHelper;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.Type;


public class MutationReturnStmtCommand extends MutationCommand {
    public MutationReturnStmtCommand(ASTNode node) {
        super(node);
    }

    @Override
    public ASTNode executeMutation(){
        ReturnStatement returnStatement = (ReturnStatement) node;
        MethodDeclaration methodDeclaration = MutationHelper.getMethodDeclarationParent(node);
        Type returnType = methodDeclaration.getReturnType2();
        Expression replacement = DefaultValues.getDefaultExpression(returnType);
        returnStatement.setExpression(replacement);
        return returnStatement;
    }
}
