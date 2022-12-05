package jmutation.mutation.explainable.doc.parser.handler;

import jmutation.mutation.explainable.doc.model.JavaComment;

public class BaseFilter extends ProjectParserFilter {
    @Override
    protected boolean privateHandle(JavaComment comment) {
        return false;
    }
}
