package jmutation.mutation;

import jmutation.mutation.commands.MutationBlockRemovalCommand;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.junit.jupiter.api.Test;

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
                "int b = 0;" +
                "}" +
                "}" +
                "}";

        helper.parseDocStr(documentStr);
        MethodDeclaration methodDeclaration = (MethodDeclaration) helper.getBodyDeclarations().get(0);
        Block methodBody = (Block) methodDeclaration.getStructuralProperty(MethodDeclaration.BODY_PROPERTY);
        MutationBlockRemovalCommand command = new MutationBlockRemovalCommand(methodBody);
        command.executeMutation();
        String expectedDoc = "public static void main(String[] args){\n}\n";
        assertTrue(methodDeclaration.toString().equals(expectedDoc));
    }
}
