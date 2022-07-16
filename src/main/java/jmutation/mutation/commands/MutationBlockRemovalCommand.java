package jmutation.mutation.commands;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.Statement;

import java.util.List;

public class MutationBlockRemovalCommand extends MutationCommand {
    public MutationBlockRemovalCommand(ASTNode node) {
        super(node);
    }

    @Override
    public ASTNode executeMutation(){
        Block block = (Block) node;
        List<Statement> stmts = block.statements();
        stmts.clear();
        return block;
    }
}
