package jmutation.model;

import jmutation.execution.Coverage;
import jmutation.model.microbat.InstrumentationResult;
import microbat.model.trace.Trace;

public class ExecutionResult {
	private Coverage coverage;
	private final String consoleOut;

	private InstrumentationResult instrumentationResult;

	public ExecutionResult(String consoleOut) {
		this.consoleOut = consoleOut;
	}

	public Coverage getCoverage() {
		return coverage;
	}

	public void setCoverage(Coverage coverage) {
		this.coverage = coverage;
	}

	public InstrumentationResult getInstrumentationResult() {
		return instrumentationResult;
	}

	public void setInstrumentationResult(InstrumentationResult instrumentationResult) {
		this.instrumentationResult = instrumentationResult;
	}

	public Trace getTrace() {
		return instrumentationResult.getMainTrace();
	}

	public boolean isSuccessful() { return instrumentationResult.getProgramMsg().startsWith("true;");}

	public boolean hasThrownException() {
		return !instrumentationResult.getProgramMsg().contains("expected:");
	}

	@Override
	public String toString() {
		 return this.consoleOut;
	}
}
