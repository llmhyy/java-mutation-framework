package jmutation.mutation;

import jmutation.model.mutation.MutationRange;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;

/**
 * ASTNode retriever that collects all nodes, without picking randomly
 */
public class AnalysisASTNodeRetriever extends MutationASTNodeRetriever {
    public AnalysisASTNodeRetriever(CompilationUnit unit, MutationRange range) {
        super(unit, range);
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
