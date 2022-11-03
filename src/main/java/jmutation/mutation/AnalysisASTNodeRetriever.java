package jmutation.mutation;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ReturnStatement;

public class AnalysisASTNodeRetriever extends MutationASTNodeRetriever {
    public AnalysisASTNodeRetriever(CompilationUnit unit, MutationRange range) {
        super(unit, range);
    }

    @Override
    public boolean visit(ReturnStatement returnStatement) {
        addNodeToList(returnStatement);
        return true;
    }

    @Override
    public boolean visit(Block block) {
        addNodeToList(block);
        return true;
    }

    @Override
    protected boolean addNodeToList(ASTNode node) {
        if (!nodeIsWithinRange(node)) {
            return false;
        }
        nodes.add(node);
        return true;
    }
}
