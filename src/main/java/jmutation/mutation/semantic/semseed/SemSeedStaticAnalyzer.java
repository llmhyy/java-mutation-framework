package jmutation.mutation.semantic.semseed;

import jmutation.execution.Coverage;
import jmutation.model.MutationRange;
import jmutation.mutation.semantic.semseed.model.StaticAnalysisResult;
import jmutation.parser.ProjectParser;
import org.apache.commons.io.FileUtils;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.StringLiteral;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Obtains tokens from a project.
 * It visits each java file from the root, and runs static analysis on each of them.
 */
public class SemSeedStaticAnalyzer {
    private final Coverage coverage;
    private final File root;
    private final int topOccurring;


    public SemSeedStaticAnalyzer(Coverage coverage, File root, int topOccurring) {
        this.coverage = coverage;
        this.root = root;
        this.topOccurring = topOccurring;
    }

    public StaticAnalysisResult analyse() {
        final Map<String, Integer> identifierCounts = new HashMap<>();
        final Map<String, Integer> literalCounts = new HashMap<>();
        final Map<String, Set<String>> identifiersByFiles = new HashMap<>();
        final Map<String, Set<String>> literalsByFiles = new HashMap<>();
        Set<String> classSet = getCoveredFilesSet(coverage);
        Iterator<File> javaFiles = FileUtils.iterateFiles(root, new String[]{"java"}, true);
        while (javaFiles.hasNext()) {
            File javaFile = javaFiles.next();
            CompilationUnit compilationUnit;
            try {
                compilationUnit = ProjectParser.parseCompilationUnit(Files.readString(javaFile.toPath()));
            } catch (IOException e) {
                // TODO: don't return null
                return null;
            }
            SemSeedStaticAnalyzerVisitor visitor = new SemSeedStaticAnalyzerVisitor();
            compilationUnit.accept(visitor);
            updateTotalCount(identifierCounts, visitor.identifierCounts);
            updateTotalCount(literalCounts, visitor.literalCounts);
            String className = getClassNameFromFileName(javaFile.getName());
            if (!classSet.contains(className)) continue;
            if (identifierCounts.keySet().size() != 0) identifiersByFiles.put(className, identifierCounts.keySet());
            if (literalCounts.keySet().size() != 0) literalsByFiles.put(className, literalCounts.keySet());
        }
        return new StaticAnalysisResult(getTopOccurringCounts(identifierCounts, topOccurring),
                getTopOccurringCounts(literalCounts, topOccurring), identifiersByFiles, literalsByFiles);
    }

    private void updateTotalCount(Map<String, Integer> currentCounts, Map<String, Integer> collectedCount) {
        collectedCount.forEach((name, count) -> {
            if (currentCounts.containsKey(name)) {
                int currentCount = currentCounts.get(name);
                currentCounts.put(name, currentCount + count);
            } else {
                currentCounts.put(name, count);
            }
        });
    }

    private Map<String, Integer> getTopOccurringCounts(Map<String, Integer> currentCounts, int topOccurring) {
        List<Entry<String, Integer>> list =
                new ArrayList<>(currentCounts.entrySet());

        // Sort the list
        Collections.sort(list, Comparator.comparingInt((Entry<String, Integer> entry) -> entry.getValue()).reversed());

        // put data from sorted list to hashmap
        HashMap<String, Integer> result = new LinkedHashMap<>();
        for (int i = 0; i < list.size() && i < topOccurring; i++) {
            Entry<String, Integer> currentEntry = list.get(i);
            result.put(currentEntry.getKey(), currentEntry.getValue());
        }
        return result;
    }

    private Set<String> getCoveredFilesSet(Coverage coverage) {
        Map<String, List<MutationRange>> rangesByClass = coverage.getRangesByClass();
        return rangesByClass.keySet();
    }

    private String getClassNameFromFileName(String fileName) {
        return fileName.substring(0, fileName.indexOf(".java"));
    }

    /**
     * Token collector for each file (Compilation Unit)
     */
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
