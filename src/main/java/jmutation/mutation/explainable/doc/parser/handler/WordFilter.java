package jmutation.mutation.explainable.doc.parser.handler;

import jmutation.mutation.explainable.doc.model.JavaComment;

public class WordFilter extends ProjectParserFilter {

    private static final String[] DEFAULT_REQUIRED_WORDS = new String[]{"if", "check"};
    private final String[] requiredWords;

    public WordFilter() {
        requiredWords = DEFAULT_REQUIRED_WORDS;
    }

    public WordFilter(String[] requiredWords) {
        this.requiredWords = requiredWords;
    }

    @Override
    protected boolean privateHandle(JavaComment comment, Request request) {
        String commentStr = comment.getComment();
        for (String word : requiredWords) {
            if (commentStr.contains(word)) {
                return true;
            }
        }
        request.setSuccessful(false);
        return false;
    }
}
