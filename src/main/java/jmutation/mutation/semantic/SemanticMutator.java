package jmutation.mutation.semantic;

import jmutation.execution.Coverage;
import jmutation.model.MutationRange;
import jmutation.model.project.Project;
import jmutation.mutation.MutationASTNodeRetriever;
import jmutation.mutation.Mutator;
import jmutation.mutation.semantic.semseed.SemSeedStaticAnalyzer;
import jmutation.mutation.semantic.semseed.io.PatternIO;
import jmutation.mutation.semantic.semseed.io.handler.FileHandler;
import jmutation.mutation.semantic.semseed.mining.TokenSequenceCreator;
import jmutation.mutation.semantic.semseed.model.Pattern;
import jmutation.parser.ProjectParser;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SemanticMutator extends Mutator {
    private String patternFilePath;

    private List<ASTNode> getNodesOfSomeType(Coverage coverage, Project project, Class<? extends ASTNode> nodeType) {
        List<ASTNode> result = new ArrayList<>();
        List<MutationRange> mutationRanges = coverage.getRanges();
        for (MutationRange mutationRange : mutationRanges) {
            String className = mutationRange.getClassName();
            File fileForClass = retrieveFileFromClassName(className, project);
            String fileContent;
            try {
                fileContent = Files.readString(fileForClass.toPath());
            } catch (IOException e) {
                throw new RuntimeException("Could not read file at " + fileForClass.toPath());
            }
            CompilationUnit unit = ProjectParser.parseCompilationUnit(fileContent);
            MutationASTNodeRetriever mutationASTNodeRetriever = new MutationASTNodeRetriever(unit, mutationRange);
            List<ASTNode> retrievedNodes = mutationASTNodeRetriever.getNodes();
            for (ASTNode retrievedNode : retrievedNodes) {
                if (retrievedNode.getClass().equals(nodeType)) {
                    result.add(retrievedNode);
                }
            }
        }
        return result;
    }

    public Project mutate(Coverage coverage, Project project) {
        // Get all patterns from file
        // Do static analysis (top k id/lit in project + id/lit in each file), future work: Cache it in temp directory
        // Get ASTNodes that can be mutated using syntax matcher
        // Randomize list of nodes
        // Create mutation command based on first node (Must take in possible replacements in constructor?)
        // execute it
        // Recompile
        // If fail, redo with second ASTNode
        List<Pattern> patterns = readPatternsFromFile(patternFilePath);
        SemSeedStaticAnalyzer staticAnalyzer = new SemSeedStaticAnalyzer(coverage, project.getRoot(), 1);
        List<ASTNode> syntacticallyMatchingNodes = getSyntacticallyMatchingNodes(coverage, project, patterns);
        return null;
    }

    private List<Pattern> readPatternsFromFile(String filePath) {
        FileHandler fileHandler = new FileHandler();
        fileHandler.setSource(filePath);
        PatternIO patternIO = new PatternIO(fileHandler);
        return patternIO.readAll();
    }

    /**
     * Obtains all ASTNode that has a syntax match with the bug fix patterns
     *
     * @param coverage
     * @param project
     * @param bugFixPatterns
     * @return
     */
    private List<ASTNode> getSyntacticallyMatchingNodes(Coverage coverage, Project project, List<Pattern> bugFixPatterns) {
        // For each pattern, get all ASTNodes of its type, and find syntax match with its fix pattern.
        List<ASTNode> result = new ArrayList<>();
        Map<Class<? extends ASTNode>, List<ASTNode>> classToASTNodeMap = new HashMap<>();
        for (Pattern pattern : bugFixPatterns) {
            List<ASTNode> nodesOfSomeType;
            Class<? extends ASTNode> astNodeClass = pattern.getFixASTNodeClass();
            if (classToASTNodeMap.containsKey(astNodeClass)) {
                nodesOfSomeType = classToASTNodeMap.get(astNodeClass);
            } else {
                nodesOfSomeType = getNodesOfSomeType(coverage, project, pattern.getFixASTNodeClass());
            }
            for (ASTNode node : nodesOfSomeType) {
                if (nodeHasSyntaxMatchWithPattern(node, pattern.getFixPattern())) {
                    result.add(node);
                }
            }
        }
        return result;
    }

    /**
     * @param node
     * @param abstractFixPatterns
     * @return
     */
    private boolean nodeHasSyntaxMatchWithPattern(ASTNode node, List<String> abstractFixPatterns) {
        TokenSequenceCreator tokenSequenceCreator = new TokenSequenceCreator();
        node.accept(tokenSequenceCreator);
        List<String> abstractTokens = tokenSequenceCreator.getAbstractTokens();
        return abstractFixPatterns.equals(abstractTokens);
    }
}
