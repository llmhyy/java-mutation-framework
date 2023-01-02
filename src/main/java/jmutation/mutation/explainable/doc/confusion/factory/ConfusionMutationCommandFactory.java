package jmutation.mutation.explainable.doc.confusion.factory;

import jmutation.mutation.explainable.doc.confusion.command.ConfusionMutationCommand;
import jmutation.mutation.explainable.doc.knowledge.APIMethodKnowledge;
import org.eclipse.jdt.core.dom.Javadoc;
import org.eclipse.jdt.core.dom.MethodDeclaration;

public abstract class ConfusionMutationCommandFactory {
    protected final APIMethodKnowledge apiMethodKnowledge;
    protected final Javadoc javadoc;

    protected final MethodDeclaration methodDeclaration;

    public ConfusionMutationCommandFactory(APIMethodKnowledge apiMethodKnowledge, Javadoc javadoc, MethodDeclaration methodDeclaration) {
        this.apiMethodKnowledge = apiMethodKnowledge;
        this.javadoc = javadoc;
        this.methodDeclaration = methodDeclaration;
    }

    public abstract ConfusionMutationCommand createCommand();
}
