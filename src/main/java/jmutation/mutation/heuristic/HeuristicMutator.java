package jmutation.mutation.heuristic;

import jmutation.execution.Coverage;
import jmutation.model.mutation.MutationRange;
import jmutation.model.project.Project;
import jmutation.mutation.AnalysisASTNodeRetriever;
import jmutation.mutation.MutationASTNodeRetriever;
import jmutation.mutation.MutationCommand;
import jmutation.mutation.Mutator;
import jmutation.mutation.heuristic.parser.MutationAnalysisParser;
import jmutation.mutation.heuristic.parser.MutationParser;
import jmutation.parser.ProjectParser;
import jmutation.utils.RandomSingleton;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Given an arbitrary project (maven or gradle) and a region, we shall mutate the region into a compilable project.
 *
 * @author Yun Lin
 */
public class HeuristicMutator extends Mutator {
    private final MutationParser mutationParser;
    private int numberOfMutations = 1;

    public HeuristicMutator() {
        super();
        mutationParser = new MutationParser();
    }

    public HeuristicMutator(MutationParser mutationParser) {
        super();
        this.mutationParser = mutationParser;
    }

    /**
     * if max number of mutations is 0 or less, there is no limit
     *
     * @param numberOfMutations Maximum number of mutations allowed
     */
    public void setMaxNumberOfMutations(int numberOfMutations) {
        this.numberOfMutations = numberOfMutations;
    }

    public Project mutate(Coverage coverage, Project project) {
        List<MutationRange> ranges = coverage.getRanges();
        boolean isRandomRetrieval = true;
        for (int i = 0; i < 2; i++) {
            ranges = RandomSingleton.getSingleton().shuffle(ranges);
            mutate(ranges, project, isRandomRetrieval);
            if (!mutationHistory.isEmpty()) {
                break;
            }
            isRandomRetrieval = false;
        }

        return project;
    }

    private void mutate(List<MutationRange> ranges, Project project, boolean isRandomRetrieval) {
        int numberOfExecutedMutations = 0;
        Map<String, List<MutationRange>> classToRange = new LinkedHashMap<>();
        for (MutationRange range : ranges) {
            String className = range.getClassName();
            List<MutationRange> rangesForClass;
            if (classToRange.containsKey(className)) {
                rangesForClass = classToRange.get(className);
            } else {
                rangesForClass = new ArrayList<>();
            }
            rangesForClass.add(range);
            classToRange.put(className, rangesForClass);
        }
        for (Entry<String, List<MutationRange>> entry : classToRange.entrySet()) {
            List<MutationRange> rangesForClass = entry.getValue();
            String className = entry.getKey();
            File file = retrieveFileFromClassName(className, project);
            String fileContent;
            try {
                fileContent = Files.readString(file.toPath());
            } catch (IOException e) {
                throw new RuntimeException("Could not read file at " + file.toPath());
            }

            CompilationUnit unit = ProjectParser.parseCompilationUnit(fileContent);

            unit.recordModifications();
            for (MutationRange range : rangesForClass) {
                // Attempt random retrieval.
                List<ASTNode> nodes = parseRangeToNodes(unit, range, isRandomRetrieval);
                if (nodes.isEmpty()) {
                    // If mutation for node types in mutation range not implemented, skip to next mutation range
                    continue;
                }
                List<MutationCommand> newMutationCommands = new ArrayList<>();
                for (ASTNode node : nodes) {
                    MutationCommand mutationCommand = mutationParser.parse(node);
                    if (mutationCommand == null) {
                        continue;
                    }
                    newMutationCommands.add(mutationCommand);
                }

                for (MutationCommand mutationCommand : newMutationCommands) {
                    ASTNode node = mutationCommand.getOriginalNode();
                    ASTNode root = node.getRoot();
                    if (!(root instanceof CompilationUnit)) {
                        continue;
                    }
                    mutationCommand.executeMutation();
                    mutationHistory.add(mutationCommand);
                    numberOfExecutedMutations++;
                    writeToFile(mutationCommand.getRewriter(), file);
                    if (numberOfMutations == numberOfExecutedMutations) {
                        return;
                    }
                }
            }
        }
    }

    public List<MutationCommand> analyse(List<MutationRange> ranges, Project project) {
        // Get all ASTNodes within the ranges, create mutation command.
        // If canExecute, add to result.
        Map<String, List<MutationRange>> classToRange = new LinkedHashMap<>();
        for (MutationRange range : ranges) {
            String className = range.getClassName();
            List<MutationRange> rangesForClass;
            if (classToRange.containsKey(className)) {
                rangesForClass = classToRange.get(className);
            } else {
                rangesForClass = new ArrayList<>();
            }
            rangesForClass.add(range);
            classToRange.put(className, rangesForClass);
        }
        List<MutationCommand> result = new ArrayList<>();
        for (Entry<String, List<MutationRange>> entry : classToRange.entrySet()) {
            List<MutationRange> rangesForClass = entry.getValue();
            String className = entry.getKey();
            File file = retrieveFileFromClassName(className, project);
            String fileContent;
            try {
                fileContent = Files.readString(file.toPath());
            } catch (IOException e) {
                throw new RuntimeException("Could not read file at " + file.toPath());
            }
            MutationAnalysisParser mutationAnalysisParser = new MutationAnalysisParser();
            for (MutationRange range : rangesForClass) {
                CompilationUnit unit = ProjectParser.parseCompilationUnit(fileContent);
                AnalysisASTNodeRetriever retriever = new AnalysisASTNodeRetriever(unit, range);
                unit.accept(retriever);
                List<ASTNode> nodes = retriever.getNodes();
                for (ASTNode node : nodes) {
                    List<MutationCommand> mutationCommands = mutationAnalysisParser.parse(node);
                    if (mutationCommands == null) {
                        continue;
                    }
                    result.addAll(mutationCommands);
                }
            }
        }
        return result;
    }

    /**
     * Gets an AST node for the portion of code to be mutated
     *
     * @param unit              Compilation unit to parse
     * @param range             line number range to mutate in the compilation unit
     * @param isRandomRetrieval whether to randomly retrieve the nodes to mutate or get all that is encountered
     * @return The list of ASTNodes to mutate
     */
    private List<ASTNode> parseRangeToNodes(CompilationUnit unit, MutationRange range, boolean isRandomRetrieval) {
        MutationASTNodeRetriever retriever = new MutationASTNodeRetriever(unit, range);
        retriever.setRandomness(isRandomRetrieval);
        unit.accept(retriever);

        return retriever.getNodes();
    }

    @Override
    public List<MutationCommand> getMutationHistory() {
        return mutationHistory;
    }

    @Override
    public void clearHistory() {
        mutationHistory = new ArrayList<>();
    }
}
