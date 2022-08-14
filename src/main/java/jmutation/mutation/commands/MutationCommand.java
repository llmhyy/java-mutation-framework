package jmutation.mutation.commands;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

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
        return getClass().getSimpleName() + "#" + cu.getPackage() + "." + cu.getClass() + "#" + startLine + "-" + endLine + "#" + node;
    }
}
