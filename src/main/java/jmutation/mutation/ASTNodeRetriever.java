package jmutation.mutation;

import jmutation.mutation.parser.MutationParser;
import org.eclipse.jdt.core.dom.*;

import java.util.ArrayList;
import java.util.List;

/*
Obtains nodes to be mutated within a given range
 */
public class ASTNodeRetriever extends ASTVisitor{

	private int startLine;
	private int endLine;
	
	private CompilationUnit unit;
	private List<ASTNode> nodes;

	private boolean shouldRandomlyVisit = true;
	
	public ASTNodeRetriever(CompilationUnit unit, MutationRange range) {
		this.unit = unit;
		this.startLine = range.getStartLine();
		this.endLine = range.getEndLine();
		nodes = new ArrayList<>();
	}

	/* Constructor to turn off random visits, for testing */
	public ASTNodeRetriever(CompilationUnit unit, MutationRange range, boolean shouldRandomlyVisit) {
		this(unit, range);
		this.shouldRandomlyVisit = shouldRandomlyVisit;
	}

	@Override
	public boolean preVisit2(ASTNode node) {
		if (!shouldRandomlyVisit) {
			return true;
		}
		boolean shouldVisit = ((int) Math.round(Math.random())) == 1;
		return shouldVisit;
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
	public boolean visit(Block node) {
		setNodeToList(node);
		// If removing block, should not visit its children
		return false;
	}

	private void setNodeToList(ASTNode node) {
		int sLine = unit.getLineNumber(node.getStartPosition());
		int eLine = unit.getLineNumber(node.getStartPosition() + node.getLength() - 1);
		if(startLine <= sLine && eLine <= endLine) {
			nodes.add(node);
		}
	}

	public List<ASTNode> getNodes() {
		return nodes;
	}
}
