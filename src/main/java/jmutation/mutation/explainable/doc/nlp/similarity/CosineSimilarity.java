package jmutation.mutation.explainable.doc.nlp.similarity;

import java.util.ArrayList;
import java.util.List;

public class CosineSimilarity extends SimilarityCalculator {
    @Override
    public float calculate(String text, String otherText) {
        return 0;
    }

    private List<Float> getWordEmbedding(String word) {
        return new ArrayList<>();
    }

    private List<Float> getAverageEmbedding(List<List<Float>> wordEmbeddings) {
        return new ArrayList<>();
    }
}
