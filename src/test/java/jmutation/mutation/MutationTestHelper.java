package jmutation.mutation;

import jmutation.mutation.commands.MutationCommand;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.TextEdit;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MutationTestHelper {
    CompilationUnit cu;

    public void parseDocStr(String documentStr) {
        IDocument doc = new Document(documentStr);
        ASTParser parser = ASTParser.newParser(AST.getJLSLatest());
        parser.setSource(doc.get().toCharArray());
        this.cu = (CompilationUnit) parser.createAST(null);
    }

    public TypeDeclaration getTypeDeclaration() {
        List<AbstractTypeDeclaration> types = cu.types();
        TypeDeclaration typeRoot = (TypeDeclaration) types.get(0);
        return typeRoot;
    }

    public List<BodyDeclaration> getBodyDeclarations() {
        List<AbstractTypeDeclaration> types = cu.types();
        TypeDeclaration typeRoot = (TypeDeclaration) types.get(0);
        List<BodyDeclaration> bodyDeclarations = typeRoot.bodyDeclarations();
        return bodyDeclarations;
    }

    public CompilationUnit getCompilationUnit() {
        return cu;
    }

    public IDocument updateDocument(ASTRewrite rewriter, String fileContent) {
        IDocument document = new Document(fileContent);
        TextEdit edits = rewriter.rewriteAST(document, null);
        try {
            edits.apply(document);
        } catch (MalformedTreeException | BadLocationException e) {
            e.printStackTrace();
        }
        return document;
    }

    public void checkMutation(MutationCommand command, String originalDocument, String expectedDocument) {
        IDocument mutatedDocument = updateDocument(command.getRewriter(), originalDocument);
        parseDocStr(mutatedDocument.get());
        CompilationUnit mutatedCU = cu;
        parseDocStr(expectedDocument);
        assertEquals(cu.toString(), mutatedCU.toString());
    }
}
