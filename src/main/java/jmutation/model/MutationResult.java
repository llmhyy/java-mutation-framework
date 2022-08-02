package jmutation.model;

import jmutation.mutation.commands.MutationCommand;
import microbat.model.trace.Trace;

import java.util.List;

public class MutationResult {
    Trace originalTrace;
    Trace mutatedTrace;

    List<MutationCommand> mutationHistory;

    Project originalProject;
    Project mutatedProject;
    public MutationResult(Trace originalTrace, Trace mutatedTrace, List<MutationCommand> mutationHistory, Project originalProject, Project mutatedProject) {
        this.originalTrace = originalTrace;
        this.mutatedTrace = mutatedTrace;
        this.mutationHistory = mutationHistory;
        this.originalProject = originalProject;
        this.mutatedProject = mutatedProject;
    }

    public Trace getOriginalTrace() {
        return originalTrace;
    }

    public Trace getMutatedTrace() {
        return mutatedTrace;
    }

    public List<MutationCommand> getMutationHistory() {
        return mutationHistory;
    }

    public Project getOriginalProject() {
        return originalProject;
    }

    public Project getMutatedProject() {
        return mutatedProject;
    }
}
