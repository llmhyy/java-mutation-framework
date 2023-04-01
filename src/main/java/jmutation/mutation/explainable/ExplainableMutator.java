package jmutation.mutation.explainable;

import jmutation.execution.Coverage;
import jmutation.model.ast.ASTNodeRetriever;
import jmutation.model.mutation.MutationRange;
import jmutation.model.project.Project;
import jmutation.mutation.MutationCommand;
import jmutation.mutation.Mutator;
import jmutation.mutation.explainable.client.ExplainableMutationClient;
import jmutation.parser.ProjectParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class ExplainableMutator extends Mutator {
    private final ExplainableMutationClient client;

    public ExplainableMutator(ExplainableMutationClient client) {
        this.client = client;
    }

    @Override
    public Project mutate(Coverage coverage, Project project) {
        return null;
    }

    public List<MutationCommand> analyse(List<MutationRange> mutationRanges, Project project) {
        // Each mutation range represents a class. Get all method declarations in each class
        List<MutationCommand> result = new ArrayList<>();
        for (MutationRange range : mutationRanges) {
            String className = range.getClassName();
            File file = retrieveFileFromClassName(className, project);
            String fileContent;
            try {
                fileContent = Files.readString(file.toPath());
            } catch (IOException e) {
                throw new RuntimeException("Could not read file at " + file.toPath());
            }
            CompilationUnit unit = ProjectParser.parseCompilationUnit(fileContent);
            ASTNodeRetriever<MethodDeclaration> methodDeclarationASTNodeRetriever = new ASTNodeRetriever<>(MethodDeclaration.class);
            unit.accept(methodDeclarationASTNodeRetriever);
            List<MethodDeclaration> methodDeclarations = methodDeclarationASTNodeRetriever.getNodes();
            List<MutationCommand> mutationCommands = convertMethodDeclarationsToMutationCmds(methodDeclarations);
            if (mutationCommands == null) {
                continue;
            }
            result.addAll(mutationCommands);
        }
        return result;
    }

    private List<MutationCommand> convertMethodDeclarationsToMutationCmds(List<MethodDeclaration> methodDeclarations) {
        List<MutationCommand> mutationCommands = new ArrayList<>();
        for (MethodDeclaration methodDeclaration : methodDeclarations) {
            mutationCommands.add(new ExplainableMutationCommand(methodDeclaration, client));
        }
        return mutationCommands;
    }
}
