package jmutation.model.ast;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import java.util.ArrayList;
import java.util.List;

public class JdtMethodRetriever extends ASTVisitor {
    private List<MethodDeclaration> nodeList = new ArrayList<>();
    private String className;

    @Override
    public boolean visit(MethodDeclaration mtd) {
        this.nodeList.add(mtd);
        return false;
    }

    @Override
    public boolean visit(TypeDeclaration type) {
        if (this.className == null) {
            this.className = type.getName().toString();
        }
        return true;
    }

    public List<MethodDeclaration> getMethods() {
        return this.nodeList;
    }

    public String getClassName() {
        return this.className;
    }
}
