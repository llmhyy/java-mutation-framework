package jmutation.mutation;

import org.eclipse.jdt.core.dom.ASTNode;

public abstract class MutationCommand {
    protected ASTNode node;

    public MutationCommand(ASTNode node) {
        this.node = node;
    }
    public abstract ASTNode executeMutation();
    public boolean canExecute() {
        return true;
    }

    public ASTNode getNode() {
        return node;
    }
}
