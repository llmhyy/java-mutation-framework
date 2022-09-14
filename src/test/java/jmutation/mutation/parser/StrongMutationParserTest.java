package jmutation.mutation.parser;

import jmutation.mutation.MutationTestHelper;
import jmutation.mutation.commands.MutationCommand;
import jmutation.mutation.commands.strong.MutationBlockRemovalCommand;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class StrongMutationParserTest {

    MutationTestHelper helper = new MutationTestHelper();

    @Test
    public void createMutationCommand_blockNode_createsCommand() {
        String documentStr = "public class Main {" +
                "public static void main(String[] args) {" +
                "int a = 0;" +
                "}" +
                "}";

        helper.parseDocStr(documentStr);
        MethodDeclaration methodDeclaration = (MethodDeclaration) helper.getBodyDeclarations().get(0);
        ASTNode methodBody = (ASTNode) methodDeclaration.getStructuralProperty(MethodDeclaration.BODY_PROPERTY);
        MutationParser parser = new StrongMutationParser();
        MutationCommand command = parser.parse(methodBody);
        assertTrue(command instanceof MutationBlockRemovalCommand);
    }
}
