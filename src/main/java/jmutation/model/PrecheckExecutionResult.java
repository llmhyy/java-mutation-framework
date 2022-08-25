package jmutation.model;

import tracecollection.model.PrecheckResult;

public class PrecheckExecutionResult extends ExecutionResult {
    private PrecheckResult precheckResult;
    public PrecheckExecutionResult(String consoleOut, PrecheckResult precheckResult) {
        super(consoleOut);
        this.precheckResult = precheckResult;
    }

    public boolean isOverLong() {
        return precheckResult.isOverLong();
    }

    public int getTotalSteps() {
        return precheckResult.getTotalSteps();
    }
}
