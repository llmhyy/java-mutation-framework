package jmutation.mutation.semantic.semseed.model;

import jmutation.mutation.semantic.semseed.constants.TokenScope;

import java.util.Map;
import java.util.Set;

public class StaticAnalysisResult {
    private final Map<String, Integer> topOccurringIdentifiers;
    private final Map<String, Integer> topOccurringLiterals;
    private final Map<String, Set<String>> identifiersByFiles;
    private final Map<String, Set<String>> literalsByFiles;

    private final Map<String, Set<String>> identifiersByMethods;
    private final Map<String, Set<String>> literalsByMethods;

    public StaticAnalysisResult(Map<String, Integer> topOccurringIdentifiers, Map<String, Integer> topOccurringLiterals,
                                Map<String, Set<String>> identifiersByFiles, Map<String, Set<String>> literalsByFiles,
                                Map<String, Set<String>> identifiersByMethods, Map<String, Set<String>> literalsByMethods) {
        this.topOccurringIdentifiers = topOccurringIdentifiers;
        this.topOccurringLiterals = topOccurringLiterals;
        this.identifiersByFiles = identifiersByFiles;
        this.literalsByFiles = literalsByFiles;
        this.identifiersByMethods = identifiersByMethods;
        this.literalsByMethods = literalsByMethods;
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

    public Set<String> getTokens(TokenScope scope, boolean isIdentifiers, String key) {
        switch (scope) {
            case SCOPE_FILE:
                return isIdentifiers ? identifiersByFiles.get(key) : literalsByFiles.get(key);
            case SCOPE_METHOD:
                return isIdentifiers ? identifiersByMethods.get(key) : literalsByMethods.get(key);
            case SCOPE_FILE_AND_TOP_K:
                Set<String> results = isIdentifiers ? identifiersByFiles.get(key) : literalsByFiles.get(key);
                Map<String, Integer> topOccurringMap = isIdentifiers ? topOccurringIdentifiers : topOccurringLiterals;
                results.addAll(topOccurringMap.keySet());
                return results;
            default:
                return null;
        }
    }
}
