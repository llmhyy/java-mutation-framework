package jmutation.mutation.commands;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.Statement;

import java.util.List;

public class MutationBlockRemovalCommand extends MutationCommand {
    public MutationBlockRemovalCommand(ASTNode node) {
        super(node);
    }

    @Override
    public ASTNode executeMutation() {
        Block block = (Block) node;
        List<Statement> stmts = block.statements();
        ASTNode blockParent = node.getParent();
        if (blockParent instanceof MethodDeclaration) {
            // If block is not outermost block, safe to remove
            for (Statement stmt : stmts) {
                // If return statement removed, will not compile.
                if (stmt instanceof ReturnStatement) {
                    return null;
                }
            }
        }
        stmts.clear();
        return block;
    }
}
