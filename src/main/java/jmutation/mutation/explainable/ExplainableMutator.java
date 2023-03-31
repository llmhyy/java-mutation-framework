package jmutation.mutation.explainable;

import jmutation.execution.Coverage;
import jmutation.model.mutation.MutationRange;
import jmutation.model.project.Project;
import jmutation.mutation.MutationCommand;
import jmutation.mutation.Mutator;

import java.util.List;

public class ExplainableMutator extends Mutator {
    @Override
    public Project mutate(Coverage coverage, Project project) {
        return null;
    }

    @Override
    public List<MutationCommand> analyse(List<MutationRange> mutationRanges, Project project) {
        // Get all method declarations in each range
        // Give the model the method + comment separately as string
        // Get back the method + comment
        // Create MethodDeclaration
        // Create the command

        return null;
    }
}
