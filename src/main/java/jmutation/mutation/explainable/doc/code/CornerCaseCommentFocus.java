package jmutation.mutation.explainable.doc.code;

import jmutation.mutation.explainable.doc.model.JavaComment;

import java.util.ArrayList;
import java.util.List;

public class CornerCaseCommentFocus extends CommentFocus {
    private static final String[] CORNER_CASE_STRS = new String[]{"corner", "edge"};

    @Override
    public List<String> focus(JavaComment comment) {
        String commentStr = comment.getComment();
        String[] lines = commentStr.split(System.lineSeparator());
        List<String> result = new ArrayList<>();
        for (String line : lines) {
            for (String str : CORNER_CASE_STRS) {
                int idx = line.indexOf(str);
                if (idx == -1) {
                    continue;
                }
                result.add(line.substring(idx));
            }
        }
        return result;
    }
}
