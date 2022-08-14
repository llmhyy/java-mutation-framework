package jmutation.mutation.command;

import jmutation.mutation.MutationTestHelper;
import jmutation.mutation.commands.MutationBlockRemovalCommand;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.WhileStatement;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MutationBlockRemovalCommandTest {
    MutationTestHelper helper = new MutationTestHelper();

    @Test
    public void executeMutation_validASTNode_mutatesCorrectly() {
        String documentStr = "public class Main {" +
                "public static void main(String[] args) {" +
                "int a = 0;" +
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
        WhileStatement whileStatement = (WhileStatement) methodStmts.get(1);
        Block whileStmtBody = (Block) whileStatement.getBody();
        MutationBlockRemovalCommand command = new MutationBlockRemovalCommand(whileStmtBody);
        command.executeMutation();
        String expectedDoc = "public class Main {" +
                "public static void main(String[] args) {" +
                "int a = 0;" +
                "while(a != 20) {" +
                "}" +
                "}" +
                "}";
        helper.parseDocStr(expectedDoc);

        assertEquals(helper.getCompilationUnit().toString(), cu.toString());
    }

    @Test
    public void executeMutation_containsAssignment_doesNotClearBlock() {
        String documentStr = "public class Main {" +
                "public static void main(String[] args) {" +
                "int a;" +
                "int b = 0;" +
                "while(a != 20) {" +
                "a = 0;" +
                "int c = 0;" +
                "}" +
                "}" +
                "}";

        helper.parseDocStr(documentStr);
        CompilationUnit cu = helper.getCompilationUnit();
        MethodDeclaration methodDeclaration = (MethodDeclaration) helper.getBodyDeclarations().get(0);
        Block methodBody = (Block) methodDeclaration.getStructuralProperty(MethodDeclaration.BODY_PROPERTY);
        List<Statement> methodStmts = methodBody.statements();
        WhileStatement whileStatement = (WhileStatement) methodStmts.get(2);
        Block whileStmtBody = (Block) whileStatement.getBody();
        MutationBlockRemovalCommand command = new MutationBlockRemovalCommand(whileStmtBody);
        command.executeMutation();
        helper.parseDocStr(documentStr);
        String expectedDoc = helper.getCompilationUnit().toString();
        assertEquals(expectedDoc, cu.toString());
    }

    @Test
    public void executeMutation_containsInnerBlock_doesNotClearBlock() {
        String documentStr = "public class Main {" +
                "public static void main(String[] args) {" +
                "int a;" +
                "int b = 0;" +
                "while(a != 20) {" +
                "a = 0;" +
                "int c = 0;" +
                "}" +
                "}" +
                "}";


        helper.parseDocStr(documentStr);
        CompilationUnit cu = helper.getCompilationUnit();
        MethodDeclaration methodDeclaration = (MethodDeclaration) helper.getBodyDeclarations().get(0);
        Block methodBody = (Block) methodDeclaration.getStructuralProperty(MethodDeclaration.BODY_PROPERTY);
        MutationBlockRemovalCommand command = new MutationBlockRemovalCommand(methodBody);

        command.executeMutation();
        helper.parseDocStr(documentStr);
        String expectedDoc = helper.getCompilationUnit().toString();
        assertEquals(expectedDoc, cu.toString());
    }
}
