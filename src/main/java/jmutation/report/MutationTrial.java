/**
 *
 */
package jmutation.report;


import jmutation.model.TestCase;
import jmutation.mutation.commands.MutationCommand;

public class MutationTrial {
    private String projectName;
    private TestCase testCase;
    private MutationCommand mutationCommand;
    private String programMsg;

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
