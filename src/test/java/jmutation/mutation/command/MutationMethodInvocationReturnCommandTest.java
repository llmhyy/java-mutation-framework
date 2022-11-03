package jmutation.mutation.command;

import jmutation.mutation.MutationTestHelper;
import jmutation.mutation.commands.MutationMethodInvocationReturnCommand;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.List;

public class MutationMethodInvocationReturnCommandTest {
    MutationTestHelper helper = new MutationTestHelper();

    @Test
    public void executeMutation_validMethodInvocationInVarDeclaration_mutatesCorrectly() {

        String documentStr = "public class Main {" +
                "public void foo() {" +
                "int a = bar();" +
                "int c = a + a;" +
                "}" +
                "public int bar() {" +
                "return 1;" +
                "}" +
                "}";

        helper.parseDocStr(documentStr);
        MethodDeclaration methodDeclaration = (MethodDeclaration) helper.getBodyDeclarations().get(0);
        Block methodBody = (Block) methodDeclaration.getStructuralProperty(MethodDeclaration.BODY_PROPERTY);
        List<Statement> stmts = methodBody.statements();
        VariableDeclarationStatement stmt = (VariableDeclarationStatement) stmts.get(0);
        MethodInvocation methodInvocation = (MethodInvocation) ((VariableDeclarationFragment) stmt.fragments().get(0)).getInitializer();
        MutationMethodInvocationReturnCommand command = new MutationMethodInvocationReturnCommand(methodInvocation);
        command.executeMutation();
        String expectedDoc = "public class Main {" +
                "public void foo() {" +
                "int a = 0;" +
                "int c = a + a;" +
                "}" +
                "public int bar() {" +
                "return 1;" +
                "}" +
                "}";
        helper.checkMutation(command, documentStr, expectedDoc);
    }

    @Disabled("Disabled until return type for MethodInvocation can be obtained efficiently w/o bindings on ASTParser")
    public void executeMutation_validMethodInvocationInFuncCall_mutatesCorrectly() {

        String documentStr = "public class Main {" +
                "public void foo() {" +
                "int a = hello(bar());" +
                "int c = a + a;" +
                "}" +
                "public int bar() {" +
                "return 1;" +
                "}" +
                "public char hello(int a) {" +
                "return a;" +
                "}" +
                "}";

        helper.parseDocStr(documentStr);
        MethodDeclaration methodDeclaration = (MethodDeclaration) helper.getBodyDeclarations().get(0);
        Block methodBody = (Block) methodDeclaration.getStructuralProperty(MethodDeclaration.BODY_PROPERTY);
        List<Statement> stmts = methodBody.statements();
        VariableDeclarationStatement stmt = (VariableDeclarationStatement) stmts.get(0);
        MethodInvocation methodInvocation = (MethodInvocation) ((VariableDeclarationFragment) stmt.fragments().get(0)).getInitializer();
        MethodInvocation innerMethodInvocation = (MethodInvocation) methodInvocation.arguments().get(0);
        MutationMethodInvocationReturnCommand command = new MutationMethodInvocationReturnCommand(innerMethodInvocation);
        command.executeMutation();
        String expectedDoc = "public class Main {" +
                "public void foo() {" +
                "int a = hello(0);" +
                "int c = a + a;" +
                "}" +
                "public int bar() {" +
                "return 1;" +
                "}" +
                "public char hello(int a) {" +
                "return a;" +
                "}" +
                "}";
        helper.checkMutation(command, documentStr, expectedDoc);
    }
}
