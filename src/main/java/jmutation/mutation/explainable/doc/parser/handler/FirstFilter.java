package jmutation.mutation.explainable.doc.parser.handler;

import jmutation.mutation.explainable.doc.model.JavaComment;

public class FirstFilter extends ProjectParserFilter {
    @Override
    protected boolean privateHandle(JavaComment comment, Request request) {
        return true;
    }
}
