package jmutation.mutation.semantic;

import com.github.jfasttext.JFastText;
import jmutation.mutation.semantic.semseed.FastTextWrapper;
import jmutation.mutation.semantic.semseed.model.Pattern;
import jmutation.mutation.semantic.semseed.model.TokenSequence;

import java.util.List;

public class FastTextWrapperStub extends FastTextWrapper {
    private List<String> replacementTokens;
    private boolean isMatching;

    public FastTextWrapperStub(JFastText jft) {
        super(jft);
    }

    @Override
    public List<String> getReplacementTokens(String buggyConcreteToken, Pattern pattern, TokenSequence targetTokenSeq,
                                             List<String> candidates) {
        return replacementTokens;
    }

    @Override
    public boolean isSemanticallyMatching(List<String> targetConcreteTokens, List<String> fixConcreteTokens) {
        return isMatching;
    }

    public void setReplacementTokens(List<String> replacementTokens) {
        this.replacementTokens = replacementTokens;
    }

    public void setMatching(boolean matching) {
        isMatching = matching;
    }
}
