package jmutation.mutation.explainable.doc.nlp.similarity;

public abstract class SimilarityCalculator {

    protected SimilarityCalculator wrappee;

    public SimilarityCalculator(SimilarityCalculator wrappee) {
        this.wrappee = wrappee;
    }

    public SimilarityCalculator() {
        this.wrappee = new BaseCalculator();
    }

    public abstract float calculate(String text, String otherText);

    public int getDepth() {
        return 1 + wrappee.getDepth();
    }

    public float getAverage(String text, String otherText) {
        int depth = getDepth();
        float total = total(text, otherText);
        return total / depth;
    }

    protected float total(String text, String otherText) {
        return calculate(text, otherText) + wrappee.total(text, otherText);
    }
}
