package jmutation.mutation.command;

import jmutation.mutation.MutationTestHelper;
import jmutation.mutation.commands.MutationMathLibCommand;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;

public class MutationMathLibCommandTest {

    MutationTestHelper helper = new MutationTestHelper();

    @Test
    public void executeMutation_validASTNode_mutatesCorrectly() {
        String documentStr = "import java.lang.Math;" +
                "public class Main {" +
                "public static void main(String[] args) {" +
                "double a = Math.pow(1,2);" +
                "while(a != 20) {" +
                "a++;" +
                "a = 0;" +
                "int b = 0;" +
                "}" +
                "}" +
                "}";

        helper.parseDocStr(documentStr);
        CompilationUnit cu = helper.getCompilationUnit();
        MethodDeclaration methodDeclaration = (MethodDeclaration) helper.getBodyDeclarations().get(0);
        Block methodBody = (Block) methodDeclaration.getStructuralProperty(MethodDeclaration.BODY_PROPERTY);
        List<Statement> methodStmts = methodBody.statements();
        VariableDeclarationStatement varDecStmt = (VariableDeclarationStatement) methodStmts.get(0);
        VariableDeclarationFragment frag = (VariableDeclarationFragment) varDecStmt.fragments().get(0);
        MethodInvocation mathPowInvocation = (MethodInvocation) frag.getInitializer();
        MutationMathLibCommand command = new MutationMathLibCommand(mathPowInvocation);
        command.executeMutation();
        String expectedDoc = "import java.lang.Math;" +
                "public class Main {" +
                "public static void main(String[] args) {" +
                "double a = Math.addExact(1,2);" +
                "while(a != 20) {" +
                "a++;" +
                "a = 0;" +
                "int b = 0;" +
                "}" +
                "}" +
                "}";
        helper.parseDocStr(expectedDoc);

        helper.checkMutation(command, documentStr, expectedDoc);
    }

    @Test
    public void canExecute_noMathImport_doesNotClearBlock() {
        String documentStr = "public class Main {" +
                "public static void main(String[] args) {" +
                "int a = Math.pow(1,2);" +
                "while(a != 20) {" +
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
        VariableDeclarationStatement varDecStmt = (VariableDeclarationStatement) methodStmts.get(0);
        VariableDeclarationFragment frag = (VariableDeclarationFragment) varDecStmt.fragments().get(0);
        MethodInvocation mathPowInvocation = (MethodInvocation) frag.getInitializer();
        MutationMathLibCommand command = new MutationMathLibCommand(mathPowInvocation);
        assertFalse(command.canExecute());
    }

    @Test
    public void canExecute_noMathMethodInvoked_doesNotClearBlock() {
        String documentStr = "import java.lang.Math;" +
                "public class Main {" +
                "public static void main(String[] args) {" +
                "int a = foo();" +
                "while(a != 20) {" +
                "a = 0;" +
                "int c = 0;" +
                "}" +
                "}" +
                "}";


        helper.parseDocStr(documentStr);
        MethodDeclaration methodDeclaration = (MethodDeclaration) helper.getBodyDeclarations().get(0);
        Block methodBody = (Block) methodDeclaration.getStructuralProperty(MethodDeclaration.BODY_PROPERTY);
        List<Statement> methodStmts = methodBody.statements();
        VariableDeclarationStatement varDecStmt = (VariableDeclarationStatement) methodStmts.get(0);
        VariableDeclarationFragment frag = (VariableDeclarationFragment) varDecStmt.fragments().get(0);
        MethodInvocation mathPowInvocation = (MethodInvocation) frag.getInitializer();
        MutationMathLibCommand command = new MutationMathLibCommand(mathPowInvocation);
        assertFalse(command.canExecute());
    }

}
