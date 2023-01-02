package jmutation.mutation.explainable.doc.nlp.similarity;

import org.junit.jupiter.api.Test;

import static junit.framework.Assert.assertEquals;

class JaccardSimilarityTest {

    @Test
    void calculate_twoTexts_correctSimilarity() {
        String text0 = "index - position at which to insert subarray.";
        String text1 = "offset - the index of the first char in subarray to be inserted.";
        JaccardSimilarity similarityCalculator = new JaccardSimilarity();
        float actualSim = similarityCalculator.calculate(text0, text1);
        float expectedSim = (float) 6 / 11;
        assertEquals(expectedSim, actualSim);
    }
}