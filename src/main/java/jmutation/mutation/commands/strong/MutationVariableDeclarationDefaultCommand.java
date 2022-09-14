package jmutation.mutation.commands.strong;

import jmutation.mutation.commands.MutationCommand;
import jmutation.mutation.utils.DefaultValueReplacements;
import jmutation.mutation.utils.DefaultValues;
import org.eclipse.jdt.core.dom.*;

import java.util.List;

public class MutationVariableDeclarationDefaultCommand extends MutationCommand {
    public MutationVariableDeclarationDefaultCommand(ASTNode node) {
        super(node);
    }

    @Override
    public ASTNode executeMutation() {
        VariableDeclarationStatement variableDeclarationStmt = (VariableDeclarationStatement) node;
        List<VariableDeclarationFragment> varDeclFragments = variableDeclarationStmt.fragments();
        VariableDeclarationFragment fragment = varDeclFragments.get(0);
        Expression replacement = getReplacementExpression();
        fragment.setInitializer(replacement);
        return variableDeclarationStmt;
    }

    @Override
    public boolean canExecute() {
        if (getReplacementExpression() == null) {
            return false;
        }
        return true;
    }

    private Expression getReplacementExpression() {
        VariableDeclarationStatement variableDeclarationStmt = (VariableDeclarationStatement) node;
        List<VariableDeclarationFragment> varDeclFragments = variableDeclarationStmt.fragments();
        VariableDeclarationFragment fragment = varDeclFragments.get(0);
        Type varType = variableDeclarationStmt.getType();
        if (DefaultValues.isDefaultExpression(fragment.getInitializer())) {
            return DefaultValueReplacements.getDefaultReplacementExpression(varType);
        }
        return DefaultValues.getDefaultExpression(varType);
    }
}
