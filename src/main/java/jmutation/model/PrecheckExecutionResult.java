package jmutation.model;

import jmutation.model.microbat.PrecheckResult;

public class PrecheckExecutionResult extends ExecutionResult {
    private PrecheckResult precheckResult;
    public PrecheckExecutionResult(String consoleOut, PrecheckResult precheckResult) {
        super(consoleOut);
        this.precheckResult = precheckResult;
    }

    public boolean isOverLong() {
        return precheckResult.isOverLong();
    }
}
