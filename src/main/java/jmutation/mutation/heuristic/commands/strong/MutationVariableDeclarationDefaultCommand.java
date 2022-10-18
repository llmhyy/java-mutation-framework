package jmutation.mutation.heuristic.commands.strong;

import jmutation.mutation.heuristic.commands.HeuristicMutationCommand;
import jmutation.mutation.heuristic.utils.DefaultValueReplacements;
import jmutation.mutation.heuristic.utils.DefaultValues;
import org.eclipse.jdt.core.dom.*;

import java.util.List;

public class MutationVariableDeclarationDefaultCommand extends HeuristicMutationCommand {
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
