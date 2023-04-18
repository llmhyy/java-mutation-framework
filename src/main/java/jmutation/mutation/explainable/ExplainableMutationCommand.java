package jmutation.mutation.explainable;

import jmutation.mutation.MutationCommand;
import jmutation.mutation.explainable.client.ExplainableMutationClient;
import jmutation.parser.ProjectParser;
import org.eclipse.jdt.core.dom.*;

import java.util.ArrayList;
import java.util.List;

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
        // Assumes that the method has a javadoc
        MethodDeclaration methodDeclaration = (MethodDeclaration) node;
        Javadoc javadoc = methodDeclaration.getJavadoc();
        originalComment = getMethodSummary(javadoc);

        methodDeclaration.setJavadoc(null);
        originalMethod = methodDeclaration.toString();
        methodDeclaration.setJavadoc(javadoc);

        String[] mutationResult = explainableMutationClient.generate(originalComment, originalMethod);
        mutatedComment = mutationResult[0];
        if (mutatedComment.equals("FAIL")) {
            throw new RuntimeException("Error with python server");
        }
        mutatedMethod = mutationResult[1];
        node = parseIntoMethodDeclaration(mutatedComment, mutatedMethod);
        return node;
    }

    public static String getMethodSummary(Javadoc javadoc) {
        StringBuilder result = new StringBuilder();
        List<String> strings = new ArrayList<>();
        List<TagElement> tags = javadoc.tags();
        for (TagElement tag : tags) {
            List<IDocElement> fragments = tag.fragments();
            for (IDocElement fragment : fragments) {
                if (fragment instanceof TextElement) {
                    TextElement textElement = (TextElement) fragment;
                    strings.add(textElement.getText());
                }
            }
        }
        for (String string : strings) {
            result.append(string);
            result.append(" ");
        }
        result.deleteCharAt(result.length() - 1);
        return result.toString();
    }

    public MethodDeclaration parseIntoMethodDeclaration(String javadoc, String methodBody) {
        String methodStr = String.join(System.lineSeparator(), "/**", " * " + javadoc, "*/", methodBody);
        String classStr = String.join(System.lineSeparator(), "public class Clazz {", methodStr, "}");
        CompilationUnit compilationUnit = ProjectParser.parseCompilationUnit(classStr);
        List<AbstractTypeDeclaration> types = compilationUnit.types();
        TypeDeclaration typeRoot = (TypeDeclaration) types.get(0);
        List<BodyDeclaration> bodyDeclarations = typeRoot.bodyDeclarations();
        return (MethodDeclaration) bodyDeclarations.get(0);
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
