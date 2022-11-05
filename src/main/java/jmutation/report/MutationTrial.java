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

    public MutationTrial(String projectName, TestCase testCase, MutationCommand mutationCommand, String programMsg) {
        this.projectName = projectName;
        this.testCase = testCase;
        this.mutationCommand = mutationCommand;
        this.programMsg = programMsg;
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
}
