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
}
