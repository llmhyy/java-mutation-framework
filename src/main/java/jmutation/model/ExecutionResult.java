package jmutation.model;

public class ExecutionResult {
	private final String consoleOut;

	public ExecutionResult(String consoleOut) {
		this.consoleOut = consoleOut;
	}

	public boolean isSuccessful() {
		return !consoleOut.contains("FAIL");
	}

	@Override
	public String toString() {
		 return this.consoleOut;
	}
}
