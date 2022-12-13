package jmutation.mutation.explainable.doc.code.matcher;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SemanticCalculatorUtils {
    public static int[] jaccardSimilarity(String[] arrOfWords, String[] arrOfWords1) {
        int intersectionCount = 0;
        Set<String> setOfWords = new HashSet<>();
        setOfWords.addAll(List.of(arrOfWords));
        Set<String> setOfWords1 = new HashSet<>();
        setOfWords1.addAll(List.of(arrOfWords1));
        for (String word : setOfWords) {
            for (String word1 : setOfWords1) {
                if (word.equals(word1)) {
                    intersectionCount++;
                }
            }
        }
        setOfWords.addAll(setOfWords1);
        return new int[]{intersectionCount, setOfWords.size()};
    }

    public static String[] splitAndObtainOnlyAlphaNum(String str) {
        return str.split("[^A-Za-z0-9]+");
    }
}
