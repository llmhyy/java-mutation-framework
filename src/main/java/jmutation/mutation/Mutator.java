package jmutation.mutation;

import jmutation.execution.Coverage;
import jmutation.model.project.Project;

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
}
