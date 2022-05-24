package jmutation.execution;

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

	@Override
	public String toString() {
		 return this.consoleOut;
	}
}
