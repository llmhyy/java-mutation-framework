package jmutation.mutation.semantic.semseed;

import com.github.jfasttext.JFastText;
import jmutation.mutation.semantic.semseed.model.Pattern;
import jmutation.mutation.semantic.semseed.model.TokenSequence;
import org.gradle.internal.Pair;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static jmutation.constants.ResourcesPath.DEFAULT_RESOURCES_PATH;
import static jmutation.constants.ResourcesPath.DEFAULT_SEMSEED_DIR;
import static jmutation.constants.ResourcesPath.DEFAULT_SEMSEED_MODEL;

public class FastTextWrapper {

    JFastText jft;
    private float threshold = 0.4f;


    public FastTextWrapper() {
        jft = new JFastText();
        jft.loadModel(String.join(File.separator, DEFAULT_RESOURCES_PATH, DEFAULT_SEMSEED_DIR, DEFAULT_SEMSEED_MODEL));
    }

    public FastTextWrapper(String pathToModel) {
        jft = new JFastText();
        jft.loadModel(pathToModel);
    }

    public FastTextWrapper(String pathToModel, float threshold) {
        jft = new JFastText();
        jft.loadModel(pathToModel);
        this.threshold = threshold;
    }

    public FastTextWrapper(JFastText jft) {
        this.jft = jft;
    }

    private static double cosineDistance(List<Float> vectorA, List<Float> vectorB) {
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
            sumOfSimilarities += cosineDistance(targetTokenEmb, fixTokenEmb);
        }
        return sumOfSimilarities / lengthOfSeq > threshold;
    }

    public List<String> getReplacementTokens(String buggyConcreteToken, Pattern pattern, TokenSequence targetTokenSeq,
                                             List<String> candidates) {
        List<String> result = new ArrayList<>();
        List<String> targetConcrete = targetTokenSeq.getConcreteTokens();
        List<String> correctConcrete = pattern.getFixConcrete();
        List<List<Float>> analogousTokenEmbeddings = new ArrayList<>();
        List<Float> buggyConcreteEmb = jft.getVector(buggyConcreteToken);
        for (int i = 0; i < targetConcrete.size(); i++) {
            List<Float> targetTokenEmb = jft.getVector(targetConcrete.get(i));
            List<Float> correctTokenEmb = jft.getVector(correctConcrete.get(i));
            List<Float> analogousEmbedding = addOrSubtractVec(buggyConcreteEmb, targetTokenEmb, true);
            analogousEmbedding = addOrSubtractVec(analogousEmbedding, correctTokenEmb, false);
            analogousTokenEmbeddings.add(analogousEmbedding);
        }
        List<Float> sumOfAllVectors = sumOfAllVec(analogousTokenEmbeddings);
        int numOfAnalogousEmb = analogousTokenEmbeddings.size();
        List<Float> avgOfAllAnalogousEmb = new ArrayList<>();
        for (Float vectorVal : sumOfAllVectors) {
            avgOfAllAnalogousEmb.add(vectorVal / numOfAnalogousEmb);
        }
        // Calculate similarity score for each candidate.
        // Get candidates.
        // Get similarity list, each index contains a score for some candidate.
        // Choose the one with highest sim
        List<Pair<Integer, Double>> similaritiesWithIdx = new ArrayList<>();
        List<Float> candidateVector;
        for (int i = 0; i < candidates.size(); i++) {
            String candidate = candidates.get(i);
            candidateVector = jft.getVector(candidate);
            similaritiesWithIdx.add(Pair.of(i, 1 - cosineDistance(avgOfAllAnalogousEmb, candidateVector)));
        }
        Collections.sort(similaritiesWithIdx, (a, b) -> {
            if (a.right.equals(b.right)) return 0;
            if (a.right < b.right) return 1;
            return -1;
        });
        for (Pair<Integer, Double> idxToSimPair : similaritiesWithIdx) {
            result.add(candidates.get(idxToSimPair.left));
        }
        return result;
    }

    private List<Float> addOrSubtractVec(List<Float> ls0, List<Float> ls1, boolean isAddition) {
        assert (ls0.size() == ls1.size());
        List<Float> result = new ArrayList<>();
        for (int i = 0; i < ls0.size(); i++) {
            float firstFloat = ls0.get(i);
            float secondFloat = ls1.get(i);
            result.add(isAddition ? firstFloat + secondFloat : firstFloat - secondFloat);
        }
        return result;
    }

    private List<Float> sumOfAllVec(List<List<Float>> vectors) {
        List<Float> result = vectors.get(0);
        for (int i = 1; i < vectors.size(); i++) {
            result = addOrSubtractVec(result, vectors.get(i), true);
        }
        return result;
    }

}
