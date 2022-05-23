package jmutation.model.ast;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.MethodDeclaration;

import java.util.ArrayList;
import java.util.List;

public class JdtMethodRetriever extends ASTVisitor {
    private List<MethodDeclaration> nodeList = new ArrayList<>();

    public boolean visit(MethodDeclaration mtd) {
        this.nodeList.add(mtd);
        return false;
    }

    public List<MethodDeclaration> getMethods() {
        return this.nodeList;
    }
}
