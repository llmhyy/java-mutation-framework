package jmutation.mutation.command;

import jmutation.mutation.MutationTestHelper;
import jmutation.mutation.commands.MutationMethodInvocationReturnCommand;
import jmutation.mutation.commands.MutationReturnStmtCommand;
import org.eclipse.jdt.core.dom.ASTMatcher;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class MutationReturnStmtCommandTest {
    MutationTestHelper helper = new MutationTestHelper();
    @Test
    public void executeMutation_validMethodDeclaration_mutatesCorrectly() {

        String documentStr = "public class Main {" +
                "public int bar() {" +
                "return 1;" +
                "}" +
                "}";

        helper.parseDocStr(documentStr);
        MethodDeclaration methodDeclaration = (MethodDeclaration) helper.getBodyDeclarations().get(0);
        Block methodBody = (Block) methodDeclaration.getStructuralProperty(MethodDeclaration.BODY_PROPERTY);
        List<Statement> stmts = methodBody.statements();
        ReturnStatement returnStatement = (ReturnStatement) stmts.get(0);
        MutationReturnStmtCommand command = new MutationReturnStmtCommand(returnStatement);
        command.executeMutation();
        String expectedDoc = "public class Main {" +
                "public int bar() {" +
                "return 0;" +
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
