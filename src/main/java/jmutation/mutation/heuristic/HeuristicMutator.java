package jmutation.mutation.heuristic;

import jmutation.execution.Coverage;
import jmutation.model.MutationRange;
import jmutation.model.project.Project;
import jmutation.mutation.MutationCommand;
import jmutation.mutation.Mutator;
import jmutation.mutation.heuristic.parser.MutationParser;
import jmutation.parser.ProjectParser;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
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

    private int numberOfMutations;

    public HeuristicMutator(MutationParser mutationParser) {
        this.mutationParser = mutationParser;
        this.mutationHistory = new ArrayList<>();
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
        boolean isRandomRetrieval = true;
        for (int i = 0; i < 2; i++) {
            coverage.shuffleRanges();
            mutate(coverage, project, isRandomRetrieval);
            if (!mutationHistory.isEmpty()) {
                break;
            }
            isRandomRetrieval = false;
        }

        return project;
    }

    private void mutate(Coverage coverage, Project project, boolean isRandomRetrieval) {
        int numberOfExecutedMutations = 0;
        Map<String, List<MutationRange>> classToRange = coverage.getRangesByClass();
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
                HeuristicMutationASTNodeRetriever retriever = new HeuristicMutationASTNodeRetriever(unit, range);
                List<ASTNode> nodes = parseRangeToNodes(unit, retriever, isRandomRetrieval);
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
                    ASTNode node = mutationCommand.getNode();
                    ASTNode root = node.getRoot();
                    if (!(root instanceof CompilationUnit)) {
                        continue;
                    }
                    mutationCommand.executeMutation();
                    mutationHistory.add(mutationCommand);
                    numberOfExecutedMutations++;
                    if (numberOfMutations == numberOfExecutedMutations) {
                        writeToFile(unit, file);
                        return;
                    }
                    /**
                     * TODO:
                     * check https://www.ibm.com/docs/en/rational-soft-arch/9.5?topic=SS8PJ7_9.5.0/org.eclipse.jdt.doc.isv/reference/api/org/eclipse/jdt/core/dom/rewrite/ASTRewrite.html
                     * https://www.eclipse.org/articles/article.php?file=Article-JavaCodeManipulation_AST/index.html
                     * to rewrite the AST
                     */

                    // step 1: define mutation operator based on AST node
                    // step 2: apply mutation on the AST node
                    // step 3: rewrite the AST node back to Java doc
                }
            }
            writeToFile(unit, file);
        }
    }


}
