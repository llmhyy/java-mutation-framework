package jmutation.mutation.semantic.semseed;

import jmutation.execution.Coverage;
import jmutation.model.MutationRange;
import jmutation.model.ast.ASTNodeRetriever;
import jmutation.mutation.semantic.semseed.model.StaticAnalysisResult;
import jmutation.parser.ProjectParser;
import org.apache.commons.io.FileUtils;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Stack;

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
        final Map<String, Set<String>> identifiersByMethods = new HashMap<>();
        final Map<String, Set<String>> literalsByMethods = new HashMap<>();
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
            ASTNodeRetriever<TypeDeclaration> typeDeclarationASTNodeRetriever = new ASTNodeRetriever<>(TypeDeclaration.class);
            compilationUnit.accept(typeDeclarationASTNodeRetriever);
            List<String> classNames = new ArrayList<>();
            for (TypeDeclaration typeDeclaration : typeDeclarationASTNodeRetriever.getNodes()) {
                classNames.add(typeDeclaration.getName().toString());
            }
            if (classSet.stream().noneMatch(classNames::contains)) continue;
            addTokensToMap(identifiersByFiles, visitor.identifierCounts.keySet(), javaFile.getAbsolutePath());
            addTokensToMap(literalsByFiles, visitor.literalCounts.keySet(), javaFile.getAbsolutePath());
            identifiersByMethods.putAll(visitor.identifiersByMethod);
            literalsByMethods.putAll(visitor.literalsByMethod);
        }
        return new StaticAnalysisResult(getTopOccurringCounts(identifierCounts, topOccurring),
                getTopOccurringCounts(literalCounts, topOccurring), identifiersByFiles, literalsByFiles,
                identifiersByMethods, literalsByMethods);
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

    private void addTokensToMap(Map<String, Set<String>> map, Set<String> setToAdd, String key) {
        if (setToAdd.isEmpty()) return;
        if (map.containsKey(key)) {
            map.get(key).addAll(setToAdd);
            return;
        }
        map.put(key, setToAdd);
    }

    /**
     * Token collector for each file (Compilation Unit)
     */
    private static class SemSeedStaticAnalyzerVisitor extends ASTVisitor {
        private final Map<String, Integer> identifierCounts = new HashMap<>();
        private final Map<String, Integer> literalCounts = new HashMap<>();

        private final Map<String, Set<String>> identifiersByMethod = new HashMap<>();
        private final Map<String, Set<String>> literalsByMethod = new HashMap<>();
        private final Stack<String> visitingMethodStack = new Stack<>();
        private final Stack<String> visitingClassNameStack = new Stack<>();

        @Override
        public boolean visit(TypeDeclaration typeDeclaration) {
            visitingClassNameStack.add(typeDeclaration.getName().toString());
            return true;
        }

        @Override
        public void endVisit(TypeDeclaration typeDeclaration) {
            visitingClassNameStack.pop();
        }

        @Override
        public boolean visit(MethodDeclaration methodDeclaration) {
            visitingMethodStack.add(visitingClassNameStack.peek() + "#" + methodDeclaration.getName().toString());
            return true;
        }

        @Override
        public void endVisit(MethodDeclaration methodDeclaration) {
            visitingMethodStack.pop();
        }

        @Override
        public boolean visit(SimpleName simpleName) {
            addToAllMaps(true, simpleName.toString());
            return true;
        }

        @Override
        public boolean visit(StringLiteral stringLiteral) {
            addToAllMaps(false, stringLiteral.toString());
            return true;
        }

        private void addToAllMaps(boolean isIdentifier, String string) {
            addToMap(isIdentifier, string);
            if (visitingMethodStack.isEmpty()) {
                return;
            }
            addToMethodMap(isIdentifier, string, visitingMethodStack.peek());
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

        private void addToMethodMap(boolean isIdentifier, String string, String methodName) {
            Map<String, Set<String>> mapToAddTo;
            if (isIdentifier) {
                mapToAddTo = identifiersByMethod;
            } else {
                mapToAddTo = literalsByMethod;
            }
            if (mapToAddTo.containsKey(methodName)) {
                mapToAddTo.get(methodName).add(string);
                return;
            }
            Set<String> newSet = new HashSet<>();
            newSet.add(string);
            mapToAddTo.put(methodName, newSet);
        }
    }
}
