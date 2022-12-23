package jmutation.mutation.explainable.doc.parser;

import jmutation.mutation.explainable.doc.model.JavaComment;
import jmutation.mutation.explainable.doc.model.JavaFileComment;
import jmutation.mutation.explainable.doc.model.Project;
import jmutation.mutation.explainable.doc.parser.ProjectCommentParser.ProjectParserBuilder;
import jmutation.mutation.explainable.doc.parser.handler.WordFilter;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import static jmutation.mutation.explainable.doc.ExplainableMutationTestConstants.PARSER_SAMPLE_PROJECT_PATH;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ProjectCommentParserTest {


    @Test
    void parse_validProject_obtainsCorrectDocumentation() throws IOException {
        Project project = new Project(PARSER_SAMPLE_PROJECT_PATH);
        ProjectCommentParser parser = new ProjectParserBuilder(project).build();
        Iterator<JavaFileComment> javaCommentIterator = parser.parse();
        Set<JavaFileComment> expectedJavaComments = new HashSet<>();
        String sampleCanonicalPath = new File(String.join(File.separator,
                PARSER_SAMPLE_PROJECT_PATH, "Sample.java")).getCanonicalPath();
        String sample1CanonicalPath = new File(String.join(File.separator,
                PARSER_SAMPLE_PROJECT_PATH, "Sample1.java")).getCanonicalPath();
        JavaFileComment sampleComments = new JavaFileComment(sampleCanonicalPath);
        JavaFileComment sample1Comments = new JavaFileComment(sample1CanonicalPath);
        sampleComments.addComment(new JavaComment(String.join(System.lineSeparator(), "/**",
                " * This is a sample class", " */"), 1, 3, sampleCanonicalPath));
        sampleComments.addComment(new JavaComment("// This is an inline comment", 7, 7,
                sampleCanonicalPath));
        sample1Comments.addComment(new JavaComment(String.join(System.lineSeparator(), "/**",
                "     * This is another sample method", "     *", "     * @param a first param",
                "     * @param b second param", "     * @return some integer", "     */"), 2, 8,
                sample1CanonicalPath));
        expectedJavaComments.add(sampleComments);
        expectedJavaComments.add(sample1Comments);
        Set<JavaFileComment> actualJavaComments = new HashSet<>();
        while (javaCommentIterator.hasNext()) {
            JavaFileComment comment = javaCommentIterator.next();
            actualJavaComments.add(comment);
        }
        assertEquals(expectedJavaComments, actualJavaComments);
    }

    @Test
    void parse_validProjectWithFilter_obtainsCorrectDocumentation() throws IOException {
        Project project = new Project(PARSER_SAMPLE_PROJECT_PATH);
        WordFilter wordFilter = new WordFilter(new String[]{"param"});
        ProjectCommentParser parser = new ProjectParserBuilder(project).addFilter(wordFilter).build();
        Iterator<JavaFileComment> javaCommentIterator = parser.parse();
        Set<JavaFileComment> expectedJavaComments = new HashSet<>();
        String sampleCanonicalPath = new File(String.join(File.separator,
                PARSER_SAMPLE_PROJECT_PATH, "Sample.java")).getCanonicalPath();
        String sample1CanonicalPath = new File(String.join(File.separator,
                PARSER_SAMPLE_PROJECT_PATH, "Sample1.java")).getCanonicalPath();
        JavaFileComment sampleComments = new JavaFileComment(sampleCanonicalPath);
        JavaFileComment sample1Comments = new JavaFileComment(sample1CanonicalPath);
        sample1Comments.addComment(new JavaComment(String.join(System.lineSeparator(), "/**",
                "     * This is another sample method", "     *", "     * @param a first param",
                "     * @param b second param", "     * @return some integer", "     */"), 2, 8,
                sample1CanonicalPath));
        expectedJavaComments.add(sampleComments);
        expectedJavaComments.add(sample1Comments);
        Set<JavaFileComment> actualJavaComments = new HashSet<>();
        while (javaCommentIterator.hasNext()) {
            JavaFileComment comment = javaCommentIterator.next();
            actualJavaComments.add(comment);
        }
        assertEquals(expectedJavaComments, actualJavaComments);
    }
}
