package jmutation.mutation.explainable.doc.code.matcher;


import jmutation.mutation.explainable.doc.model.JavaComment;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.IfStatement;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
        JaccardCalculator jaccardCalculator = new JaccardCalculator();
        double[] similarity = jaccardCalculator.calculate(ifStatement);
        double currentSimilarity = similarity[0] / similarity[1];
        if (largestSimilarity < currentSimilarity) {
            largestSimilarity = currentSimilarity;
            resultNode = ifStatement;
        }
        return true;
    }

    private class JaccardCalculator {
        private int intersectionCount = 0;

        private double[] calculate(ASTNode node) {
            String nodeStr = node.toString();
            String[] nodeWords = nodeStr.split("[^A-Za-z0-9]+");
            for (String nodeWord : nodeWords) {
                for (String commentWord : wordsInComment) {
                    if (nodeWord.equals(commentWord)) {
                        intersectionCount++;
                    }
                }
            }
            Set<String> setOfWords = new HashSet<>();
            setOfWords.addAll(List.of(nodeWords));
            setOfWords.addAll(List.of(wordsInComment));
            return new double[]{intersectionCount, setOfWords.size()};
        }

    }
}
