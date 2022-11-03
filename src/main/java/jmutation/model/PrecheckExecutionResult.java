package jmutation.model;

import jmutation.execution.Coverage;
import tracecollection.model.PrecheckResult;

public class PrecheckExecutionResult extends ExecutionResult {
    private final PrecheckResult precheckResult;
    private Coverage coverage;

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

    public boolean testCasePassed() {
        return precheckResult.getProgramMessage().startsWith("true;");
    }

    public Coverage getCoverage() {
        return coverage;
    }

    public void setCoverage(Coverage coverage) {
        this.coverage = coverage;
    }

    public PrecheckResult getPrecheckResult() {
        return precheckResult;
    }
}
