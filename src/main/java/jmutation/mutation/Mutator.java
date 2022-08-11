package jmutation.mutation;

import jmutation.execution.Coverage;
import jmutation.model.Project;
import jmutation.mutation.commands.MutationCommand;
import jmutation.mutation.parser.MutationParser;
import jmutation.parser.ProjectParser;
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
import java.util.List;

/**
 * Given an arbitrary project (maven or gradle) and a region, we shall mutate the region into a compilable project.
 *
 * @author Yun Lin
 */
public class Mutator {
    private MutationParser mutationParser;
    private List<MutationCommand> mutationHistory;

    public Mutator(MutationParser mutationParser) {
        this.mutationParser = mutationParser;
        this.mutationHistory = new ArrayList<>();
    }

    public Project mutate(Coverage coverage, Project project) {
        List<MutationRange> ranges = coverage.getRanges();
        for (MutationRange range : ranges) {
            String className = range.getClassName();

            File file = retrieveFileFromClassName(className, project);
            String fileContent;
            try {
                fileContent = Files.readString(file.toPath());
            } catch (IOException e) {
                throw new RuntimeException("Could not read file at " + file.toPath());
            }

            CompilationUnit unit = ProjectParser.parseCompilationUnit(fileContent);
            // Attempt random retrieval. If no nodes, attempt to get all possible nodes for mutation.
            List<ASTNode> nodes = parseRangeToNodes(unit, range, true);
            if (nodes.isEmpty()) {
                nodes = parseRangeToNodes(unit, range, false);
                if (nodes.isEmpty()) continue;
                // If mutation for node types in mutation range not implemented, skip to next mutation range
            }
            unit.recordModifications();
            for (ASTNode node : nodes) {
                MutationCommand mutationCommand = mutationParser.parse(node);
                if (mutationCommand == null) {
                    continue;
                }
                mutationCommand.executeMutation();
                mutationHistory.add(mutationCommand);

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
            writeToFile(unit, file);
        }

        return project;
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
