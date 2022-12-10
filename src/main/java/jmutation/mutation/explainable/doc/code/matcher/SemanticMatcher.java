package jmutation.mutation.explainable.doc.code.matcher;

import jmutation.model.ast.JdtMethodRetriever;
import jmutation.mutation.explainable.doc.model.CodeChunk;
import jmutation.mutation.explainable.doc.model.JavaComment;
import jmutation.parser.ProjectParser;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class SemanticMatcher extends CommentCodeMatcher {
    @Override
    public CodeChunk match(String focusedComment, JavaComment comment, String filePath) {
        // Find code segment with semantically similar meaning.
        // 1. get method code
        //  Get line number of comment, and get the closest code (check the closest line right below)
        // Both cases get method.
        // Only get nodes that are close for inline. JavaDoc, is free for all
        // Sometimes it is the java doc, so get method.
        // Sometimes it is inline, so get
        // 2. Obtain semantic similarity of each line (Iterator), and store the line with the highest similarity
        // 3. Create code chunk from the line (ASTVisitor)
        try {
            MethodDeclaration method = getMethodDeclaration(comment, filePath);
            ASTNode mostSimilarNode = new JaccardSemanticSimilarityCalculator(focusedComment, comment, method).start();
            return new CodeChunk(filePath, mostSimilarNode);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Different cases it should handle?
        // 1. removing if statement
        // 2. Removing condition from if statement
        return null;
    }

    private MethodDeclaration getMethodDeclaration(JavaComment comment, String filePath) throws IOException {
        String source = Files.readString(Path.of(filePath));
        CompilationUnit unit = ProjectParser.parseCompilationUnit(source);
        JdtMethodRetriever methodRetriever = new JdtMethodRetriever();
        unit.accept(methodRetriever);
        List<MethodDeclaration> methods = methodRetriever.getMethods();
        for (MethodDeclaration method : methods) {
            int startPos = method.getStartPosition();
            int startLine = unit.getLineNumber(startPos);
            int endLine = unit.getLineNumber(startLine + method.getLength());
            if (comment.getStartLine() >= startLine && comment.getEndLine() <= endLine) {
                return method;
            }
        }
        return null;
    }
}
