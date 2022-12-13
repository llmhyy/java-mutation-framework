package jmutation.mutation.explainable.doc.code.matcher;


import jmutation.mutation.explainable.doc.model.JavaComment;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.IfStatement;

public class JaccardSemanticSimilarityCalculator extends SemanticSimilarityCalculator {
    private final String[] wordsInComment;
    private double largestSimilarity = -Double.MAX_VALUE;


    public JaccardSemanticSimilarityCalculator(String commentStr, JavaComment javaComment, ASTNode rootNode) {
        super(commentStr, javaComment, rootNode);
        wordsInComment = commentStr.split(" ");
    }

    @Override
    public ASTNode start() {
        rootNode.accept(this);
        return resultNode;
    }

    @Override
    public boolean visit(IfStatement ifStatement) {
        double[] similarity = calculate(ifStatement);
        double currentSimilarity = similarity[0] / similarity[1];
        if (largestSimilarity < currentSimilarity) {
            largestSimilarity = currentSimilarity;
            resultNode = ifStatement;
        }
        return true;
    }

    private double[] calculate(ASTNode node) {
        String nodeStr = node.toString().toLowerCase();
        String[] nodeWords = SemanticCalculatorUtils.splitAndObtainOnlyAlphaNum(nodeStr);
        int[] result = SemanticCalculatorUtils.jaccardSimilarity(nodeWords, wordsInComment);
        return new double[]{(double) result[0], result[1]};
    }
}
