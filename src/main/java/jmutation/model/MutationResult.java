package jmutation.model;

import jmutation.model.project.Project;
import jmutation.mutation.commands.MutationCommand;
import microbat.model.trace.Trace;
import microbat.model.trace.TraceNode;
import tracecollection.model.InstrumentationResult;

import java.util.List;

public class MutationResult {
    InstrumentationResult originalResult;
    InstrumentationResult mutatedResult;

    InstrumentationResult originalResultWithAssertions;

    InstrumentationResult mutatedResultWithAssertions;

    List<MutationCommand> mutationHistory;

    List<TraceNode> rootCauses;

    Project originalProject;
    Project mutatedProject;

    String testClass;
    String testSimpleName;

    boolean testCasePassed;

    public MutationResult(InstrumentationResult originalResult, InstrumentationResult mutatedResult,
                          InstrumentationResult originalResultWithAssertions, InstrumentationResult mutatedResultWithAssertions,
                          List<MutationCommand> mutationHistory, Project originalProject,
                          Project mutatedProject, List<TraceNode> rootCauses, boolean testCasePassed, TestCase testCase) {
        this.originalResult = originalResult;
        this.mutatedResult = mutatedResult;
        this.originalResultWithAssertions = originalResultWithAssertions;
        this.mutatedResultWithAssertions = mutatedResultWithAssertions;
        this.mutationHistory = mutationHistory;
        this.originalProject = originalProject;
        this.mutatedProject = mutatedProject;
        this.rootCauses = rootCauses;
        this.testCasePassed = testCasePassed;
        testClass = testCase.testClass;
        testSimpleName = testCase.simpleName;
    }

    public InstrumentationResult getOriginalResult() {
        return originalResult;
    }

    public InstrumentationResult getMutatedResult() {
        return mutatedResult;
    }

    public InstrumentationResult getOriginalResultWithAssertions() {
        return originalResultWithAssertions;
    }

    public InstrumentationResult getMutatedResultWithAssertions() {
        return mutatedResultWithAssertions;
    }

    public Trace getOriginalTrace() {
        return originalResult.getMainTrace();
    }

    public Trace getMutatedTrace() {
        return mutatedResult.getMainTrace();
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

    public String getTestClass() {
        return testClass;
    }

    public String getTestSimpleName() {
        return testSimpleName;
    }

    public boolean isTestCasePassed() {
        return testCasePassed;
    }
}
