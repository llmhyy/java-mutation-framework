package jmutation.mutation.explainable;

import jmutation.mutation.MutationCommand;
import jmutation.mutation.explainable.client.ExplainableMutationClient;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Javadoc;
import org.eclipse.jdt.core.dom.MethodDeclaration;

public class ExplainableMutationCommand extends MutationCommand {
    private final ExplainableMutationClient explainableMutationClient;

    public ExplainableMutationCommand(ASTNode node, ExplainableMutationClient explainableMutationClient) {
        super(node);
        this.explainableMutationClient = explainableMutationClient;
    }

    @Override
    public ASTNode executeMutation() {
        MethodDeclaration methodDeclaration = (MethodDeclaration) node;
        Javadoc javadoc = methodDeclaration.getJavadoc();
        String methodBody = null;
        String[] mutationResult = explainableMutationClient.generate(javadoc.toString(), methodBody);
        node = parseIntoMethodDeclaration(mutationResult[0], mutationResult[1]);
        return node;
    }

    public MethodDeclaration parseIntoMethodDeclaration(String javadoc, String methodBody) {
        return null;
    }
}
