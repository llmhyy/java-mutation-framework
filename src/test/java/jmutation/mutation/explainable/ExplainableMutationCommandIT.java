package jmutation.mutation.explainable;

import jmutation.mutation.MutationTestHelper;
import jmutation.mutation.explainable.client.Client;
import jmutation.mutation.explainable.client.ExplainableMutationClient;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;

import static org.junit.jupiter.api.Assertions.*;

class ExplainableMutationCommandIT {
    private MutationTestHelper helper = new MutationTestHelper();
    @Test
    // Disabled since it requires the python server to be on
    @DisabledIf("jmutation.utils.MiscellaneousTestUtils#isRunningInGitHubActions")
    void executeMutation_GivenMethodDeclNode_CreatesMutatedNode() {
        Client client = new Client("127.0.0.1", 8080);
        client.connectServer();
        ExplainableMutationClient mutationClient = new ExplainableMutationClient(client);
        String methodSummary = "A method summary.";
        String methodBody = String.join(System.lineSeparator(),
        "public static void main(String[] args){" ,
                "  for (int i=0; i < 5; i++) {",
                "    a++;",
                "    a=0;" ,
                "    int b=0;" ,
                "  }" ,
                "}", "");
        String documentStr = String.join(System.lineSeparator(),
                "public class Main {" ,
                "/**" ,
                " * " + methodSummary ,
                " * @param args" ,
                " * @return" ,
                " */" ,
                methodBody,
                "}");

        helper.parseDocStr(documentStr);
        MethodDeclaration methodDeclaration = (MethodDeclaration) helper.getBodyDeclarations().get(0);
        ExplainableMutationCommand command = new ExplainableMutationCommand(methodDeclaration, mutationClient);
        MethodDeclaration node = (MethodDeclaration) command.executeMutation();
        assertEquals(methodBody, command.getOriginalMethod());
        assertEquals(methodSummary, command.getOriginalComment());
        assertNotEquals(methodSummary, command.getMutatedComment());
        assertNotEquals(methodBody, command.getMutatedMethod());
        assertNotNull(node);
    }
}
