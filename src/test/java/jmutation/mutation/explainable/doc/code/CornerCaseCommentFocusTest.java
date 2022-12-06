package jmutation.mutation.explainable.doc.code;

import jmutation.mutation.explainable.doc.model.JavaComment;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CornerCaseCommentFocusTest {
    @Test
    void focus_commentContainingCornerCaseDetails_returnsCornerCaseDetails() {
        String comment = String.join(System.lineSeparator(), "/**", "* Some description", "* corner case: some explanation", "**/");
        CornerCaseCommentFocus focus = new CornerCaseCommentFocus();
        JavaComment javaComment = new JavaComment(comment, 1, 3, "filePath");
        String focusedComment = focus.focus(javaComment).get(0);
        String expectedComment = "corner case: some explanation";
        assertEquals(expectedComment, focusedComment);
    }
}