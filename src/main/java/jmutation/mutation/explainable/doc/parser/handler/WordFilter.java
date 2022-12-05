package jmutation.mutation.explainable.doc.parser.handler;

import jmutation.mutation.explainable.doc.model.JavaComment;

public class WordFilter extends ProjectParserFilter {
    private static final String[] NEEDED_WORDS = new String[]{"if", "check"};

    @Override
    protected boolean privateHandle(JavaComment comment) {
        String commentStr = comment.getComment();
        for (String word : NEEDED_WORDS) {
            if (commentStr.contains(word)) {
                return true;
            }
        }
        return false;
    }
}
