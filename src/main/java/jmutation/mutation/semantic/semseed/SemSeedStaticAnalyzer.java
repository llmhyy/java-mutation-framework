package jmutation.mutation.semantic.semseed;

import jmutation.execution.Coverage;
import jmutation.mutation.semantic.semseed.model.StaticAnalysisResult;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.StringLiteral;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class SemSeedStaticAnalyzer {
    private final Coverage coverage;
    private final File root;

    private final Map<String, Integer> identifierCounts = new HashMap<>();
    private final Map<String, Integer> literalCounts = new HashMap<>();

    private final Map<String, Set<String>> identifiersByFiles = new HashMap<>();
    private final Map<String, Set<String>> literalsByFiles = new HashMap<>();

    public SemSeedStaticAnalyzer(Coverage coverage, File root) {
        this.coverage = coverage;
        this.root = root;
    }

    public StaticAnalysisResult analyse() {
        return null;
    }

    private class SemSeedStaticAnalyzerVisitor extends ASTVisitor {
        private final Map<String, Integer> identifierCounts = new HashMap<>();
        private final Map<String, Integer> literalCounts = new HashMap<>();

        @Override
        public boolean visit(SimpleName simpleName) {
            addToMap(true, simpleName.toString());
            return true;
        }

        @Override
        public boolean visit(StringLiteral stringLiteral) {
            addToMap(false, stringLiteral.toString());
            return true;
        }

        private void addToMap(boolean isIdentifier, String string) {
            Map<String, Integer> mapToAddTo;
            if (isIdentifier) {
                mapToAddTo = identifierCounts;
            } else {
                mapToAddTo = literalCounts;
            }
            if (mapToAddTo.containsKey(string)) {
                int count = mapToAddTo.get(string);
                mapToAddTo.put(string, count + 1);
                return;
            }
            mapToAddTo.put(string, 1);
        }
    }
}
