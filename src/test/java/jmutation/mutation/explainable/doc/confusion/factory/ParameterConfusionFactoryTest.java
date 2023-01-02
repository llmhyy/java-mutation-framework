package jmutation.mutation.explainable.doc.confusion.factory;

import jmutation.mutation.MutationTestHelper;
import jmutation.mutation.explainable.doc.confusion.command.ConfusionMutationCommand;
import jmutation.mutation.explainable.doc.knowledge.APIMethodKnowledge;
import jmutation.mutation.explainable.doc.knowledge.APIMethodKnowledge.APIMethodKnowledgeBuilder;
import jmutation.mutation.explainable.doc.knowledge.Parameter;
import org.eclipse.jdt.core.dom.Javadoc;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ParameterConfusionFactoryTest {

    MutationTestHelper helper = new MutationTestHelper();

    @Disabled("Not yet implemented")
    @Test
    void createCommand_apiWithSimilarParameters_confusesTheTwoParams() {
        String documentStr = "public class Test {" +
                "public void method(String arg, String arg1) {" +
                "for(int i = 0; i<5;i++) {" +
                "a++;" +
                "a = 0;" +
                "int b = 0;" +
                "}" +
                "}" +
                "}";
        helper.parseDocStr(documentStr);
        MethodDeclaration methodDeclaration = (MethodDeclaration) helper.getBodyDeclarations().get(0);
        APIMethodKnowledgeBuilder knowledgeBuilder = new APIMethodKnowledgeBuilder();
        List<Parameter> parameters = new ArrayList<>();
        Parameter indexParam = new Parameter("index", "position at which to insert subarray.");
        Parameter offsetParam = new Parameter("offset", "the index of the first char in subarray to be inserted.");
        parameters.add(indexParam);
        parameters.add(new Parameter("str", "A char array."));
        parameters.add(offsetParam);
        parameters.add(new Parameter("len", "the number of chars in the subarray to be inserted."));
        knowledgeBuilder.setParameters(parameters);
        APIMethodKnowledge knowledge = new APIMethodKnowledgeBuilder().build();
        Javadoc javadoc = null;
        ParameterConfusionFactory factory = new ParameterConfusionFactory(knowledge, javadoc, methodDeclaration);
        ConfusionMutationCommand actualCommand = factory.createCommand();
        ConfusionMutationCommand expectedCommand = new ConfusionMutationCommand(null, indexParam, offsetParam);
        assertEquals(expectedCommand, actualCommand);
    }
}