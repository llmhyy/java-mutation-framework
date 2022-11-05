package jmutation.mutation.heuristic.commands;

import jmutation.mutation.MutationTestHelper;
import jmutation.mutation.heuristic.commands.MutationForLoopToIfCommand;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.junit.jupiter.api.Test;

public class MutationForLoopToIfCommandTest {

    MutationTestHelper helper = new MutationTestHelper();

    @Test
    public void executeMutation_validASTNode_mutatesCorrectly() {
        String documentStr = "public class Main {" +
                "public static void main(String[] args) {" +
                "int a = 0;" +
                "for(int i = 0; i < a; i++) {" +
                "a++;" +
                "int b = 0;" +
                "}" +
                "}" +
                "}";
        helper.parseDocStr(documentStr);
        org.eclipse.jdt.core.dom.MethodDeclaration methodDeclaration = (MethodDeclaration) helper.getBodyDeclarations().get(0);
        Block methodBody = (Block) methodDeclaration.getStructuralProperty(MethodDeclaration.BODY_PROPERTY);
        ForStatement forStatement = (ForStatement) methodBody.statements().get(1);
        MutationForLoopToIfCommand mutationCmd = new MutationForLoopToIfCommand(forStatement);
        mutationCmd.executeMutation();
        String expectedDoc = "public class Main {" +
                "public static void main(String[] args) {" +
                "int a = 0;" +
                "if (true) {" +
                "int i = 0;" +
                "if(i < a) {" +
                "a++;" +
                "int b = 0;" +
                "}" +
                "}" +
                "}" +
                "}";
        helper.checkMutation(mutationCmd, documentStr, expectedDoc);
    }
}
