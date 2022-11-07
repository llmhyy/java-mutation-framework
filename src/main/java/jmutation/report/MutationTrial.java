/**
 *
 */
package jmutation.report;


import jmutation.model.TestCase;
import jmutation.mutation.MutationCommand;

public class MutationTrial {
    private final String projectName;
    private final TestCase testCase;
    private final MutationCommand mutationCommand;
    private final String programMsg;
    private final String mutatedProgramMsg;

    private final int traceLength;
    private final int mutatedTraceLength;

    public MutationTrial(String projectName, TestCase testCase, MutationCommand mutationCommand, String programMsg,
                         String mutatedProgramMsg,
                         int traceLength, int mutatedTraceLength) {
        this.projectName = projectName;
        this.testCase = testCase;
        this.mutationCommand = mutationCommand;
        this.programMsg = programMsg;
        this.mutatedProgramMsg = mutatedProgramMsg;
        this.traceLength = traceLength;
        this.mutatedTraceLength = mutatedTraceLength;
    }

    public String getProjectName() {
        return projectName;
    }

    public TestCase getTestCase() {
        return testCase;
    }

    public MutationCommand getMutationCommand() {
        return mutationCommand;
    }

    public String getProgramMsg() {
        return programMsg;
    }

    public String getMutatedProgramMsg() {
        return mutatedProgramMsg;
    }

    public int getMutatedTraceLength() {
        return mutatedTraceLength;
    }

    public int getTraceLength() {
        return traceLength;
    }
}
