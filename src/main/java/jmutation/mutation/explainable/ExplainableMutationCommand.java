package jmutation.mutation.explainable;

import jmutation.mutation.MutationCommand;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.MethodDeclaration;

public class ExplainableMutationCommand extends MutationCommand {
    private final MethodDeclaration replacement;

    public ExplainableMutationCommand(ASTNode node, MethodDeclaration replacement) {
        super(node);
        this.replacement = replacement;
    }

    @Override
    public ASTNode executeMutation() {
        node = replacement;
        return node;
    }
}
