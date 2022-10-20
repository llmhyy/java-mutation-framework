package jmutation.mutation.semantic.semseed.model;

import java.util.List;

public class Pattern {

    private final List<String> buggyPattern;
    private final List<String> fixPattern;
    private final List<String> buggyConcrete;
    private final List<String> fixConcrete;

    public Pattern(List<String> buggyPattern, List<String> fixPattern, List<String> buggyConcrete, List<String> fixConcrete) {
        this.buggyPattern = buggyPattern;
        this.fixPattern = fixPattern;
        this.buggyConcrete = buggyConcrete;
        this.fixConcrete = fixConcrete;
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
