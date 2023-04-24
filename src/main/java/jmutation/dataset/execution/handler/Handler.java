package jmutation.dataset.execution.handler;

import jmutation.dataset.execution.Request;

public abstract class Handler {
    protected final Handler nextHandler;

    public Handler(Handler nextHandler) {
        this.nextHandler = nextHandler;
    }

    public void handle(Request request) {
        if (!individualHandler(request)) {
            return;
        }
        nextHandler.handle(request);
    }

    /**
     * Executes custom code. Returns true if it should call the next handler, otherwise, false.
     *
     * @param request
     * @return
     */
    protected abstract boolean individualHandler(Request request);
}
