package jmutation.model;

import jmutation.mutation.commands.MutationCommand;

import java.util.List;

public class MutationResult {
    PrecheckExecutionResult fixedPrecheckExecutionResult;
    PrecheckExecutionResult mutatedPrecheckExecutionResult;
    TestCase testCase;
    List<MutationCommand> mutationCommandList;

    public MutationResult(PrecheckExecutionResult fixedPrecheckExecutionResult, PrecheckExecutionResult mutatedPrecheckExecutionResult, TestCase testCase, List<MutationCommand> mutationCommandList) {
        this.fixedPrecheckExecutionResult = fixedPrecheckExecutionResult;
        this.mutatedPrecheckExecutionResult = mutatedPrecheckExecutionResult;
        this.testCase = testCase;
        this.mutationCommandList = mutationCommandList;
    }

    public PrecheckExecutionResult getFixedPrecheckExecutionResult() {
        return fixedPrecheckExecutionResult;
    }

    public PrecheckExecutionResult getMutatedPrecheckExecutionResult() {
        return mutatedPrecheckExecutionResult;
    }

    public TestCase getTestCase() {
        return testCase;
    }

    public List<MutationCommand> getMutationCommandList() {
        return mutationCommandList;
    }
}
