package jmutation.mutation;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

import java.util.List;

public abstract class MutationCommand {
    protected ASTNode node;
    protected ASTNode originalNode;
    protected ASTRewrite rewriter;
    protected AST ast;
    protected CompilationUnit cu;

    protected int startLine;
    protected int endLine;

    protected MutationCommand(ASTNode node) {
        originalNode = node;
        cu = (CompilationUnit) node.getRoot();
        ast = cu.getAST();
        this.node = ASTNode.copySubtree(ast, originalNode);
        rewriter = ASTRewrite.create(ast);
        startLine = cu.getLineNumber(node.getStartPosition());
        endLine = cu.getLineNumber(node.getStartPosition() + node.getLength() - 1);
    }

    abstract public ASTNode executeMutation();

    public boolean canExecute() {
        return true;
    }

    public ASTNode getNode() {
        return node;
    }

    public CompilationUnit getCu() {
        return cu;
    }

    public ASTRewrite getRewriter() {
        rewriter.replace(originalNode, node, null);
        return rewriter;
    }

    public ASTNode getOriginalNode() {
        return originalNode;
    }

    @Override
    public String toString() {
        List<AbstractTypeDeclaration> types = cu.types();
        String className = types.get(0).getName().toString();
        PackageDeclaration packageDeclaration = cu.getPackage();
        if (packageDeclaration == null) {
            return getClass().getSimpleName() + "#" + className +
                    "#lines " + startLine + "-" + endLine +
                    "#[" + originalNode + "]->[" + node + "]";
        }
        String packageName = packageDeclaration.getName().toString();
        return getClass().getSimpleName() + "#" + packageName + "." + className +
                "#lines " + startLine + "-" + endLine +
                "#[" + originalNode + "]->[" + node + "]";
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof MutationCommand)) {
            return false;
        }
        return this.toString().equals(other.toString());
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }
}
