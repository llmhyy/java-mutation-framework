package jmutation.mutation;

import jmutation.model.mutation.MutationRange;
import org.eclipse.jdt.core.dom.CompilationUnit;

/**
 * ASTNode retriever that collects all nodes, without picking randomly
 */
public class AnalysisASTNodeRetriever extends MutationASTNodeRetriever {
    public AnalysisASTNodeRetriever(CompilationUnit unit, MutationRange range) {
        super(unit, range);
        setRandomness(false);
    }
}
