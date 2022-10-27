package jmutation.mutation.semantic.semseed.mining;

import jmutation.mutation.semantic.semseed.model.Pattern;
import org.eclipse.jdt.core.dom.ASTNode;

import java.util.List;

public class PatternCreator {
    public Pattern createPattern(ASTNode buggyNode, ASTNode fixedNode) {
        TokenSequenceCreator tokenSequenceCreator = new TokenSequenceCreator();
        buggyNode.accept(tokenSequenceCreator);
        List<String> buggyAbstractSeq = tokenSequenceCreator.getAbstractTokens();
        List<String> concreteAbstractSeq = tokenSequenceCreator.getConcreteTokens();

        tokenSequenceCreator = new TokenSequenceCreator();
        fixedNode.accept(tokenSequenceCreator);
        try {
            return new Pattern(buggyAbstractSeq, tokenSequenceCreator.getAbstractTokens(), concreteAbstractSeq, tokenSequenceCreator.getConcreteTokens(), buggyNode.getClass(), fixedNode.getClass());
        } catch (ClassNotFoundException e) {
            System.out.println("PatternCreator#createPattern: Class not found for " + buggyNode.getClass() + " or " + fixedNode.getClass());
            return null;
        }
    }
}
