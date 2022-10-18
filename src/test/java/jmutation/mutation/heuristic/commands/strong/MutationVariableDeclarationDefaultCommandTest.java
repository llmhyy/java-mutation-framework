package jmutation.mutation.heuristic.commands.strong;

import jmutation.mutation.MutationTestHelper;
import jmutation.mutation.heuristic.commands.strong.MutationVariableDeclarationDefaultCommand;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MutationVariableDeclarationDefaultCommandTest {

    MutationTestHelper helper = new MutationTestHelper();

    @Test
    public void executeMutation_validASTNode_mutatesCorrectly() {
        String documentStr = "public class Main {" +
                "public static void main(String[] args) {" +
                "int a = 2;" +
                "}" +
                "}";

        helper.parseDocStr(documentStr);
        CompilationUnit cu = helper.getCompilationUnit();
        MethodDeclaration methodDeclaration = (MethodDeclaration) helper.getBodyDeclarations().get(0);
        Block methodBody = (Block) methodDeclaration.getStructuralProperty(MethodDeclaration.BODY_PROPERTY);
        List<Statement> methodStmts = methodBody.statements();
        MutationVariableDeclarationDefaultCommand command = new MutationVariableDeclarationDefaultCommand(methodStmts.get(0));
        command.executeMutation();
        String expectedDoc = "public class Main {" +
                "public static void main(String[] args) {" +
                "int a = 0;" +
                "}" +
                "}";
        helper.parseDocStr(expectedDoc);

        assertEquals(helper.getCompilationUnit().toString(), cu.toString());
    }

    @Test
    public void executeMutation_defaultValue_mutatesCorrectly() {
        String documentStr = "public class Main {" +
                "public static void main(String[] args) {" +
                "int a = 0;" +
                "}" +
                "}";

        helper.parseDocStr(documentStr);
        CompilationUnit cu = helper.getCompilationUnit();
        MethodDeclaration methodDeclaration = (MethodDeclaration) helper.getBodyDeclarations().get(0);
        Block methodBody = (Block) methodDeclaration.getStructuralProperty(MethodDeclaration.BODY_PROPERTY);
        List<Statement> methodStmts = methodBody.statements();
        MutationVariableDeclarationDefaultCommand command = new MutationVariableDeclarationDefaultCommand(methodStmts.get(0));
        command.executeMutation();
        String expectedDoc = "public class Main {" +
                "public static void main(String[] args) {" +
                "int a = 1;" +
                "}" +
                "}";
        helper.parseDocStr(expectedDoc);

        assertEquals(helper.getCompilationUnit().toString(), cu.toString());
    }

    @Test
    public void executeMutation_validObjectAssignment_mutatesCorrectly() {
        String documentStr = "public class Main {" +
                "public static void main(String[] args) {" +
                "ClassName a = new ClassName();" +
                "}" +
                "}";

        helper.parseDocStr(documentStr);
        CompilationUnit cu = helper.getCompilationUnit();
        MethodDeclaration methodDeclaration = (MethodDeclaration) helper.getBodyDeclarations().get(0);
        Block methodBody = (Block) methodDeclaration.getStructuralProperty(MethodDeclaration.BODY_PROPERTY);
        List<Statement> methodStmts = methodBody.statements();
        MutationVariableDeclarationDefaultCommand command = new MutationVariableDeclarationDefaultCommand(methodStmts.get(0));
        command.executeMutation();
        String expectedDoc = "public class Main {" +
                "public static void main(String[] args) {" +
                "ClassName a = null;" +
                "}" +
                "}";
        helper.parseDocStr(expectedDoc);

        assertEquals(helper.getCompilationUnit().toString(), cu.toString());
    }

    @Test
    public void executeMutation_noDeclaration_mutatesCorrectly() {
        String documentStr = "public class Main {" +
                "public static void main(String[] args) {" +
                "int a;" +
                "}" +
                "}";

        helper.parseDocStr(documentStr);
        CompilationUnit cu = helper.getCompilationUnit();
        MethodDeclaration methodDeclaration = (MethodDeclaration) helper.getBodyDeclarations().get(0);
        Block methodBody = (Block) methodDeclaration.getStructuralProperty(MethodDeclaration.BODY_PROPERTY);
        List<Statement> methodStmts = methodBody.statements();
        MutationVariableDeclarationDefaultCommand command = new MutationVariableDeclarationDefaultCommand(methodStmts.get(0));
        command.executeMutation();
        String expectedDoc = "public class Main {" +
                "public static void main(String[] args) {" +
                "int a = 0;" +
                "}" +
                "}";
        helper.parseDocStr(expectedDoc);

        assertEquals(helper.getCompilationUnit().toString(), cu.toString());
    }
}
