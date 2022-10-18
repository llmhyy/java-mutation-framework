package jmutation.mutation.heuristic.commands;

import jmutation.mutation.MutationTestHelper;
import jmutation.mutation.heuristic.commands.strong.MutationReturnReplaceArgCommand;
import org.eclipse.jdt.core.dom.ASTMatcher;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.Statement;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class MutationReturnReplaceArgCommandTest {
    MutationTestHelper helper = new MutationTestHelper();
    @Test
    public void executeMutation_possibleMutation_mutatesCorrectly() {
        String documentStr = "public class Main {" +
                "public int bar(int a) {" +
                "return 1;" +
                "}" +
                "}";

        helper.parseDocStr(documentStr);
        MethodDeclaration methodDeclaration = (MethodDeclaration) helper.getBodyDeclarations().get(0);
        Block methodBody = (Block) methodDeclaration.getStructuralProperty(MethodDeclaration.BODY_PROPERTY);
        List<Statement> stmts = methodBody.statements();
        ReturnStatement returnStatement = (ReturnStatement) stmts.get(0);
        MutationReturnReplaceArgCommand command = new MutationReturnReplaceArgCommand(returnStatement);
        command.executeMutation();
        String expectedDoc = "public class Main {" +
                "public int bar(int a) {" +
                "return a;" +
                "}" +
                "}";

        CompilationUnit actualCU = helper.getCompilationUnit();
        helper.parseDocStr(expectedDoc);
        CompilationUnit expectedCU = helper.getCompilationUnit();
        ASTMatcher matcher = new ASTMatcher();
        boolean isCorrectMutation = matcher.match(expectedCU, actualCU);
        assertTrue(isCorrectMutation);
    }
}
