package jmutation.mutation.explainable.doc.confusion.command;

import jmutation.mutation.MutationCommand;
import jmutation.mutation.explainable.doc.knowledge.Parameter;
import org.eclipse.jdt.core.dom.ASTNode;

public class ConfusionMutationCommand extends MutationCommand {
    private final Parameter parameterToReplace;
    private final Parameter replacement;

    public ConfusionMutationCommand(ASTNode node, Parameter parameterToReplace, Parameter replacement) {
        super(node);
        this.parameterToReplace = parameterToReplace;
        this.replacement = replacement;
    }

    @Override
    public ASTNode executeMutation() {
        return null;
    }
}
