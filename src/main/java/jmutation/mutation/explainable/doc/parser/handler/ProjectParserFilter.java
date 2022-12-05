package jmutation.mutation.explainable.doc.parser.handler;

import jmutation.mutation.explainable.doc.model.JavaComment;

public abstract class ProjectParserFilter {
    private ProjectParserFilter nextHandler;

    public static void setup(ProjectParserFilter next, ProjectParserFilter... otherHandlers) {
        ProjectParserFilter curr = next;
        for (ProjectParserFilter handler : otherHandlers) {
            curr.nextHandler = handler;
            curr = handler;
        }
        curr.nextHandler = new BaseFilter();
    }

    public void handle(JavaComment comment) {
        if (privateHandle(comment)) {
            nextHandler.handle(comment);
        }
    }

    protected abstract boolean privateHandle(JavaComment comment);
}
