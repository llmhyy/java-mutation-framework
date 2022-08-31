package jmutation.mutation;

import jmutation.execution.Coverage;
import jmutation.model.Project;
import jmutation.mutation.commands.MutationCommand;
import jmutation.mutation.parser.MutationParser;
import jmutation.parser.ProjectParser;
import jmutation.utils.RandomSingleton;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.text.edits.UndoEdit;

import java.io.File;
import java.io.FileWriter;
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
public class Mutator {
    private MutationParser mutationParser;
    private List<MutationCommand> mutationHistory;

    private int numberOfMutations;

    public Mutator(MutationParser mutationParser) {
        this.mutationParser = mutationParser;
        this.mutationHistory = new ArrayList<>();
    }

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
                     *
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

    private File retrieveFileFromClassName(String className, Project newProject) {
        File root = newProject.getRoot();
        return ProjectParser.getFileOfClass(className, root);
    }

    /**
     * Gets an AST node for the portion of code to be mutated
     *
     * @param unit
     * @param range
     * @return
     */
    private List<ASTNode> parseRangeToNodes(CompilationUnit unit, MutationRange range, boolean isRandomRetrieval) {
        MutationASTNodeRetriever retriever = new MutationASTNodeRetriever(unit, range);
        retriever.setRandomness(isRandomRetrieval);
        unit.accept(retriever);

        return retriever.getNodes();
    }

    private void writeToFile(CompilationUnit unit, File file) {
        String fileContent;
        try {
            fileContent = Files.readString(file.toPath());
        } catch (IOException e) {
            throw new RuntimeException("Could not read file at " + file.toPath());
        }
        IDocument document = new Document(fileContent);
        TextEdit edits = unit.rewrite(document, null);
        UndoEdit undo;
        try {
            undo = edits.apply(document);
        } catch (MalformedTreeException e) {
            e.printStackTrace();
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
        try {
            FileWriter fw = new FileWriter(file);
            fw.write(document.get());
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<MutationCommand> getMutationHistory() {
        return mutationHistory;
    }
}
