package jmutation.dataset.bug.model;

import jmutation.model.TestCase;
import jmutation.mutation.MutationCommand;
import org.json.JSONObject;

public class BuggyProject {
    private final TestCase testCase;
    private final MutationCommand command;
    private final String projectName;

    public BuggyProject(TestCase testCase, MutationCommand command, String projectName) {
        this.testCase = testCase;
        this.command = command;
        this.projectName = projectName;
    }

    public TestCase testCase() {
        return testCase;
    }

    public MutationCommand command() {
        return command;
    }

    public String projectName() {
        return projectName;
    }

    public JSONObject createJSONObject() {
        JSONObject value = new JSONObject();
        value.put("testCase", testCase.toString());
        value.put("command", command.toString());
        value.put("projectName", projectName);
        return value;
    }

    public String key() {
        return toString();
    }

    @Override
    public String toString() {
        return projectName + "#" + testCase + "#" + command;
    }
}
