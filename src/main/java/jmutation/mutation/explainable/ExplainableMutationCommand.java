package jmutation.mutation.explainable;

import jmutation.mutation.MutationCommand;
import jmutation.mutation.explainable.client.ExplainableMutationClient;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Javadoc;
import org.eclipse.jdt.core.dom.MethodDeclaration;

public class ExplainableMutationCommand extends MutationCommand {
    private final ExplainableMutationClient explainableMutationClient;
    private String originalComment = "";
    private String originalMethod = "";
    private String mutatedComment = "";
    private String mutatedMethod = "";

    public ExplainableMutationCommand(ASTNode node, ExplainableMutationClient explainableMutationClient) {
        super(node);
        this.explainableMutationClient = explainableMutationClient;
    }

    @Override
    public ASTNode executeMutation() {
        MethodDeclaration methodDeclaration = (MethodDeclaration) node;
        Javadoc javadoc = methodDeclaration.getJavadoc();
        originalComment = javadoc.toString();
        originalMethod = null;
        String[] mutationResult = explainableMutationClient.generate(originalComment, originalMethod);
        mutatedComment = mutationResult[0];
        mutatedMethod = mutationResult[1];
        node = parseIntoMethodDeclaration(mutatedComment, mutatedMethod);
        return node;
    }

    public MethodDeclaration parseIntoMethodDeclaration(String javadoc, String methodBody) {
        return null;
    }

    public String getOriginalComment() {
        return originalComment;
    }

    public String getOriginalMethod() {
        return originalMethod;
    }

    public String getMutatedComment() {
        return mutatedComment;
    }

    public String getMutatedMethod() {
        return mutatedMethod;
    }
}
