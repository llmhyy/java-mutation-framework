package jmutation.mutation.heuristic.parser;

import jmutation.mutation.MutationCommand;
import jmutation.mutation.MutationTestHelper;
import jmutation.mutation.heuristic.commands.strong.MutationBlockRemovalCommand;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class StrongMutationParserTest {

    MutationTestHelper helper = new MutationTestHelper();

    @Test
    void createMutationCommand_blockNode_createsCommand() {
        String documentStr = "public class Main {" +
                "public static void main(String[] args) {" +
                "int a = 0;" +
                "}" +
                "}";

        helper.parseDocStr(documentStr);
        MethodDeclaration methodDeclaration = (MethodDeclaration) helper.getBodyDeclarations().get(0);
        ASTNode methodBody = (ASTNode) methodDeclaration.getStructuralProperty(MethodDeclaration.BODY_PROPERTY);
        MutationParser parser = new StrongMutationParser();
        List<MutationCommand> commands = parser.parse(methodBody);
        assertTrue(commands.get(0) instanceof MutationBlockRemovalCommand);
    }
}
