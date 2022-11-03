package jmutation.mutation.command.strong;

import jmutation.mutation.MutationTestHelper;
import jmutation.mutation.commands.strong.MutationIfCondToTrueCommand;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.junit.jupiter.api.Test;

import java.util.List;

public class MutationIfCondToTrueCommandTest {
    MutationTestHelper helper = new MutationTestHelper();

    @Test
    public void executeMutation_validIfStatement_mutatesCorrectly() {

        String documentStr = "public class Main {" +
                "public static void main(String[] args) {" +
                "int a = 0;" +
                "if(a != 20) {" +
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
        MutationIfCondToTrueCommand command = new MutationIfCondToTrueCommand(ifStmt);
        command.executeMutation();
        String expectedDoc = "public class Main {" +
                "public static void main(String[] args) {" +
                "int a = 0;" +
                "if(true) {" +
                "a++;" +
                "int c = b + a;" +
                "}" +
                "}" +
                "}";
        helper.checkMutation(command, documentStr, expectedDoc);
    }
}
