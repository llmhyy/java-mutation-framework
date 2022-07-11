package jmutation.mutation.commands;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

public abstract class MutationCommand {
    ASTNode node;
    ASTRewrite rewriter;
    AST ast;
    MutationCommand(ASTNode node) {
        this.node = node;
        ast = node.getAST();
        rewriter = ASTRewrite.create(ast);
    }
    abstract public ASTNode executeMutation();
}
