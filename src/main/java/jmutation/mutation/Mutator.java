package jmutation.mutation;

import jmutation.execution.Coverage;
import jmutation.model.mutation.MutationRange;
import jmutation.model.project.Project;
import jmutation.parser.ProjectParser;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.TextEdit;

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

public abstract class Mutator {
    protected List<MutationCommand> mutationHistory;

    public Mutator() {
        this.mutationHistory = new ArrayList<>();
    }

    public abstract Project mutate(Coverage coverage, Project project);

    public Project mutate(MutationCommand command, Project project) {
        CompilationUnit unit = command.getCu();
        TypeDeclaration type = (TypeDeclaration) unit.types().get(0);
        String className = unit.getPackage().getName() + "." + type.getName().toString();
        File file = retrieveFileFromClassName(className, project);
        command.executeMutation();
        mutationHistory.add(command);
        try {
            writeToFile(command.getRewriter(), file);
        } catch (Exception e) {
            System.out.println(e);
        }
        return project;
    }

    /**
     * Obtains all possible mutation commands that can be executed
     *
     * @param mutationRanges
     * @param project
     * @return
     */
    public abstract List<MutationCommand> analyse(List<MutationRange> mutationRanges, Project project);

    public List<MutationCommand> getMutationHistory() {
        return mutationHistory;
    }

    public void clearHistory() {
        mutationHistory = new ArrayList<>();
    }

    protected File retrieveFileFromClassName(String className, Project newProject) {
        File root = newProject.getRoot();
        return ProjectParser.getFileOfClass(className, root);
    }

    /**
     * Gets an AST node for the portion of code to be mutated
     *
     * @param unit              Compilation unit to parse
     * @param retriever         ASTVisitor which obtains ASTNodes to mutate from given mutation range
     * @param isRandomRetrieval whether to randomly retrieve the nodes to mutate or get all that is encountered
     * @return The list of ASTNodes to mutate
     */
    protected List<ASTNode> parseRangeToNodes(CompilationUnit unit, MutationASTNodeRetriever retriever,
                                              boolean isRandomRetrieval) {
        retriever.setRandomness(isRandomRetrieval);
        unit.accept(retriever);

        return retriever.getNodes();
    }

    protected void writeToFile(CompilationUnit unit, File file) {
        String fileContent;
        try {
            fileContent = Files.readString(file.toPath());
        } catch (IOException e) {
            throw new RuntimeException("Could not read file at " + file.toPath());
        }
        IDocument document = new Document(fileContent);
        TextEdit edits = unit.rewrite(document, null);
        try {
            edits.apply(document);
        } catch (MalformedTreeException | BadLocationException e) {
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

    protected void writeToFile(ASTRewrite rewriter, File file) {
        String fileContent;
        try {
            fileContent = Files.readString(file.toPath());
        } catch (IOException e) {
            throw new RuntimeException("Could not read file at " + file.toPath());
        }
        IDocument document = new Document(fileContent);
        TextEdit edits = rewriter.rewriteAST(document, null);
        try {
            edits.apply(document);
        } catch (MalformedTreeException | BadLocationException e) {
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
}
