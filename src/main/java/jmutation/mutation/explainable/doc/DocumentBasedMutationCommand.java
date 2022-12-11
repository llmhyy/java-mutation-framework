package jmutation.mutation.explainable.doc;

import jmutation.mutation.MutationCommand;
import org.eclipse.jdt.core.dom.ASTNode;

public class DocumentBasedMutationCommand extends MutationCommand {
    public DocumentBasedMutationCommand(ASTNode node) {
        super(node);
    }

    @Override
    public ASTNode executeMutation() {
        node = null;
        return null;
    }
}
