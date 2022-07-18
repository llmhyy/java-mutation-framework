package jmutation.mutation;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ForStatement;
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

    private boolean shouldRandomlyVisit = true;

    public MutationASTNodeRetriever(CompilationUnit unit, MutationRange range) {
        this.unit = unit;
        this.startLine = range.getStartLine();
        this.endLine = range.getEndLine();
        nodes = new ArrayList<>();
    }

    /* Constructor to turn off random visits, for testing */
    public MutationASTNodeRetriever(CompilationUnit unit, MutationRange range, boolean shouldRandomlyVisit) {
        this(unit, range);
        this.shouldRandomlyVisit = shouldRandomlyVisit;
    }

    @Override
    public boolean preVisit2(ASTNode node) {
        if (!shouldRandomlyVisit) {
            return true;
        }
        if (node instanceof InfixExpression || node instanceof WhileStatement || node instanceof ForStatement || node instanceof Block) {
            boolean shouldVisit = ((int) Math.round(Math.random())) == 1;
            return shouldVisit;
        }
        return true;
    }

    @Override
    public boolean visit(InfixExpression node) {
        setNodeToList(node);
        return true;
    }

    @Override
    public boolean visit(WhileStatement node) {
        setNodeToList(node);
        return true;
    }

    @Override
    public boolean visit(ForStatement node) {
        setNodeToList(node);
        return true;
    }

    @Override
    public boolean visit(Block node) {
        setNodeToList(node);
        return true;
    }

    private void setNodeToList(ASTNode node) {
        int sLine = unit.getLineNumber(node.getStartPosition());
        int eLine = unit.getLineNumber(node.getStartPosition() + node.getLength() - 1);
        if (startLine <= sLine && eLine <= endLine) {
            nodes.add(node);
        }
    }

    public List<ASTNode> getNodes() {
        return nodes;
    }
}
