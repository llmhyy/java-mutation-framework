package jmutation.mutation.semantic.semseed;

import jmutation.mutation.MutationTestHelper;
import jmutation.mutation.semantic.semseed.mining.TokenSequenceCreator;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TokenSequenceCreatorTest {
    MutationTestHelper helper = new MutationTestHelper();

    @Test
    public void visit_infixExpressionWithIdentifierAndString_shouldGetTokenSequences() {
        String documentStr = "public class Main {" +
                "public static void main(String[] args) {" +
                "String a = \"string\";" +
                "if (a.b != \"string\") {" +
                "a++;" +
                "int c = b + a;" +
                "}" +
                "}" +
                "}";

        helper.parseDocStr(documentStr);
        MethodDeclaration methodDeclaration = (MethodDeclaration) helper.getBodyDeclarations().get(0);
        Block methodBody = (Block) methodDeclaration.getStructuralProperty(MethodDeclaration.BODY_PROPERTY);
        List<Statement> stmts = methodBody.statements();
        IfStatement ifStmt = (IfStatement) stmts.get(1);
        InfixExpression infixExpression = (InfixExpression) ifStmt.getExpression();
        TokenSequenceCreator tokenSequenceCreator = new TokenSequenceCreator();
        infixExpression.accept(tokenSequenceCreator);
        List<String> abstractSequence = tokenSequenceCreator.getAbstractTokens();
        List<String> concreteSequence = tokenSequenceCreator.getConcreteTokens();
        List<String> expectedAbstractSequence = List.of("Idf_0", ".", "Idf_1", "!=", "Lit_0");
        List<String> expectedConcreteSequence = List.of("a", ".", "b", "!=", "\"string\"");
        assertEquals(expectedAbstractSequence, abstractSequence);
        assertEquals(expectedConcreteSequence, concreteSequence);
    }
}
