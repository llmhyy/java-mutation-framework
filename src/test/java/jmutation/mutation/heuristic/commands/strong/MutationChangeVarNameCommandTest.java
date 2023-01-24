package jmutation.mutation.heuristic.commands.strong;

import jmutation.mutation.MutationTestHelper;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.WhileStatement;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MutationChangeVarNameCommandTest {
    MutationTestHelper helper = new MutationTestHelper();

    @Test
    public void canExecute_declaredVariable_cannotExecute() {
        String documentStr = "public class Main {" +
                "public static void main(String[] args) {" +
                "int a = 0;" +
                "while(a != 20) {" +
                "a++;" +
                "int c = b + a;" +
                "}" +
                "}" +
                "}";

        helper.parseDocStr(documentStr);
        MethodDeclaration methodDeclaration = (MethodDeclaration) helper.getBodyDeclarations().get(0);
        Block methodBody = (Block) methodDeclaration.getStructuralProperty(MethodDeclaration.BODY_PROPERTY);
        List<Statement> stmts = methodBody.statements();
        WhileStatement whileStmt = (WhileStatement) stmts.get(1);
        Block whileBody = (Block) whileStmt.getBody();
        List<Statement> whileBodyStmts = whileBody.statements();
        VariableDeclarationStatement cDeclaration = (VariableDeclarationStatement) whileBodyStmts.get(1);
        SimpleName cSimpleName = ((VariableDeclarationFragment) cDeclaration.fragments().get(0)).getName();
        MutationChangeVarNameCommand command = new MutationChangeVarNameCommand(cSimpleName);
        assertFalse(command.canExecute());
    }

    @Test
    public void executeMutation_usedVariable_mutatesCorrectly() {
        String documentStr = "public class Main {" +
                "public static void main(String[] args) {" +
                "int a = 0;" +
                "int b = 0;" +
                "while(a != 20) {" +
                "a++;" +
                "int c = b + a;" +
                "}" +
                "}" +
                "}";

        helper.parseDocStr(documentStr);
        MethodDeclaration methodDeclaration = (MethodDeclaration) helper.getBodyDeclarations().get(0);
        Block methodBody = (Block) methodDeclaration.getStructuralProperty(MethodDeclaration.BODY_PROPERTY);
        List<Statement> stmts = methodBody.statements();
        WhileStatement whileStmt = (WhileStatement) stmts.get(2);
        Block whileBody = (Block) whileStmt.getBody();
        List<Statement> whileBodyStmts = whileBody.statements();
        VariableDeclarationStatement cDeclaration = (VariableDeclarationStatement) whileBodyStmts.get(1);
        InfixExpression infixExp = (InfixExpression) ((VariableDeclarationFragment) cDeclaration.fragments().get(0)).getInitializer();
        SimpleName bSimpleName = (SimpleName) infixExp.getLeftOperand();
        MutationChangeVarNameCommand command = new MutationChangeVarNameCommand(bSimpleName);
        assertTrue(command.canExecute());
        command.executeMutation();
        String expectedDoc = String.join("\n", "public class Main {",
                "public static void main(String[] args) {",
                "int a = 0;",
                "int b = 0;",
                "while(a != 20) {",
                "a++;",
                "int c = a + a;",
                "}",
                "}",
                "}");
        helper.checkMutation(command, documentStr, expectedDoc);
    }

    @Test
    public void canExecute_noReplacements_cannotExecute() {
        String documentStr = "public class Main {" +
                "public static void main(String[] args) {" +
                "int b = 0;" +
                "while(b != 20) {" +
                "a++;" +
                "int c = b + b;" +
                "}" +
                "}" +
                "}";

        helper.parseDocStr(documentStr);
        MethodDeclaration methodDeclaration = (MethodDeclaration) helper.getBodyDeclarations().get(0);
        Block methodBody = (Block) methodDeclaration.getStructuralProperty(MethodDeclaration.BODY_PROPERTY);
        List<Statement> stmts = methodBody.statements();
        WhileStatement whileStmt = (WhileStatement) stmts.get(1);
        Block whileBody = (Block) whileStmt.getBody();
        List<Statement> whileBodyStmts = whileBody.statements();
        VariableDeclarationStatement cDeclaration = (VariableDeclarationStatement) whileBodyStmts.get(1);
        InfixExpression infixExp = (InfixExpression) ((VariableDeclarationFragment) cDeclaration.fragments().get(0)).getInitializer();
        SimpleName bSimpleName = (SimpleName) infixExp.getLeftOperand();
        MutationChangeVarNameCommand command = new MutationChangeVarNameCommand(bSimpleName);
        assertFalse(command.canExecute());
    }
}
