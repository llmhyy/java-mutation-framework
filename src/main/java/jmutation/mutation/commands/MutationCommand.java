package jmutation.mutation.commands;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

import java.util.List;

public abstract class MutationCommand {
    ASTNode node;
    ASTRewrite rewriter;
    AST ast;
    CompilationUnit cu;

    int startLine;
    int endLine;
    MutationCommand(ASTNode node) {
        this.node = node;
        ast = node.getAST();
        rewriter = ASTRewrite.create(ast);
        cu = (CompilationUnit) node.getRoot();
        startLine = cu.getLineNumber(node.getStartPosition());
        endLine = cu.getLineNumber(node.getStartPosition() + node.getLength() - 1);
    }
    abstract public ASTNode executeMutation();

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
}
