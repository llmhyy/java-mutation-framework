package jmutation.mutation.explainable.doc.parser.handler;

import jmutation.mutation.explainable.doc.model.JavaComment;

public class WordFilter extends ProjectParserFilter {

    private static final String[] DEFAULT_REQUIRED_WORDS = new String[]{"if", "check"};
    private final String[] NEEDED_WORDS;

    public WordFilter() {
        NEEDED_WORDS = DEFAULT_REQUIRED_WORDS;
    }

    public WordFilter(String[] requiredWords) {
        NEEDED_WORDS = requiredWords;
    }

    @Override
    protected boolean privateHandle(JavaComment comment, Request request) {
        String commentStr = comment.getComment();
        for (String word : NEEDED_WORDS) {
            if (commentStr.contains(word)) {
                return true;
            }
        }
        request.setSuccessful(false);
        return false;
    }
}
