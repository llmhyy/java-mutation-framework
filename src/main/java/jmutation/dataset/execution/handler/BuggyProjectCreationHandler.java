package jmutation.dataset.execution.handler;

import jmutation.dataset.bug.creator.BuggyProjectCreator;
import jmutation.dataset.execution.Request;

public class BuggyProjectCreationHandler extends Handler {
    private final BuggyProjectCreator buggyProjectCreator;

    public BuggyProjectCreationHandler(BuggyProjectCreator projectCreator) {
        super(new BaseHandler());
        buggyProjectCreator = projectCreator;
    }

    public BuggyProjectCreationHandler(Handler nextHandler, BuggyProjectCreator projectCreator) {
        super(nextHandler);
        this.buggyProjectCreator = projectCreator;
    }

    @Override
    protected boolean individualHandler(Request request) {
        return buggyProjectCreator.call();
    }
}
