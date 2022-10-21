package jmutation.mutation.semantic.semseed;

import jmutation.mutation.semantic.semseed.model.Pattern;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.StringLiteral;

import java.util.List;

/*
Given a parent node, check if its children follows the pattern
 */
public class SyntaxMatchingVisitor extends ASTVisitor {
    private Pattern pattern;
    private AST patternAST;
    private List<String> buggyPattern;

    private int idx;

    private boolean hasFailed = false;

    public SyntaxMatchingVisitor(Pattern pattern) {
        // 1. Create AST of fix pattern, check if they match with some subtree of the AST
        // 2.
        super();
        this.pattern = pattern;
        buggyPattern = pattern.getBuggyPattern();
    }

    @Override
    public boolean visit(StringLiteral stringLiteral) {
        // Can use ASTMatcher here?
        String token = buggyPattern.get(idx);
        if (!token.startsWith("Lit_")) {
            hasFailed = true;
            return false;
        }
        return false;
    }

    @Override
    public boolean visit(SimpleName simpleName) {
        String token = buggyPattern.get(idx);
        if (!token.startsWith("Idf_")) {
            hasFailed = true;
            return false;
        }
        return false;
    }

    private boolean canContinue() {
        return idx < pattern.getPatternLen();
    }
}
