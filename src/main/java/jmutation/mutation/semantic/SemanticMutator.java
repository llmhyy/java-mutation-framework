package jmutation.mutation.semantic;

import jmutation.execution.Coverage;
import jmutation.execution.ProjectExecutor;
import jmutation.model.MutationRange;
import jmutation.model.project.Project;
import jmutation.mutation.MutationASTNodeRetriever;
import jmutation.mutation.Mutator;
import jmutation.mutation.semantic.semseed.FastTextWrapper;
import jmutation.mutation.semantic.semseed.SemSeedMutationCommand;
import jmutation.mutation.semantic.semseed.SemSeedStaticAnalyzer;
import jmutation.mutation.semantic.semseed.io.PatternIO;
import jmutation.mutation.semantic.semseed.io.handler.FileHandler;
import jmutation.mutation.semantic.semseed.mining.TokenSequenceCreator;
import jmutation.mutation.semantic.semseed.model.Pattern;
import jmutation.mutation.semantic.semseed.model.StaticAnalysisResult;
import jmutation.mutation.semantic.semseed.model.TokenSequence;
import jmutation.parser.ProjectParser;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SemanticMutator extends Mutator {
    private String patternFilePath;

    private FastTextWrapper fastTextWrapper;
    private ProjectExecutor projectExecutor;

    public SemanticMutator(String patternFilePath, String modelPath, ProjectExecutor executor) {
        this.patternFilePath = patternFilePath;
        this.fastTextWrapper = new FastTextWrapper(modelPath);
        this.projectExecutor = executor;
    }

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
            mutationASTNodeRetriever.setRandomness(false);
            unit.accept(mutationASTNodeRetriever);
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
        List<ASTNode> possibleASTNodes = getPossibleASTNodes(coverage, project, patterns);
        List<TokenSequence> tokenSequencesOfNodes = getTokenSequences(possibleASTNodes);
        List<TokenSequence> possibleTokenSequences = getMatchingTokenSequences(tokenSequencesOfNodes, patterns);
        StaticAnalysisResult staticAnalysisResult = staticAnalyzer.analyse();
        for (TokenSequence tokenSequence : possibleTokenSequences) {
            for (Pattern pattern : patterns) {
                SemSeedMutationCommand mutationCommand = new SemSeedMutationCommand(tokenSequence.getNode(),
                        staticAnalysisResult, pattern, tokenSequence);
                while (mutationCommand.hasAnotherSeq()) {
                    mutationCommand.executeMutation();
                    //writeToFile
                    if (compileProject()) {
                        return project;
                    }
                }
            }
        }
        System.out.println("No semantic mutations found");
        return project;
    }

    private List<Pattern> readPatternsFromFile(String filePath) {
        FileHandler fileHandler = new FileHandler();
        fileHandler.setSource(filePath);
        PatternIO patternIO = new PatternIO(fileHandler);
        return patternIO.readAll();
    }

    /**
     * Get all target ASTNodes that have the same class type as those in fixed patterns
     *
     * @param coverage
     * @param project
     * @param bugFixPatterns
     * @return
     */
    public List<ASTNode> getPossibleASTNodes(Coverage coverage, Project project, List<Pattern> bugFixPatterns) {
        List<ASTNode> result = new ArrayList<>();
        Set<Class<? extends ASTNode>> astNodeClassesSet = new HashSet<>();
        for (Pattern pattern : bugFixPatterns) {
            astNodeClassesSet.add(pattern.getFixASTNodeClass());
        }
        for (Class<? extends ASTNode> astNodeClass : astNodeClassesSet) {
            result.addAll(getNodesOfSomeType(coverage, project, astNodeClass));
        }
        return result;
    }

    public List<TokenSequence> getTokenSequences(List<ASTNode> nodes) {
        List<TokenSequence> result = new ArrayList<>();
        TokenSequenceCreator tokenSequenceCreator = new TokenSequenceCreator();
        for (ASTNode node : nodes) {
            node.accept(tokenSequenceCreator);
            result.add(tokenSequenceCreator.getTokenSequence());
            tokenSequenceCreator.reset();
        }
        return result;
    }

    public List<TokenSequence> getMatchingTokenSequences(List<TokenSequence> targetTokenSequences, List<Pattern> bugFixPatterns) {
        // For each pattern, get all ASTNodes of its type, and find syntax match with its fix pattern.
        List<TokenSequence> result = new ArrayList<>();
        Map<Class<? extends ASTNode>, List<ASTNode>> classToASTNodeMap = new HashMap<>();
        for (Pattern pattern : bugFixPatterns) {
            for (TokenSequence targetTokenSeq : targetTokenSequences) {
                if (!targetTokenSeq.getAbstractTokens().equals(pattern.getFixPattern())) continue;
                if (!fastTextWrapper.isSemanticallyMatching(targetTokenSeq.getConcreteTokens(),
                        pattern.getFixConcrete())) continue;
                result.add(targetTokenSeq);
            }
        }
        return result;
    }

    public boolean compileProject() {
        String output = projectExecutor.compile();
        return !output.contains("FAIL");
    }
}
