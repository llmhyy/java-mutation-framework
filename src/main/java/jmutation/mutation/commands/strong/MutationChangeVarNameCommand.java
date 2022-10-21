package jmutation.mutation.commands.strong;

import jmutation.model.ast.ASTNodeParentRetriever;
import jmutation.model.ast.ASTNodeRetriever;
import jmutation.mutation.commands.MutationCommand;
import jmutation.utils.RandomSingleton;
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
    private List<SimpleName> possibleReplacements = null;
    private Type nodeType = null;

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

        List<SimpleName> possibleReplacements = getPossibleReplacements();

        if (possibleReplacements.isEmpty()) {
            return null;
        }
        replace(possibleReplacements);
        return simpleName;
    }

    @Override
    public boolean canExecute() {
        // Check that the obtained SimpleName is not the declared variable.
        ASTNodeParentRetriever<VariableDeclarationStatement> parentRetriever = new ASTNodeParentRetriever<>(VariableDeclarationStatement.class);
        VariableDeclarationStatement variableDeclarationStatement = parentRetriever.getParentOfType(node);
        if (variableDeclarationStatement == null) return true;
        List<VariableDeclarationFragment> fragments = variableDeclarationStatement.fragments();
        if (fragments.isEmpty()) return true;
        if (fragments.get(0).getName().equals(node)) return false;
        // If no possible replacements, return false, otherwise true
        return !getPossibleReplacements().isEmpty();
    }

    /**
     * If the ASTNode is in a variable declaration, it will return the SimpleName of variable being declared. Otherwise, null;
     * e.g. int a = b + c; and b is chosen, it will return a
     *
     * @return SimpleName of variable declared in the statement, if node is used in variable declaration
     */
    private SimpleName varDeclaredInStatement() {
        ASTNodeParentRetriever<VariableDeclarationStatement> parentRetriever = new ASTNodeParentRetriever<>(VariableDeclarationStatement.class);
        VariableDeclarationStatement variableDeclarationStatement = parentRetriever.getParentOfType(node);
        if (variableDeclarationStatement == null) return null;
        List<VariableDeclarationFragment> fragments = variableDeclarationStatement.fragments();
        if (fragments.isEmpty()) return null;
        return fragments.get(0).getName();
    }

    private void replace(List<SimpleName> possibleReplacements) {
        SimpleName simpleName = (SimpleName) node;
        ASTNode parent = simpleName.getParent();
        StructuralPropertyDescriptor locationInParent = simpleName.getLocationInParent();
        int randIdx = (int) Math.round(RandomSingleton.getSingleton().random() * (possibleReplacements.size() - 1));
        SimpleName replacement = possibleReplacements.get(randIdx);
        SimpleName replacementClone = ast.newSimpleName(replacement.getIdentifier());
        parent.setStructuralProperty(locationInParent, replacementClone);
        node = replacementClone;
    }

    private List<SimpleName> getPossibleReplacements() {
        if (possibleReplacements != null) {
            return possibleReplacements;
        }
        List<SimpleName> possibleReplacements = new ArrayList<>();
        Type currentNodeType = getNodeType();
        if (currentNodeType == null) {
            this.possibleReplacements = possibleReplacements;
            return possibleReplacements;
        }
        SimpleName varDeclaredInStatement = varDeclaredInStatement();
        SimpleName simpleName = (SimpleName) node;
        CompilationUnit compilationUnit = (CompilationUnit) simpleName.getRoot();
        ASTNodeRetriever<VariableDeclarationStatement> variableDeclarationASTNodeRetriever = new ASTNodeRetriever<>(VariableDeclarationStatement.class);
        variableDeclarationASTNodeRetriever.setStopNode(simpleName);
        compilationUnit.accept(variableDeclarationASTNodeRetriever);
        List<VariableDeclarationStatement> variableDeclarations = variableDeclarationASTNodeRetriever.getNodes();
        for (VariableDeclarationStatement vd : variableDeclarations) {
            Type currentType = vd.getType();
            boolean isSameType = currentNodeType.toString().equals(currentType.toString());
            if (!isSameType) {
                continue;
            }
            List<VariableDeclarationFragment> fragments = vd.fragments();
            VariableDeclarationFragment currentFragment = fragments.get(0);
            SimpleName currentName = currentFragment.getName();
            boolean isSameName = currentName.toString().equals(simpleName.toString());
            boolean isDeclaredVar = currentName.equals(varDeclaredInStatement);
            if (isSameName || isDeclaredVar) {
                continue;
            }
            possibleReplacements.add(currentName);
        }
        this.possibleReplacements = possibleReplacements;
        return possibleReplacements;
    }

    private Type getNodeType() {
        if (nodeType != null) {
            return nodeType;
        }
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
            boolean isSameName = currentName.toString().equals(simpleName.toString());
            if (isSameName) {
                currentNodeType = vd.getType();
            }
        }
        nodeType = currentNodeType;
        return nodeType;
    }
}
