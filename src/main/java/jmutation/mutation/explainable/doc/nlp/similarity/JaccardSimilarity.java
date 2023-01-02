package jmutation.mutation.explainable.doc.nlp.similarity;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.Pair;
import jmutation.mutation.explainable.doc.nlp.annotator.StopwordAnnotator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Pattern;

public class JaccardSimilarity extends SimilarityCalculator {

    @Override
    public float calculate(String text, String otherText) {
        BagOfWords bag0 = new BagOfWords(text);
        BagOfWords bag1 = new BagOfWords(otherText);
        return bag0.jaccardSimilarity(bag1);
    }

    private class BagOfWords {
        public final Map<String, Integer> wordAndCounts = new HashMap<>();
        private final Pattern alphaNumericPattern = Pattern.compile("^[a-zA-Z0-9]+$");

        public BagOfWords(String text) {
            // Tokenize, then lemmatize. Filter out the stop words.
            Properties props = new Properties();
            props.setProperty("customAnnotatorClass.stopword",
                    "jmutation.mutation.explainable.doc.nlp.annotator.StopwordAnnotator");
            props.setProperty("annotators", "tokenize, pos, lemma, ssplit, stopword");
            StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
            CoreDocument document = new CoreDocument(text);
            pipeline.annotate(document);
            List<CoreLabel> tokens = document.tokens();
            for (CoreLabel token : tokens) {
                Pair<Boolean, Boolean> stopword = token.get(StopwordAnnotator.class);
                String lemma = token.lemma();
                if (Boolean.TRUE.equals(stopword.first()) || !alphaNumericPattern.matcher(lemma).matches()) {
                    continue;
                }
                int count = wordAndCounts.containsKey(lemma) ? wordAndCounts.get(lemma) : 0;
                wordAndCounts.put(lemma, count + 1);
            }
        }

        // TODO: Extract out into a utility, and use it in the jaccard matcher as well
        float jaccardSimilarity(BagOfWords otherBOW) {
            int totalCount = 0;
            Set<String> keySet = wordAndCounts.keySet();
            for (String key : keySet) {
                totalCount += wordAndCounts.get(key);
            }
            keySet = otherBOW.wordAndCounts.keySet();
            for (String key : keySet) {
                totalCount += otherBOW.wordAndCounts.get(key);
            }
            return (float) intersectCount(otherBOW) / (float) totalCount;
        }

        int intersectCount(BagOfWords otherBOW) {
            Set<String> keySet = wordAndCounts.keySet();
            keySet.retainAll(otherBOW.wordAndCounts.keySet());
            int result = 0;
            for (String key : keySet) {
                result += wordAndCounts.get(key);
                result += otherBOW.wordAndCounts.get(key);
            }
            return result;
        }
    }
}
