package jmutation.mutation.explainable.doc.parser;

import jmutation.mutation.explainable.doc.model.JavaComment;
import jmutation.mutation.explainable.doc.model.JavaFileComment;
import jmutation.mutation.explainable.doc.model.Project;
import jmutation.mutation.explainable.doc.parser.ProjectParser.ProjectParserBuilder;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ProjectParserTest {
    private static final String SAMPLE_PROJECT_PATH = String.join(File.separator, "src", "test", "files",
            "jmutation", "mutation", "explainable", "doc", "parser", "sample-project");

    @Test
    void parse_validProjectWithFilter_obtainsCorrectDocumentation() throws IOException {
        Project project = new Project(SAMPLE_PROJECT_PATH);
        ProjectParser parser = new ProjectParserBuilder(project).build();
        Iterator<JavaFileComment> javaCommentIterator = parser.parse();
        List<JavaFileComment> expectedJavaComments = new ArrayList<>();
        String sampleCanonicalPath = new File(String.join(File.separator, SAMPLE_PROJECT_PATH, "Sample.java")).getCanonicalPath();
        String sample1CanonicalPath = new File(String.join(File.separator, SAMPLE_PROJECT_PATH, "Sample1.java")).getCanonicalPath();
        JavaFileComment sampleComments = new JavaFileComment();
        JavaFileComment sample1Comments = new JavaFileComment();
        sampleComments.addComment(new JavaComment(String.join(System.lineSeparator(), "/**", " * This is a sample class", " */"), 1, 3, sampleCanonicalPath)); // correct file, start and end lines, and comment string
        sampleComments.addComment(new JavaComment("// This is an inline comment", 7, 7, sampleCanonicalPath)); // correct file, start and end lines, and comment string
        sample1Comments.addComment(new JavaComment(String.join(System.lineSeparator(), "/**", "     * This is another sample method", "     *", "     * @param a first param", "     * @param b second param", "     * @return some integer", "     */"), 2, 8, sample1CanonicalPath)); // correct file, start and end lines, and comment string
        expectedJavaComments.add(sampleComments); // correct file, start and end lines, and comment string
        expectedJavaComments.add(sample1Comments); // correct file, start and end lines, and comment string
        int idx = 0;
        while (javaCommentIterator.hasNext()) {
            JavaFileComment comment = javaCommentIterator.next();
            assertEquals(expectedJavaComments.get(idx), comment);
            idx++;
        }
    }
}
