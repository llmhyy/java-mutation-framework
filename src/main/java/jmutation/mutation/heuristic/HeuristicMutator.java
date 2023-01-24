package jmutation.mutation.heuristic;

import jmutation.execution.Coverage;
import jmutation.model.mutation.MutationRange;
import jmutation.model.project.Project;
import jmutation.mutation.AnalysisASTNodeRetriever;
import jmutation.mutation.MutationCommand;
import jmutation.mutation.Mutator;
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

    public HeuristicMutator() {
        super();
        mutationParser = new MutationParser();
    }

    public HeuristicMutator(MutationParser mutationParser) {
        super();
        this.mutationParser = mutationParser;
    }

    public Project mutate(Coverage coverage, Project project) {
        List<MutationRange> ranges = coverage.getRanges();
        mutate(ranges, project);
        return project;
    }

    private void mutate(List<MutationRange> ranges, Project project) {
        List<MutationCommand> commands = analyse(ranges, project);
        if (commands.isEmpty()) return;
        int commandIdx = (int) (RandomSingleton.getSingleton().random() * commands.size());
        mutate(commands.get(commandIdx), project);
    }

    public List<MutationCommand> analyse(List<MutationRange> ranges, Project project) {
        // Get all ASTNodes within the ranges, create mutation command.
        Map<String, List<MutationRange>> classToRange = mapRangesToClassNameAndRanges(ranges);
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
            for (MutationRange range : rangesForClass) {
                CompilationUnit unit = ProjectParser.parseCompilationUnit(fileContent);
                AnalysisASTNodeRetriever retriever = new AnalysisASTNodeRetriever(unit, range);
                unit.accept(retriever);
                List<ASTNode> nodes = retriever.getNodes();
                for (ASTNode node : nodes) {
                    List<MutationCommand> mutationCommands = mutationParser.parse(node);
                    if (mutationCommands == null) {
                        continue;
                    }
                    result.addAll(mutationCommands);
                }
            }
        }
        return result;
    }

    private Map<String, List<MutationRange>> mapRangesToClassNameAndRanges(List<MutationRange> ranges) {
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
        return classToRange;
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
