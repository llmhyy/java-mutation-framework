package jmutation.model;

import jmutation.mutation.commands.MutationCommand;
import microbat.model.trace.Trace;
import microbat.model.trace.TraceNode;

import java.util.List;

public class MutationResult {
    Trace originalTrace;
    Trace mutatedTrace;

    List<MutationCommand> mutationHistory;

    List<TraceNode> rootCauses;

    Project originalProject;
    Project mutatedProject;

    List<TestIO> testIOs;
    public MutationResult(Trace originalTrace, Trace mutatedTrace, List<MutationCommand> mutationHistory, Project originalProject, Project mutatedProject, List<TraceNode> rootCauses, List<TestIO> testIOs) {
        this.originalTrace = originalTrace;
        this.mutatedTrace = mutatedTrace;
        this.mutationHistory = mutationHistory;
        this.originalProject = originalProject;
        this.mutatedProject = mutatedProject;
        this.rootCauses = rootCauses;
        this.testIOs = testIOs;
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

    public List<TraceNode> getRootCauses() {
        return rootCauses;
    }
}
