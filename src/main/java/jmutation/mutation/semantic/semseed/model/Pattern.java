package jmutation.mutation.semantic.semseed.model;

import org.eclipse.jdt.core.dom.ASTNode;

import java.util.List;

public class Pattern {

    private final List<String> buggyPattern;
    private final List<String> fixPattern;
    private final List<String> buggyConcrete;
    private final List<String> fixConcrete;

    private final Class<? extends ASTNode> buggyASTNodeClass;
    private final Class<? extends ASTNode> fixASTNodeClass;

    public Pattern(List<String> buggyPattern, List<String> fixPattern, List<String> buggyConcrete,
                   List<String> fixConcrete, String buggyASTNodeClass, String fixASTNodeClass) throws ClassNotFoundException {
        assert (buggyPattern.size() == fixPattern.size() && fixPattern.size() == buggyConcrete.size() &&
                buggyConcrete.size() == fixConcrete.size());
        this.buggyPattern = buggyPattern;
        this.fixPattern = fixPattern;
        this.buggyConcrete = buggyConcrete;
        this.fixConcrete = fixConcrete;
        this.buggyASTNodeClass = (Class<? extends ASTNode>) Class.forName(buggyASTNodeClass);
        this.fixASTNodeClass = (Class<? extends ASTNode>) Class.forName(fixASTNodeClass);
    }

    public List<String> getBuggyPattern() {
        return buggyPattern;
    }

    public List<String> getFixPattern() {
        return fixPattern;
    }

    public List<String> getBuggyConcrete() {
        return buggyConcrete;
    }

    public List<String> getFixConcrete() {
        return fixConcrete;
    }

    public int getPatternLen() {
        return buggyPattern.size();
    }

    public Class<? extends ASTNode> getBuggyASTNodeClass() {
        return buggyASTNodeClass;
    }

    public Class<? extends ASTNode> getFixASTNodeClass() {
        return fixASTNodeClass;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof Pattern)) {
            return false;
        }
        Pattern otherPattern = (Pattern) other;
        return buggyPattern.equals(otherPattern.buggyPattern) && fixPattern.equals(otherPattern.fixPattern) &&
                buggyConcrete.equals(otherPattern.buggyConcrete) && fixConcrete.equals(otherPattern.fixConcrete);
    }
}
