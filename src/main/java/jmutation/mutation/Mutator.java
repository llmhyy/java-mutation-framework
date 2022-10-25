package jmutation.mutation;

import jmutation.execution.Coverage;
import jmutation.model.project.Project;
import jmutation.parser.ProjectParser;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public abstract class Mutator {
    protected List<MutationCommand> mutationHistory;

    public abstract Project mutate(Coverage coverage, Project project);

    public List<MutationCommand> getMutationHistory() {
        return mutationHistory;
    }

    public void clearHistory() {
        mutationHistory = new ArrayList<>();
    }

    protected File retrieveFileFromClassName(String className, Project newProject) {
        File root = newProject.getRoot();
        return ProjectParser.getFileOfClass(className, root);
    }

    /**
     * Gets an AST node for the portion of code to be mutated
     *
     * @param unit              Compilation unit to parse
     * @param retriever         ASTVisitor which obtains ASTNodes to mutate from given mutation range
     * @param isRandomRetrieval whether to randomly retrieve the nodes to mutate or get all that is encountered
     * @return The list of ASTNodes to mutate
     */
    protected List<ASTNode> parseRangeToNodes(CompilationUnit unit, MutationASTNodeRetriever retriever,
                                              boolean isRandomRetrieval) {
        retriever.setRandomness(isRandomRetrieval);
        unit.accept(retriever);

        return retriever.getNodes();
    }
}
