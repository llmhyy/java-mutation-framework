package jmutation.mutation.semantic.semseed;

import jmutation.mutation.MutationTestHelper;
import jmutation.mutation.semantic.FastTextWrapperStub;
import jmutation.mutation.semantic.semseed.model.Pattern;
import jmutation.mutation.semantic.semseed.model.StaticAnalysisResult;
import jmutation.mutation.semantic.semseed.model.TokenSequence;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.Statement;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static jmutation.mutation.semantic.semseed.constants.TokenPrefix.PREFIX_IDENTIFIER;

public class SemSeedMutationCommandTest {
    MutationTestHelper helper = new MutationTestHelper();

    @Test
    public void executeMutation_handlesUnboundTokens_validMutation() throws ClassNotFoundException {
        // TODO: create token replacements, target sequence, bug fix pattern and the target code, see if it mutates correctly
        // Future can use sample project
        String documentStr = "public class Main {" +
                "public boolean bar(int a) {" +
                "return var0 == var1;" +
                "}" +
                "}";

        helper.parseDocStr(documentStr);
        MethodDeclaration methodDeclaration = (MethodDeclaration) helper.getBodyDeclarations().get(0);
        Block methodBody = (Block) methodDeclaration.getStructuralProperty(MethodDeclaration.BODY_PROPERTY);
        List<Statement> stmts = methodBody.statements();
        ReturnStatement returnStatement = (ReturnStatement) stmts.get(0);
        InfixExpression infixExpression = (InfixExpression) returnStatement.getExpression();

        // Creating target token seq
        List<String> abstractTokens = new ArrayList<>();
        abstractTokens.add(PREFIX_IDENTIFIER + 0);
        abstractTokens.add("==");
        abstractTokens.add(PREFIX_IDENTIFIER + 1);
        List<String> concreteTokens = new ArrayList<>();
        concreteTokens.add("var0");
        concreteTokens.add("==");
        concreteTokens.add("var1");
        TokenSequence targetSequence = new TokenSequence(abstractTokens, concreteTokens, infixExpression);

        // Create bug fix pattern
        List<String> buggyAbstractTokens = new ArrayList<>();
        buggyAbstractTokens.add(PREFIX_IDENTIFIER + 0);
        buggyAbstractTokens.add("!=");
        buggyAbstractTokens.add(PREFIX_IDENTIFIER + 1);
        List<String> buggyConcreteTokens = new ArrayList<>();
        buggyConcreteTokens.add("a");
        buggyConcreteTokens.add("!=");
        buggyConcreteTokens.add("b");
        List<String> fixedAbstractTokens = new ArrayList<>();
        fixedAbstractTokens.add(PREFIX_IDENTIFIER + 0);
        fixedAbstractTokens.add("==");
        fixedAbstractTokens.add(PREFIX_IDENTIFIER + 2);
        List<String> fixedConcreteTokens = new ArrayList<>();
        fixedConcreteTokens.add("a");
        fixedConcreteTokens.add("==");
        fixedConcreteTokens.add("c");
        String nodeClassName = "org.eclipse.jdt.core.dom.InfixExpression";
        Pattern bugfixPattern = new Pattern(buggyAbstractTokens, fixedAbstractTokens, buggyConcreteTokens,
                fixedConcreteTokens, nodeClassName, nodeClassName);

        // Create token replacement
        Map<String, Integer> topIdentifiers = new HashMap<>();
        Map<String, Set<String>> identifiersByMethod = new HashMap<>();
        StaticAnalysisResult tokenReplacements = new StaticAnalysisResult(topIdentifiers, new HashMap<>(),
                new HashMap<>(), new HashMap<>(), identifiersByMethod, new HashMap<>());

        // Create fast text wrapper
        FastTextWrapperStub fastTextWrapperStub = new FastTextWrapperStub();
        List<String> replacementTokens = new ArrayList<>();
        replacementTokens.add("id");
        fastTextWrapperStub.setReplacementTokens(replacementTokens);

        // Execute mutation
        SemSeedMutationCommand command = new SemSeedMutationCommand(infixExpression, tokenReplacements, bugfixPattern,
                targetSequence, fastTextWrapperStub);
        command.executeMutation();
        String expectedDoc = "public class Main {" +
                "public boolean bar(int a) {" +
                "return var0 != id;" +
                "}" +
                "}";
        helper.checkMutation(command, documentStr, expectedDoc);
    }
}
