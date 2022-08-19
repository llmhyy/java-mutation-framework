package jmutation.mutation.command;

import jmutation.mutation.MutationTestHelper;
import jmutation.mutation.commands.MutationConditionalBoundaryCommand;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;

public class MutationComparisonOperatorCommandTest {
    MutationTestHelper helper = new MutationTestHelper();

    @Test
    public void canExecute_infixExpressionInForLoop_returnsFalse() {
        String documentStr = "public class Main {" +
                "public static void main(String[] args) {" +
                "for(int i = 0; i<5;i++) {" +
                "a++;" +
                "a = 0;" +
                "int b = 0;" +
                "}" +
                "}" +
                "}";

        helper.parseDocStr(documentStr);
        MethodDeclaration methodDeclaration = (MethodDeclaration) helper.getBodyDeclarations().get(0);
        Block methodBody = (Block) methodDeclaration.getStructuralProperty(MethodDeclaration.BODY_PROPERTY);
        List<Statement> methodStmts = methodBody.statements();
        ForStatement forStatement = (ForStatement) methodStmts.get(0);
        InfixExpression infixExpression = (InfixExpression) forStatement.getExpression();
        MutationConditionalBoundaryCommand command = new MutationConditionalBoundaryCommand(infixExpression);
        assertFalse(command.canExecute());
    }

}
