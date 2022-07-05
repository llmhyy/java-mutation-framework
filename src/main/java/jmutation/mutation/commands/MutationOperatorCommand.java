package jmutation.mutation.commands;

import org.eclipse.jdt.core.dom.ASTNode;

/**
 * Mutate operators (+, -, /, etc)
 */
public class MutationOperatorCommand extends MutationCommand{
    public MutationOperatorCommand(ASTNode node) {
        super(node);
    }

    @Override
    public ASTNode executeMutation(){
        return null;
    }
}
