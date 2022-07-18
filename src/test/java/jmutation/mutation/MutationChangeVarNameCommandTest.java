package jmutation.mutation;

import jmutation.mutation.commands.MutationChangeVarNameCommand;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.WhileStatement;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class MutationChangeVarNameCommandTest {
    MutationTestHelper helper = new MutationTestHelper();

    @Test
    public void executeMutation_validASTNode_mutatesCorrectly() {
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
        SimpleName cSimpleName = ((VariableDeclarationFragment) cDeclaration.fragments().get(0)).getName();
        MutationChangeVarNameCommand command = new MutationChangeVarNameCommand(cSimpleName);
        command.executeMutation();
        String expectedDoc = "public static void main(String[] args){\n}\n";
        assertTrue(methodDeclaration.toString().equals(expectedDoc));
    }
}
