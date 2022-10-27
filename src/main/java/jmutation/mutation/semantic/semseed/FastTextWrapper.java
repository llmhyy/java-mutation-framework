package jmutation.mutation.semantic.semseed;

import com.github.jfasttext.JFastText;

import java.util.List;

public class FastTextWrapper {

    JFastText jft;
    private float threshold;


    public FastTextWrapper(String pathToModel) {
        jft = new JFastText();
        jft.loadModel(pathToModel);
        threshold = 0.4f;
    }

    public FastTextWrapper(String pathToModel, float threshold) {
        jft = new JFastText();
        jft.loadModel(pathToModel);
        this.threshold = threshold;
    }

    private static double cosineSimilarity(List<Float> vectorA, List<Float> vectorB) {
        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;
        for (int i = 0; i < vectorA.size(); i++) {
            dotProduct += vectorA.get(i) * vectorB.get(i);
            normA += Math.pow(vectorA.get(i), 2);
            normB += Math.pow(vectorB.get(i), 2);
        }
        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    public boolean isSemanticallyMatching(List<String> targetConcreteTokens, List<String> fixConcreteTokens) {
        assert (targetConcreteTokens.size() == fixConcreteTokens.size());
        int lengthOfSeq = targetConcreteTokens.size();
        float sumOfSimilarities = 0;
        for (int i = 0; i < targetConcreteTokens.size(); i++) {
            String targetToken = targetConcreteTokens.get(i);
            String fixToken = fixConcreteTokens.get(i);
            List<Float> targetTokenEmb = jft.getVector(targetToken);
            List<Float> fixTokenEmb = jft.getVector(fixToken);
            sumOfSimilarities += cosineSimilarity(targetTokenEmb, fixTokenEmb);
        }
        return sumOfSimilarities / lengthOfSeq > threshold;
    }
}
