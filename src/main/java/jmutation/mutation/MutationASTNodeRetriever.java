package jmutation.mutation;

import jmutation.model.mutation.MutationRange;
import jmutation.mutation.heuristic.MutationProbabilityCalculator;
import jmutation.utils.RandomSingleton;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.WhileStatement;

import java.util.ArrayList;
import java.util.List;

/*
Obtains nodes to be mutated within a given range
 */
public class MutationASTNodeRetriever extends ASTVisitor {

    private final CompilationUnit unit;
    protected List<ASTNode> nodes;
    private int startLine;
    private int endLine;
    private MutationProbabilityCalculator mutationProbabilityCalculator;

    private boolean isRandomRetrieval = true;

    public MutationASTNodeRetriever(CompilationUnit unit) {
        this.unit = unit;
        this.startLine = 0;
        this.endLine = Integer.MAX_VALUE;
        nodes = new ArrayList<>();
        mutationProbabilityCalculator = new MutationProbabilityCalculator();
    }

    public MutationASTNodeRetriever(CompilationUnit unit, MutationRange range) {
        this(unit);
        this.startLine = range.getStartLine();
        this.endLine = range.getEndLine();
    }

    public MutationASTNodeRetriever(CompilationUnit unit, MutationRange range, MutationProbabilityCalculator mutationProbabilityCalculator) {
        this(unit, range);
        this.mutationProbabilityCalculator = mutationProbabilityCalculator;
    }

    public void setRandomness(boolean isRandomRetrieval) {
        this.isRandomRetrieval = isRandomRetrieval;
    }

    @Override
    public boolean visit(InfixExpression node) {
        addNodeToList(node);
        return true;
    }

    @Override
    public boolean visit(WhileStatement node) {
        addNodeToList(node);
        return true;
    }

    @Override
    public boolean visit(ForStatement node) {
        addNodeToList(node);
        return true;
    }

    @Override
    public boolean visit(Block node) {
        addNodeToList(node);
        return true;
    }

    @Override
    public boolean visit(IfStatement node) {
        addNodeToList(node);
        return true;
    }

    @Override
    public boolean visit(ReturnStatement node) {
        addNodeToList(node);
        return true;
    }

    /**
     * Randomly adds the node to list of nodes to mutate
     *
     * @param node
     * @return True if node was added, false otherwise.
     */
    protected boolean addNodeToList(ASTNode node) {
        if (!nodeIsWithinRange(node)) {
            return false;
        }
        if (isRandomRetrieval) {
            double randomValue = RandomSingleton.getSingleton().random();
            boolean shouldNotAdd = randomValue > mutationProbabilityCalculator.getProbability(node);
            if (shouldNotAdd) {
                return false;
            }
        }
        nodes.add(node);
        return true;
    }

    protected boolean nodeIsWithinRange(ASTNode node) {
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
