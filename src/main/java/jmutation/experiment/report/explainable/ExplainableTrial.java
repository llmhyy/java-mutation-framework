package jmutation.experiment.report.explainable;

// (project name, version, old method, new method, old comment, new comment,
// message, failing tests, total test count)
public class ExplainableTrial {
    private final String projectName;
    private final String version;
    private final String oldMethod;
    private final String newMethod;
    private final String oldComment;
    private final String newComment;
    private final String message;
    private final String command;
    private final String[] failingTests;
    private final int totalTestCount;

    public ExplainableTrial(String projectName, String version, String oldMethod, String newMethod, String oldComment,
                            String newComment, String message, String[] failingTests, int totalTestCount, String command) {
        this.projectName = projectName;
        this.version = version;
        this.oldMethod = oldMethod;
        this.newMethod = newMethod;
        this.oldComment = oldComment;
        this.newComment = newComment;
        this.message = message;
        this.failingTests = failingTests;
        this.totalTestCount = totalTestCount;
        this.command = command;
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

    public String getOldMethod() {
        return oldMethod;
    }

    public String getNewMethod() {
        return newMethod;
    }

    public String getOldComment() {
        return oldComment;
    }

    public String getNewComment() {
        return newComment;
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
}
