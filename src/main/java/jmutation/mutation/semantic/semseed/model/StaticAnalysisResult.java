package jmutation.mutation.semantic.semseed.model;

import java.util.Map;
import java.util.Set;

public class StaticAnalysisResult {
    private final Map<String, Integer> topOccurringIdentifiers;
    private final Map<String, Integer> topOccurringLiterals;
    private final Map<String, Set<String>> identifiersByFiles;
    private final Map<String, Set<String>> literalsByFiles;

    public StaticAnalysisResult(Map<String, Integer> topOccurringIdentifiers, Map<String, Integer> topOccurringLiterals, Map<String, Set<String>> identifiersByFiles, Map<String, Set<String>> literalsByFiles) {
        this.topOccurringIdentifiers = topOccurringIdentifiers;
        this.topOccurringLiterals = topOccurringLiterals;
        this.identifiersByFiles = identifiersByFiles;
        this.literalsByFiles = literalsByFiles;
    }

    public Map<String, Integer> getTopOccurringIdentifiers() {
        return topOccurringIdentifiers;
    }

    public Map<String, Integer> getTopOccurringLiterals() {
        return topOccurringLiterals;
    }

    public Map<String, Set<String>> getIdentifiersByFiles() {
        return identifiersByFiles;
    }

    public Map<String, Set<String>> getLiteralsByFiles() {
        return literalsByFiles;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof StaticAnalysisResult)) return false;
        StaticAnalysisResult otherResult = (StaticAnalysisResult) other;
        return topOccurringIdentifiers.equals(otherResult.topOccurringIdentifiers) &&
                topOccurringLiterals.equals(otherResult.topOccurringLiterals) &&
                identifiersByFiles.equals(otherResult.identifiersByFiles) &&
                literalsByFiles.equals(otherResult.literalsByFiles);
    }
}
