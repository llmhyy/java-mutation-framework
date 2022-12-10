package jmutation.mutation.explainable.doc.parser.handler;

import jmutation.execution.Coverage;
import jmutation.model.TestCase;
import jmutation.mutation.explainable.doc.model.JavaComment;
import microbat.model.ClassLocation;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import static jmutation.mutation.explainable.doc.ExplainableMutationTestConstants.PARSER_SAMPLE_PROJECT_PATH;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CoverageFilterTest {
    private static final String SAMPLE_FILE_PATH = String.join(File.separator, PARSER_SAMPLE_PROJECT_PATH, "Sample.java");

    @Test
    void handle_commentFromSameClassAsCoverage_doesNotFilter() {
        Coverage coverage = new Coverage();
        Set<ClassLocation> classLocationSet = new HashSet<>();
        ClassLocation classLocation = new ClassLocation("Sample", "methodSignature", 1);
        classLocationSet.add(classLocation);
        TestCase testCase = new TestCase("signature", 1, 1, "simpleName", "className", null);
        coverage.formMutationRanges(classLocationSet, testCase);
        CoverageFilter filter = new CoverageFilter(coverage);
        ProjectParserFilter.setup(filter);
        JavaComment jComment = new JavaComment("comment", 1, 1, SAMPLE_FILE_PATH);
        Request request = new Request();
        filter.handle(jComment, request);
        assertTrue(request.wasSuccessful());
    }
}
