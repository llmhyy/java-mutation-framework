package jmutation.mutation;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;

import java.util.List;

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
}
