package jmutation.model;

import jmutation.execution.Coverage;
import microbat.instrumentation.precheck.PrecheckInfo;

public class PrecheckExecutionResult extends ExecutionResult {
    private final PrecheckInfo precheckInfo;
    private Coverage coverage;

    public PrecheckExecutionResult(String consoleOut, PrecheckInfo precheckInfo) {
        super(consoleOut);
        this.precheckInfo = precheckInfo;
    }

    public boolean isOverLong() {
        return precheckInfo.isOverLong();
    }

    public int getTotalSteps() {
        return precheckInfo.getStepTotal();
    }

    public boolean testCasePassed() {
        return precheckInfo.getProgramMsg().startsWith("true;");
    }

    public Coverage getCoverage() {
        return coverage;
    }

    public void setCoverage(Coverage coverage) {
        this.coverage = coverage;
    }

    public PrecheckInfo getPrecheckInfo() {
        return precheckInfo;
    }
}
