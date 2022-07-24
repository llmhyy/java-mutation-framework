package jmutation.mutation;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.WhileStatement;

import java.util.ArrayList;
import java.util.List;

/*
Obtains nodes to be mutated within a given range
 */
public class MutationASTNodeRetriever extends ASTVisitor {

    private int startLine;
    private int endLine;

    private CompilationUnit unit;

    private List<ASTNode> nodes;

    private MutationProbabilityCalculator mutationProbabilityCalculator;

    public MutationASTNodeRetriever(CompilationUnit unit, MutationRange range) {
        this.unit = unit;
        this.startLine = range.getStartLine();
        this.endLine = range.getEndLine();
        nodes = new ArrayList<>();
        mutationProbabilityCalculator = new MutationProbabilityCalculator();
    }

    public MutationASTNodeRetriever(CompilationUnit unit, MutationRange range, MutationProbabilityCalculator mutationProbabilityCalculator) {
        this.unit = unit;
        this.startLine = range.getStartLine();
        this.endLine = range.getEndLine();
        nodes = new ArrayList<>();
        this.mutationProbabilityCalculator = mutationProbabilityCalculator;
    }

    @Override
    public boolean visit(InfixExpression node) {
        addNodeToListRandomly(node);
        return true;
    }

    @Override
    public boolean visit(WhileStatement node) {
        addNodeToListRandomly(node);
        return true;
    }

    @Override
    public boolean visit(ForStatement node) {
        addNodeToListRandomly(node);
        return true;
    }

    @Override
    public boolean visit(Block node) {
        if (addNodeToListRandomly(node)) {
            return false;
        }
        return true;
    }

    @Override
    public boolean visit(IfStatement node) {
        addNodeToListRandomly(node);
        return true;
    }

    /**
     * Randomly adds the node to list of nodes to mutate
     *
     * @param node
     * @return True if node was added, false otherwise.
     */
    private boolean addNodeToListRandomly(ASTNode node) {
        if (!nodeIsWithinRange(node)) {
            return false;
        }
        boolean shouldNotAdd = Math.random() > mutationProbabilityCalculator.getProbability(node);
        if (shouldNotAdd) {
            return false;
        }
        nodes.add(node);
        return true;
    }

    private boolean nodeIsWithinRange(ASTNode node) {
        int sLine = unit.getLineNumber(node.getStartPosition());
        int eLine = unit.getLineNumber(node.getStartPosition() + node.getLength() - 1);
        if (startLine <= sLine && eLine <= endLine) {
            return true;
        }
        return false;
    }

    public List<ASTNode> getNodes() {
        return nodes;
    }
}
