package jmutation.mutation.parser;

import jmutation.mutation.MutationTestHelper;
import jmutation.mutation.commands.strong.MutationBlockRemovalCommand;
import jmutation.mutation.commands.MutationCommand;
import jmutation.mutation.commands.MutationMathLibCommand;
import org.eclipse.jdt.core.dom.*;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class MutationParserTest {
    MutationTestHelper helper = new MutationTestHelper();

    @Test
    public void createMutationCommand_mathMethodInvocation_createsCommand() {
        String documentStr = "import java.lang.Math;" +
                "public class Main {" +
                "public static void main(String[] args) {" +
                "int a = Math.pow(1,2);" +
                "}" +
                "}";

        helper.parseDocStr(documentStr);
        CompilationUnit cu = helper.getCompilationUnit();
        MethodDeclaration methodDeclaration = (MethodDeclaration) helper.getBodyDeclarations().get(0);
        Block methodBody = (Block) methodDeclaration.getStructuralProperty(MethodDeclaration.BODY_PROPERTY);
        List<Statement> methodStmts = methodBody.statements();
        VariableDeclarationStatement varDecStmt = (VariableDeclarationStatement) methodStmts.get(0);
        VariableDeclarationFragment frag = (VariableDeclarationFragment) varDecStmt.fragments().get(0);
        ASTNode mathPowInvocation = frag.getInitializer();
        MutationParser parser = new MutationParser();
        MutationCommand command = parser.parse(mathPowInvocation);
        assertTrue(command instanceof MutationMathLibCommand);
    }
}
