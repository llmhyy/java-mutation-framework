package jmutation.mutation.explainable.doc.code.matcher;

import jmutation.mutation.MutationTestHelper;
import jmutation.mutation.explainable.doc.model.CodeChunk;
import jmutation.mutation.explainable.doc.model.JavaComment;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static jmutation.mutation.explainable.doc.ExplainableMutationTestConstants.CODE_SAMPLE_JAVA_FILE_PATH;
import static org.junit.jupiter.api.Assertions.assertEquals;

class GPT3MatcherTest {
    MutationTestHelper helper = new MutationTestHelper();

    @Test
    // Disabled in GitHub actions, as it requires a GPT3 API key
    @DisabledIf("jmutation.utils.MiscellaneousTestUtils#isRunningInGitHubActions")
    void match_cornerCaseCommentAndCodeProvided_matchesCorrectly() throws IOException {
        String documentStr = Files.readString(Path.of(CODE_SAMPLE_JAVA_FILE_PATH));
        helper.parseDocStr(documentStr);
        MethodDeclaration methodDeclaration = (MethodDeclaration) helper.getBodyDeclarations().get(0);
        IfStatement ifStatement = (IfStatement) methodDeclaration.getBody().statements().get(1);
        CodeChunk expectedCodeChunk = new CodeChunk(CODE_SAMPLE_JAVA_FILE_PATH,
                ifStatement);
        GPT3Matcher matcher = new GPT3Matcher();
        JavaComment javaComment = new JavaComment("", 0, 0,
                CODE_SAMPLE_JAVA_FILE_PATH);
        CodeChunk actualCodeChunk = matcher.match("     * if a is 0, it will throw a Runtime exception",
                javaComment, CODE_SAMPLE_JAVA_FILE_PATH);
        assertEquals(expectedCodeChunk, actualCodeChunk);
    }
}