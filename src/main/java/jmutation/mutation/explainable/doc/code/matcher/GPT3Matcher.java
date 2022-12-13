package jmutation.mutation.explainable.doc.code.matcher;

import com.theokanning.openai.OpenAiService;
import com.theokanning.openai.completion.CompletionRequest;
import jmutation.model.ast.ASTNodeRetriever;
import jmutation.mutation.explainable.doc.model.CodeChunk;
import jmutation.mutation.explainable.doc.model.JavaComment;
import jmutation.parser.ProjectParser;
import jmutation.utils.MiscellaneousUtils;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IfStatement;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class GPT3Matcher extends CommentCodeMatcher {
    @Override
    public CodeChunk match(String focusedComment, JavaComment comment, String targetFilePath) {
        // Request to gpt 3 with contents of targetFilePath
        // (for now if statements? How to determine the code chunks to check, it will likely change in the future)
        // Calculate similarity of each code chunk
        // return the code chunk with highest similarity
        List<? extends ASTNode> astNodesToAnalyse = retrieveASTNodes(targetFilePath);
        double maxSim = -Double.MAX_VALUE;
        CodeChunk mostSimilarCodeChunk = null;
        for (ASTNode astNode : astNodesToAnalyse) {
            CodeChunk currCodeChunk = new CodeChunk(targetFilePath, astNode);
            String generatedDoc = getDocForCodeChunk(currCodeChunk);
            double currSim = calculateSim(focusedComment, generatedDoc);
            if (currSim > maxSim) {
                maxSim = currSim;
                mostSimilarCodeChunk = currCodeChunk;
            }
        }
        return mostSimilarCodeChunk;
    }

    private List<? extends ASTNode> retrieveASTNodes(String filePath) {
        String source = "";
        try {
            source = Files.readString(Path.of(filePath));
        } catch (IOException e) {
            e.printStackTrace();
        }
        CompilationUnit unit = ProjectParser.parseCompilationUnit(source);
        ASTNodeRetriever<IfStatement> ifStatementASTNodeRetriever = new ASTNodeRetriever<>(IfStatement.class);
        unit.accept(ifStatementASTNodeRetriever);
        return ifStatementASTNodeRetriever.getNodes();
    }

    private String getDocForCodeChunk(CodeChunk codeChunk) {
        // create prompt
        // send req to gpt3
        // parse the response, and obtain the comment
        // return it as string
        String message = "Generate JavaDoc for this code";
        String codePrefix = "Code: ";
        String docPrefix = "Documentation: ";
        String prompt = String.join(System.lineSeparator(), message, codePrefix, codeChunk.getNode().toString(), docPrefix);
        OpenAiService service = new OpenAiService(MiscellaneousUtils.loadEnv().get("GPT3-API-KEY"));
        CompletionRequest completionRequest = CompletionRequest.builder()
                .prompt(prompt)
                .model("text-davinci-001")
                .maxTokens(1000)
                .build();
        return service.createCompletion(completionRequest).getChoices().get(0).getText();
    }

    private double calculateSim(String originalComment, String generatedComment) {
        String[] wordsInOriginalComment = SemanticCalculatorUtils.splitAndObtainOnlyAlphaNum(originalComment.toLowerCase());
        String[] wordsInGeneratedComment = SemanticCalculatorUtils.splitAndObtainOnlyAlphaNum(generatedComment.toLowerCase());
        int[] result = SemanticCalculatorUtils.jaccardSimilarity(wordsInGeneratedComment, wordsInOriginalComment);
        return (double) result[0] / (double) result[1];
    }
}
