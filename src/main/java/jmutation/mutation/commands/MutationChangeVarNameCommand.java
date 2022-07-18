package jmutation.mutation.commands;

import jmutation.model.ast.ASTNodeRetriever;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import java.util.ArrayList;
import java.util.List;

public class MutationChangeVarNameCommand extends MutationCommand {
    public MutationChangeVarNameCommand(ASTNode node) {
        super(node);
    }

    @Override
    public ASTNode executeMutation() {
        SimpleName simpleName = (SimpleName) node;
        CompilationUnit compilationUnit = (CompilationUnit) simpleName.getRoot();
        ASTNodeRetriever<VariableDeclarationStatement> variableDeclarationASTNodeRetriever = new ASTNodeRetriever<>(VariableDeclarationStatement.class);
        variableDeclarationASTNodeRetriever.setStopNode(simpleName);
        compilationUnit.accept(variableDeclarationASTNodeRetriever);
        List<VariableDeclarationStatement> variableDeclarations = variableDeclarationASTNodeRetriever.getNodes();

        Type currentNodeType = null;
        for (VariableDeclarationStatement vd : variableDeclarations) {
            List<VariableDeclarationFragment> fragments = vd.fragments();
            VariableDeclarationFragment currentFragment = fragments.get(0);
            SimpleName currentName = currentFragment.getName();
            // Use subtree match
            if (currentName == simpleName) {
                currentNodeType = vd.getType();
            }
        }
        List<SimpleName> possibleReplacements = new ArrayList<>();
        for (VariableDeclarationStatement vd : variableDeclarations) {
            Type currentType = vd.getType();
            if (currentType != currentNodeType) {
                continue;
            }
            List<VariableDeclarationFragment> fragments = vd.fragments();
            VariableDeclarationFragment currentFragment = fragments.get(0);
            SimpleName currentName = currentFragment.getName();
            // Use subtree match
            if (currentName == simpleName) {
                continue;
            }
            possibleReplacements.add(currentName);
        }
        if (possibleReplacements.isEmpty()) {
            return null;
        }
        ASTNode parent = simpleName.getParent();
        StructuralPropertyDescriptor locationInParent = simpleName.getLocationInParent();
        int randIdx = (int) Math.round(Math.random() * (possibleReplacements.size() - 1));
        parent.setStructuralProperty(locationInParent, possibleReplacements.get(randIdx));
        return simpleName;
    }
}
