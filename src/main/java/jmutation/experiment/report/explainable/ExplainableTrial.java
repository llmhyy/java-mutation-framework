package jmutation.experiment.report.explainable;

import jmutation.mutation.explainable.ExplainableMutationCommand;

import java.util.Arrays;
import java.util.Objects;

// (project name, version, old method, new method, old comment, new comment,
// message, failing tests, total test count)
public class ExplainableTrial {
    private final String projectName;
    private final String version;
    private final String method;
    private final String mutatedMethod;
    private final String comment;
    private final String mutatedComment;
    private final String message;
    private final String command;
    private final String[] failingTests;
    private final int totalTestCount;

    public ExplainableTrial(String projectName, String version, String method, String mutatedMethod,
                            String comment, String mutatedComment,
                            String message, String command, String[] failingTests, int totalTestCount) {
        this.projectName = projectName;
        this.version = version;
        this.method = method;
        this.mutatedMethod = mutatedMethod;
        this.comment = comment;
        this.mutatedComment = mutatedComment;
        this.message = message;
        this.command = command;
        this.failingTests = failingTests;
        this.totalTestCount = totalTestCount;
    }

    public String getCommand() {
        return command;
    }

    public String getProjectName() {
        return projectName;
    }

    public String getVersion() {
        return version;
    }

    public String getMethod() {
        return method;
    }

    public String getMutatedMethod() {
        return mutatedMethod;
    }

    public String getComment() {
        return comment;
    }

    public String getMutatedComment() {
        return mutatedComment;
    }

    public String getMessage() {
        return message;
    }

    public String[] getFailingTests() {
        return failingTests;
    }

    public int getTotalTestCount() {
        return totalTestCount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExplainableTrial that = (ExplainableTrial) o;
        return totalTestCount == that.totalTestCount && Objects.equals(projectName, that.projectName) && Objects.equals(version, that.version) && Objects.equals(method, that.method) && Objects.equals(mutatedMethod, that.mutatedMethod) && Objects.equals(comment, that.comment) && Objects.equals(mutatedComment, that.mutatedComment) && Objects.equals(message, that.message) && Objects.equals(command, that.command) && Arrays.equals(failingTests, that.failingTests);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(projectName, version, method, mutatedMethod, comment, mutatedComment, message, command, totalTestCount);
        result = 31 * result + Arrays.hashCode(failingTests);
        return result;
    }

    public static class ExplainableTrialFactory { // For stubbing the explainable trial creation process for testability
        public ExplainableTrial create(String projectName, String version, ExplainableMutationCommand mutationCommand,
                                String message, String[] failingTests, int totalTestCount) {
            return new ExplainableTrial(projectName, version, mutationCommand.getMutatedMethod(),
                    mutationCommand.getOriginalMethod(), mutationCommand.getOriginalComment(),
                    mutationCommand.getMutatedComment(), mutationCommand.toString(),
                    message, failingTests, totalTestCount);
        }
    }
}
