package jmutation.mutation.semantic.semseed.mining;

import jmutation.mutation.semantic.semseed.model.TokenSequence;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.StringLiteral;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Creates the token sequences for a given target
 * Must be used for buggy and fixed version each
 */
public class TokenSequenceCreator extends ASTVisitor {
    private Map<String, Integer> identifierToNumber;
    private Map<String, Integer> literalToNumber;
    private int literalCounter;
    private int identifierCounter;
    private List<String> abstractTokens;
    private List<String> concreteTokens;
    private ASTNode node;

    public TokenSequenceCreator() {
        identifierToNumber = new HashMap<>();
        literalToNumber = new HashMap<>();
        abstractTokens = new ArrayList<>();
        concreteTokens = new ArrayList<>();
        literalCounter = 0;
        identifierCounter = 0;
    }

    @Override
    public void preVisit(ASTNode node) {
        if (this.node == null) {
            this.node = node;
        }
    }

    @Override
    public boolean visit(InfixExpression infixExpression) {
        concreteTokens.add(infixExpression.getOperator().toString());
        abstractTokens.add(infixExpression.getOperator().toString());
        return true;
    }

    @Override
    public boolean visit(SimpleName simpleName) {
        String identifierName = simpleName.toString();
        concreteTokens.add(identifierName);
        String abstractName = "Idf_";
        if (identifierToNumber.containsKey(identifierName)) {
            int identifierNumber = identifierToNumber.get(identifierName);
            abstractName += identifierNumber;
        } else {
            abstractName += identifierCounter;
            identifierToNumber.put(identifierName, identifierCounter);
            identifierCounter++;
        }
        abstractTokens.add(abstractName);
        return true;
    }

    @Override
    public boolean visit(StringLiteral stringLiteral) {
        String string = stringLiteral.toString();
        concreteTokens.add(string);
        String abstractName = "Lit_";
        if (literalToNumber.containsKey(string)) {
            int identifierNumber = literalToNumber.get(string);
            abstractName += identifierNumber;
        } else {
            abstractName += literalCounter;
            literalToNumber.put(string, literalCounter);
            literalCounter++;
        }
        abstractTokens.add(abstractName);
        return true;
    }

    public List<String> createAbstractTokens(String codeSnippet) {
        // split by spaces and fullstops
        String[] tokens = codeSnippet.split("[ .]+");
        return Arrays.asList(tokens);
    }

    public boolean areMatchingTokenSequences(List<String> seq0, List<String> seq1) {
        if (seq0.size() != seq1.size()) return false;
        for (int i = 0; i < seq0.size(); i++) {
            String tokenFrom0 = seq0.get(i);
            String tokenFrom1 = seq1.get(i);
            return true;
        }
        return true;
    }

    public List<String> getAbstractTokens() {
        return abstractTokens;
    }

    public List<String> getConcreteTokens() {
        return concreteTokens;
    }

    public TokenSequence getTokenSequence() {
        return new TokenSequence(abstractTokens, concreteTokens, node);
    }

    public void reset() {
        identifierToNumber = new HashMap<>();
        literalToNumber = new HashMap<>();
        abstractTokens = new ArrayList<>();
        concreteTokens = new ArrayList<>();
        literalCounter = 0;
        identifierCounter = 0;
        node = null;
    }
}
