package jmutation.mutation.heuristic;

import jmutation.model.MutationRange;
import jmutation.mutation.MutationASTNodeRetriever;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ReturnStatement;

/*
Obtains nodes to be mutated within a given range
 */
public class HeuristicMutationASTNodeRetriever extends MutationASTNodeRetriever {

    public HeuristicMutationASTNodeRetriever(CompilationUnit unit, MutationRange range) {
        super(unit, range);
    }

    public HeuristicMutationASTNodeRetriever(CompilationUnit unit, MutationRange range, MutationProbabilityCalculator mutationProbabilityCalculator) {
        super(unit, range, mutationProbabilityCalculator);
    }

    @Override
    public boolean visit(Block node) {
        // Since we are removing all stmts in block, should not visit its children
        if (addNodeToList(node)) {
            return false;
        }
        return true;
    }

    @Override
    public boolean visit(ReturnStatement node) {
        // Should not visit children in return statement, if we are replacing the expression (return <expression>;)
        // with default value.
        if (addNodeToList(node)) {
            return false;
        }
        return true;
    }
}
