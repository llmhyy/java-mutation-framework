package jmutation.mutation.explainable.doc.nlp.similarity;

public class BaseCalculator extends SimilarityCalculator {
    public BaseCalculator() {
        super(null);
    }

    public BaseCalculator(SimilarityCalculator wrappee) {
        throw new IllegalArgumentException("Base should not have a wrappee");
    }

    @Override
    public float calculate(String text, String otherText) {
        return 0;
    }

    @Override
    public int getDepth() {
        return 0;
    }

    @Override
    public float total(String text, String otherText) {
        return 0;
    }
}
