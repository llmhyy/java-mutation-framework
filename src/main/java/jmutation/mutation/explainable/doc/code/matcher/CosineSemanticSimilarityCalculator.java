package jmutation.mutation.explainable.doc.code.matcher;


import jmutation.mutation.explainable.doc.model.JavaComment;
import jmutation.mutation.semantic.semseed.FastTextWrapper;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.IfStatement;

import java.util.List;

public class CosineSemanticSimilarityCalculator extends SemanticSimilarityCalculator {
    private final FastTextWrapper fastTextWrapper;
    private final List<Float> commentVector;
    private double lowestDiff;

    public CosineSemanticSimilarityCalculator(String commentStr, JavaComment javaComment, ASTNode rootNode) {
        super(commentStr, javaComment, rootNode);
        fastTextWrapper = new FastTextWrapper("C:\\Users\\bchenghi\\Desktop\\all_token_embedding_FAST_TEXT.bin");
        commentVector = fastTextWrapper.getVector(commentStr);
        resultNode = rootNode;
        lowestDiff = Double.MAX_VALUE;
    }

    @Override
    public ASTNode start() {
        rootNode.accept(this);
        return resultNode;
    }

    @Override
    public boolean visit(IfStatement node) {
        String codeChunk = node.toString();
        List<Float> codeChunkVec = obtainVector(codeChunk);
        double difference = getDifference(commentVector, codeChunkVec);
        if (difference < lowestDiff) {
            lowestDiff = difference;
            resultNode = node;
        }
        return true;
    }

    private List<Float> obtainVector(String str) {
        return fastTextWrapper.getVector(str);
    }

    private double getDifference(List<Float> v0, List<Float> v1) {
        return FastTextWrapper.cosineDistance(v0, v1);
    }
}
