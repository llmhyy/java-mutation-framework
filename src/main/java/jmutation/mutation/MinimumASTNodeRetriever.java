package jmutation.mutation;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;

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

	public void preVisit(ASTNode node) {
		int sLine = unit.getLineNumber(node.getStartPosition());
		int eLine = unit.getLineNumber(node.getStartPosition() + node.getLength());
		
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
			int nELine = unit.getLineNumber(this.node.getStartPosition() + this.node.getLength());
			
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
