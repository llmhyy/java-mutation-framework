package jmutation.mutation.explainable.doc.confusion.factory;

import jmutation.mutation.explainable.doc.confusion.command.ConfusionMutationCommand;
import jmutation.mutation.explainable.doc.knowledge.APIMethodKnowledge;
import jmutation.mutation.explainable.doc.knowledge.Parameter;
import jmutation.mutation.explainable.doc.nlp.similarity.JaccardSimilarity;
import jmutation.utils.RandomSingleton;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Javadoc;
import org.eclipse.jdt.core.dom.MethodDeclaration;

import java.util.ArrayList;
import java.util.List;

public class ParameterConfusionFactory extends ConfusionMutationCommandFactory {
    public ParameterConfusionFactory(APIMethodKnowledge apiMethodKnowledge, Javadoc javadoc,
                                     MethodDeclaration methodDeclaration) {
        super(apiMethodKnowledge, javadoc, methodDeclaration);
    }

    @Override
    public ConfusionMutationCommand createCommand() {
        // TODO: Calculate the sentence vectors
        // Calculate pairwise sims
        // Choose the highest pairs
        // Obtain the sentences from javadoc, and modify one of them using the argument
        // Align with expanded code (Create another module)
        // Create a command that will mutate the aligned code here
        List<Parameter> mostSimilarParameters = getMostSimilarParameters();
        mostSimilarParameters = RandomSingleton.getSingleton().shuffle(mostSimilarParameters);
        Parameter parameterToReplace = mostSimilarParameters.get(0);
        Parameter replacement = mostSimilarParameters.get(1);
        String sentenceToMutate = identifySentenceWithParam(parameterToReplace);
        if (sentenceToMutate.isEmpty()) {
            parameterToReplace = mostSimilarParameters.get(1);
            replacement = mostSimilarParameters.get(0);
            sentenceToMutate = identifySentenceWithParam(parameterToReplace);
        }
        MethodDeclaration expandedMethod = expandMethod(methodDeclaration);
        ASTNode nodeToMutate = alignSentenceWithCode(expandedMethod, sentenceToMutate);
        return new ConfusionMutationCommand(nodeToMutate);
    }

    private List<Parameter> getMostSimilarParameters() {
        List<Parameter> parameters = apiMethodKnowledge.getParameters();
        JaccardSimilarity similarityCalculator = new JaccardSimilarity();
        float maxSim = -Float.MAX_VALUE;
        List<Parameter> result = new ArrayList<>();
        for (int i = 0; i < parameters.size(); i++) {
            for (int j = i + 1; j < parameters.size(); j++) {
                Parameter param0 = parameters.get(i);
                Parameter param1 = parameters.get(j);
                float similarity = similarityCalculator.calculate(param0.toString(), param1.toString());
                if (similarity > maxSim) {
                    maxSim = similarity;
                    result.set(0, param0);
                    result.set(1, param1);
                }
            }
        }
        return result;
    }

    private String identifySentenceWithParam(Parameter parameter) {
        return "";
    }

    private MethodDeclaration expandMethod(MethodDeclaration methodDeclaration) {
        return null;
    }

    private ASTNode alignSentenceWithCode(MethodDeclaration expandedMethod, String sentence) {
        return null;
    }
}
