package jmutation.mutation.heuristic.parser;

import jmutation.mutation.MutationCommand;
import jmutation.mutation.MutationTestHelper;
import jmutation.mutation.heuristic.commands.MutationMathLibCommand;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
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
        List<MutationCommand> commands = parser.parse(mathPowInvocation);
        assertTrue(commands.get(0) instanceof MutationMathLibCommand);
    }
}
