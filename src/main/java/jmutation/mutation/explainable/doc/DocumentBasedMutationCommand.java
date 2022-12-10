package jmutation.mutation.explainable.doc;

import jmutation.mutation.MutationCommand;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;

public class DocumentBasedMutationCommand extends MutationCommand {
    public DocumentBasedMutationCommand(ASTNode node) {
        super(node);
    }

    @Override
    public ASTNode executeMutation() {
        IfStatement ifStatement = (IfStatement) node;
        StructuralPropertyDescriptor descriptor = ifStatement.getLocationInParent();
        ASTNode parent = ifStatement.getParent();
        parent.setStructuralProperty(descriptor, null);
        return ifStatement;
    }
}
