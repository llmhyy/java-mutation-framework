package jmutation.mutation.semantic.semseed;

import jmutation.model.ast.ASTNodeParentRetriever;
import jmutation.mutation.MutationCommand;
import jmutation.mutation.semantic.semseed.constants.TokenPrefix;
import jmutation.mutation.semantic.semseed.constants.TokenScope;
import jmutation.mutation.semantic.semseed.model.Pattern;
import jmutation.mutation.semantic.semseed.model.StaticAnalysisResult;
import jmutation.mutation.semantic.semseed.model.TokenSequence;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.gradle.internal.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class SemSeedMutationCommand extends MutationCommand {
    private static final String PLACEHOLDER_TOKEN = "PLACEHOLDER";
    private StaticAnalysisResult tokenReplacements;
    private Pattern pattern;
    private TokenSequence targetSequence;

    public SemSeedMutationCommand(ASTNode node) {
        super(node);
    }

    public void setTokenReplacements(StaticAnalysisResult tokenReplacements) {
        this.tokenReplacements = tokenReplacements;
    }

    public void setPattern(Pattern pattern) {
        this.pattern = pattern;
    }

    public void setTargetSequence(TokenSequence targetSequence) {
        this.targetSequence = targetSequence;
    }

    @Override
    public ASTNode executeMutation() {
        boolean wasSuccessful = false;
        List<List<String>> mutatedTokenSequences = formMutatedTokenSequences();
        for (List<String> possibleMutatedSeq : mutatedTokenSequences) {
            applySeq(possibleMutatedSeq);
            if (recompile()) {
                wasSuccessful = true;
                break;
            }
        }
        if (wasSuccessful) return node;
        return null;
    }

    private List<List<String>> formMutatedTokenSequences() {
        List<String> buggyAbstractSeq = pattern.getBuggyPattern();
        List<String> buggyConcreteSeq = pattern.getBuggyConcrete();
        List<String> mutatedTokenSeq = new ArrayList<>();
        Map<Integer, List<String>> replacementsByIdx = new HashMap<>();
        // TODO: Figure out how to pass the path to model
        FastTextWrapper fastTextWrapper = new FastTextWrapper("");
        for (int i = 0; i < buggyAbstractSeq.size(); i++) {
            String buggyAbstractToken = buggyAbstractSeq.get(i);
            boolean tokenIsIdf = buggyAbstractToken.startsWith(TokenPrefix.PREFIX_IDENTIFIER);
            if (!tokenIsIdf &&
                    !buggyAbstractToken.startsWith(TokenPrefix.PREFIX_LITERAL)) {
                mutatedTokenSeq.add(buggyConcreteSeq.get(i));
            } else if (pattern.isBound(buggyAbstractToken)) {
                // Get index of token in fixed sequence, get the concrete token from target at same index
                int idxInFixPattern = pattern.getFixPattern().indexOf(buggyAbstractToken);
                mutatedTokenSeq.add(targetSequence.getConcreteTokens().get(idxInFixPattern));
            } else {
                List<String> replacements = fastTextWrapper.getReplacementTokens(buggyConcreteSeq.get(i), pattern, targetSequence,
                        getCandidates(tokenIsIdf));
                if (replacements.size() == 0) {
                    return null;
                }
                replacementsByIdx.put(i, replacements);
                mutatedTokenSeq.add(PLACEHOLDER_TOKEN);
            }
        }
        List<List<String>> possibleMutatedTokenSeqs = createPossibleMutatedSeqsFromReplacements(replacementsByIdx,
                mutatedTokenSeq);
        return null;
    }

    private List<String> getCandidates(boolean isIdentifiers) {
        return new ArrayList<>(tokenReplacements.getTokens(TokenScope.SCOPE_METHOD, isIdentifiers, getMethodName()));
    }

    private String getClassName() {
        ASTNodeParentRetriever<TypeDeclaration> typeDeclarationASTNodeParentRetriever =
                new ASTNodeParentRetriever<>(TypeDeclaration.class);
        TypeDeclaration typeDeclaration = typeDeclarationASTNodeParentRetriever.getParentOfType(node);
        return typeDeclaration.getName().toString();
    }

    private String getMethodName() {
        ASTNodeParentRetriever<MethodDeclaration> methodDeclarationASTNodeParentRetriever =
                new ASTNodeParentRetriever<>(MethodDeclaration.class);
        MethodDeclaration methodDeclaration = methodDeclarationASTNodeParentRetriever.getParentOfType(node);
        return getClassName() + "#" + methodDeclaration.getName();
    }

    private boolean recompile() {
        return true;
    }

    private List<List<String>> createPossibleMutatedSeqsFromReplacements(Map<Integer, List<String>> replacementsByIdxOfSeq,
                                                                         List<String> mutatedTokenSeq) {
        int totalPossibleCombinations = 0;
        // token id as key, to the idx of replacement to use as value
        Map<Integer, Pair<Integer, Integer>> replacementIdxTracker = new HashMap<>();
        for (Entry<Integer, List<String>> idxToReplacements : replacementsByIdxOfSeq.entrySet()) {
            totalPossibleCombinations += idxToReplacements.getValue().size();
            replacementIdxTracker.put(idxToReplacements.getKey(), Pair.of(0, idxToReplacements.getValue().size() - 1));
        }
        List<List<String>> possibleMutatedTokenSeqs = new ArrayList<>();
        for (int i = 0; i < totalPossibleCombinations; i++) {
            List<String> possibleMutatedSeq = new ArrayList<>(mutatedTokenSeq);
            for (Entry<Integer, Pair<Integer, Integer>> replacementIdx : replacementIdxTracker.entrySet()) {
                List<String> replacements = replacementsByIdxOfSeq.get(replacementIdx.getKey());
                possibleMutatedSeq.set(replacementIdx.getKey(), replacements.get(replacementIdx.getValue().left));
            }
            possibleMutatedTokenSeqs.add(possibleMutatedSeq);
            // Increment the last replacement idx
            int idx = replacementIdxTracker.size() - 1;
            Pair<Integer, Integer> currIdx = replacementIdxTracker.get(idx);
            Pair<Integer, Integer> replacementPair = Pair.of(currIdx.left + 1, currIdx.right);
            replacementIdxTracker.put(idx, replacementPair);

            // If the last idx overflow, add 1 to the next replacement idx and repeat
            // e.g. 0999 + 1 -> 1000 if at each idx, there is at most 10 replacements
            while (currIdx.left > currIdx.right) {
                replacementPair = Pair.of(0, currIdx.right);
                replacementIdxTracker.put(idx, replacementPair);
                currIdx = replacementIdxTracker.get(idx - 1);
                replacementPair = Pair.of(currIdx.left + 1, currIdx.right);
                replacementIdxTracker.put(idx - 1, replacementPair);
                idx--;
            }
        }
        return possibleMutatedTokenSeqs;
    }

    private void applySeq(List<String> mutatedSeq) {

    }
}
