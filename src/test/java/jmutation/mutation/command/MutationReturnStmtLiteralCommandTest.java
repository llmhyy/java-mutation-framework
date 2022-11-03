package jmutation.mutation.command;

import jmutation.mutation.MutationTestHelper;
import jmutation.mutation.commands.MutationReturnStmtLiteralCommand;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.Statement;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;

public class MutationReturnStmtLiteralCommandTest {
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
        MutationReturnStmtLiteralCommand command = new MutationReturnStmtLiteralCommand(returnStatement);
        command.executeMutation();
        String expectedDoc = "public class Main {" +
                "public int bar() {" +
                "return 0;" +
                "}" +
                "}";

        helper.checkMutation(command, documentStr, expectedDoc);
    }

    @Test
    public void canExecute_returnStmtIsMethodInvocation_cannotMutate() {

        String documentStr = "public class Main {" +
                "public int bar() {" +
                "return foo();" +
                "}" +
                "public int foo() {" +
                "return 1;" +
                "}" +
                "}";

        helper.parseDocStr(documentStr);
        MethodDeclaration methodDeclaration = (MethodDeclaration) helper.getBodyDeclarations().get(0);
        Block methodBody = (Block) methodDeclaration.getStructuralProperty(MethodDeclaration.BODY_PROPERTY);
        List<Statement> stmts = methodBody.statements();
        ReturnStatement returnStatement = (ReturnStatement) stmts.get(0);
        MutationReturnStmtLiteralCommand command = new MutationReturnStmtLiteralCommand(returnStatement);
        assertFalse(command.canExecute());
    }

    @Test
    public void executeMutation_returnsDefaultInt_canMutate() {

        String documentStr = "public class Main {" +
                "public int bar() throws Exception {" +
                "return 0;" +
                "}" +
                "}";

        helper.parseDocStr(documentStr);
        MethodDeclaration methodDeclaration = (MethodDeclaration) helper.getBodyDeclarations().get(0);
        Block methodBody = (Block) methodDeclaration.getStructuralProperty(MethodDeclaration.BODY_PROPERTY);
        List<Statement> stmts = methodBody.statements();
        ReturnStatement returnStatement = (ReturnStatement) stmts.get(0);
        MutationReturnStmtLiteralCommand command = new MutationReturnStmtLiteralCommand(returnStatement);
        command.executeMutation();
        String expectedStr = "public class Main {" +
                "public int bar() throws Exception {" +
                "return 1;" +
                "}" +
                "}";
        helper.checkMutation(command, documentStr, expectedStr);
    }

    @Test
    public void canExecute_returnsDefaultDouble_canMutate() {

        String documentStr = "public class Main {" +
                "public double bar() throws Exception {" +
                "return 00.0000;" +
                "}" +
                "}";

        helper.parseDocStr(documentStr);
        MethodDeclaration methodDeclaration = (MethodDeclaration) helper.getBodyDeclarations().get(0);
        Block methodBody = (Block) methodDeclaration.getStructuralProperty(MethodDeclaration.BODY_PROPERTY);
        List<Statement> stmts = methodBody.statements();
        ReturnStatement returnStatement = (ReturnStatement) stmts.get(0);
        MutationReturnStmtLiteralCommand command = new MutationReturnStmtLiteralCommand(returnStatement);
        command.executeMutation();
        String expectedStr = "public class Main {" +
                "public double bar() throws Exception {" +
                "return 1.0;" +
                "}" +
                "}";
        helper.checkMutation(command, documentStr, expectedStr);
    }
}
