package jmutation.mutation.command;

import jmutation.mutation.MutationTestHelper;
import jmutation.mutation.commands.MutationWhileLoopToIfCommand;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.WhileStatement;
import org.junit.jupiter.api.Test;

public class MutationWhileLoopToIfCommandTest {
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
        org.eclipse.jdt.core.dom.MethodDeclaration methodDeclaration = (MethodDeclaration) helper.getBodyDeclarations().get(0);
        Block methodBody = (Block) methodDeclaration.getStructuralProperty(MethodDeclaration.BODY_PROPERTY);
        WhileStatement whileStatement = (WhileStatement) methodBody.statements().get(1);
        MutationWhileLoopToIfCommand mutationCmd = new MutationWhileLoopToIfCommand(whileStatement);
        mutationCmd.executeMutation();
        String expectedDoc = String.join("\n",
                "public class Main {",
                "public static void main(String[] args){",
                "  int a=0;",
                "  if (a != 20) {",
                "    a++;",
                "    int b=0;",
                "  }",
                "}",
                "}"
        );
        helper.checkMutation(mutationCmd, documentStr, expectedDoc);
    }
}
