package jmutation.mutation;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

import java.util.List;

public abstract class MutationCommand {
    protected ASTRewrite rewriter;
    protected AST ast;
    protected CompilationUnit cu;

    protected int startLine;
    protected int endLine;
    protected ASTNode node;
    protected ASTNode originalNode;

    protected MutationCommand(ASTNode node) {
        this.node = node;
        ast = node.getAST();
        rewriter = ASTRewrite.create(ast);
        cu = (CompilationUnit) node.getRoot();
        startLine = cu.getLineNumber(node.getStartPosition());
        endLine = cu.getLineNumber(node.getStartPosition() + node.getLength() - 1);
    }

    public ASTNode getNode() {
        return node;
    }

    @Override
    public String toString() {
        List<TypeDeclaration> types = cu.types();
        String className = types.get(0).getName().toString();
        PackageDeclaration packageDeclaration = cu.getPackage();
        if (packageDeclaration == null) {
            return getClass().getSimpleName() + "#" + className + "#lines " + startLine + "-" + endLine + "#[" + node + "]";
        }
        String packageName = packageDeclaration.getName().toString();
        return getClass().getSimpleName() + "#" + packageName + "." + className + "#lines " + startLine + "-" + endLine + "#[" + node + "]";
    }

    public boolean canExecute() {
        return true;
    }

    public abstract ASTNode executeMutation();
}
