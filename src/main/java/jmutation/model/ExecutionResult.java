package jmutation.model;

import jmutation.execution.Coverage;

public class ExecutionResult {
	private Coverage coverage;
	private final String consoleOut;

	public ExecutionResult(String consoleOut) {
		this.consoleOut = consoleOut;
	}

	public Coverage getCoverage() {
		return coverage;
	}

	public void setCoverage(Coverage coverage) {
		this.coverage = coverage;
	}

	public boolean isSuccessful() { return !consoleOut.contains("is successful? false");}

	public boolean hasThrownException() {
		return !consoleOut.contains("expected:");
	}

	@Override
	public String toString() {
		 return this.consoleOut;
	}
}
