package jmutation.dataset.execution.handler;

import jmutation.dataset.execution.Request;

public class BaseHandler extends Handler {
    public BaseHandler() {
        this(null);
    }

    public BaseHandler(Handler nextHandler) {
        super(null);
    }

    @Override
    public void handle(Request request) {
        // NOP
    }

    @Override
    public boolean individualHandler(Request request) {
        return false;
    }

}
