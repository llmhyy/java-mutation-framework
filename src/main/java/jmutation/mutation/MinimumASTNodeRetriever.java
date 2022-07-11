package jmutation.mutation;

import org.eclipse.jdt.core.dom.*;

public class MinimumASTNodeRetriever extends ASTVisitor{

	private int startLine;
	private int endLine;
	
	private CompilationUnit unit;
	private ASTNode node;
	
	public MinimumASTNodeRetriever(CompilationUnit unit, MutationRange range) {
		this.unit = unit;
		this.startLine = range.getStartLine();
		this.endLine = range.getEndLine();
	}

	@Override
	public boolean visit(InfixExpression node) {
		setNodeAndLines(node);
		return super.visit(node);
	}

	@Override
	public boolean visit(WhileStatement node) {
		setNodeAndLines(node);
		return super.visit(node);
	}

	public void setNodeAndLines(ASTNode node) {
		int sLine = unit.getLineNumber(node.getStartPosition());
		int eLine = unit.getLineNumber(node.getStartPosition() + node.getLength() - 1);

		if(this.node == null) {
			if(startLine <= sLine && eLine <= endLine) {
				this.node = node;
			}
		}
		else {
			/**
			 * TODO Cheng Hin
			 */
			int nSLine = unit.getLineNumber(this.node.getStartPosition());
			int nELine = unit.getLineNumber(this.node.getStartPosition() + this.node.getLength() - 1);
			if(startLine <= sLine && eLine <= endLine) {
				if(sLine <= nSLine && nELine <= eLine) {
					this.node = node;
				}
			}
		}
	}

	public ASTNode getNode() {
		return node;
	}

	public void setNode(ASTNode node) {
		this.node = node;
	}
	
}
