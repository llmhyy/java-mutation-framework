package jmutation.mutation.command.strong;

import jmutation.mutation.MutationTestHelper;
import jmutation.mutation.commands.strong.MutationReturnStmtCommand;
import org.eclipse.jdt.core.dom.ASTMatcher;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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

    @Test
    public void canExecute_returnStmtInTryBlock_cannotMutate() {

        String documentStr = "public class Main {" +
                "public int bar() {" +
                "try {" +
                "return foo();" +
                "} catch (Exception e) {" +
                "}" +
                "}" +
                "public int foo() {" +
                "return 1;" +
                "}" +
                "}";

        helper.parseDocStr(documentStr);
        MethodDeclaration methodDeclaration = (MethodDeclaration) helper.getBodyDeclarations().get(0);
        Block methodBody = (Block) methodDeclaration.getStructuralProperty(MethodDeclaration.BODY_PROPERTY);
        List<Statement> stmts = methodBody.statements();
        TryStatement tryStatement = (TryStatement) stmts.get(0);
        Block tryBlock = tryStatement.getBody();
        List<Statement> tryBlockStmts = tryBlock.statements();
        ReturnStatement returnStatement = (ReturnStatement) tryBlockStmts.get(0);
        MutationReturnStmtCommand command = new MutationReturnStmtCommand(returnStatement);
        assertFalse(command.canExecute());
    }

    @Test
    public void canExecute_returnsDefaultInt_canMutate() {

        String documentStr = "public class Main {" +
                "public int bar() throws Exception {" +
                "return 0;" +
                "}" +
                "}";

        helper.parseDocStr(documentStr);
        CompilationUnit cu = helper.getCompilationUnit();
        MethodDeclaration methodDeclaration = (MethodDeclaration) helper.getBodyDeclarations().get(0);
        Block methodBody = (Block) methodDeclaration.getStructuralProperty(MethodDeclaration.BODY_PROPERTY);
        List<Statement> stmts = methodBody.statements();
        ReturnStatement returnStatement = (ReturnStatement) stmts.get(0);
        MutationReturnStmtCommand command = new MutationReturnStmtCommand(returnStatement);
        command.executeMutation();
        String actual = cu.toString();
        String expectedStr = "public class Main {" +
                "public int bar() throws Exception {" +
                "return 1;" +
                "}" +
                "}";
        helper.parseDocStr(expectedStr);
        String expected = helper.getCompilationUnit().toString();
        assertEquals(expected, actual);
    }

    @Test
    public void canExecute_returnsDefaultDouble_canMutate() {

        String documentStr = "public class Main {" +
                "public double bar() throws Exception {" +
                "return 00.0000;" +
                "}" +
                "}";

        helper.parseDocStr(documentStr);
        CompilationUnit cu = helper.getCompilationUnit();
        MethodDeclaration methodDeclaration = (MethodDeclaration) helper.getBodyDeclarations().get(0);
        Block methodBody = (Block) methodDeclaration.getStructuralProperty(MethodDeclaration.BODY_PROPERTY);
        List<Statement> stmts = methodBody.statements();
        ReturnStatement returnStatement = (ReturnStatement) stmts.get(0);
        MutationReturnStmtCommand command = new MutationReturnStmtCommand(returnStatement);
        command.executeMutation();
        String actual = cu.toString();
        String expectedStr = "public class Main {" +
                "public double bar() throws Exception {" +
                "return 1.0;" +
                "}" +
                "}";
        helper.parseDocStr(expectedStr);
        String expected = helper.getCompilationUnit().toString();
        assertEquals(expected, actual);
    }
}
