package jmutation.mutation.explainable;

import jmutation.mutation.MutationTestHelper;
import org.eclipse.jdt.core.dom.*;
import org.junit.jupiter.api.Test;

import static jmutation.mutation.explainable.ExplainableMutationCommand.getMethodSummary;
import static org.junit.jupiter.api.Assertions.*;

class ExplainableMutationCommandTest {

    private MutationTestHelper helper = new MutationTestHelper();
    @Test
    void getMethodSummary_JavadocWithSummaryAndParamsAndReturn_ReturnsOnlySummary() {
        String methodSummary = "A method summary.";
        String documentStr = String.join(System.lineSeparator(),
                "public class Main {" ,
                "/**" ,
                " * " + methodSummary ,
                " * " + methodSummary ,
                " * @param args" ,
                " * @return" ,
                " */" ,
                "public static void main(String[] args) {" ,
                "for(int i = 0; i<5;i++) {" +
                "a,+;" +
                "a = 0;" ,
                "int b = 0;" ,
                "}" ,
                "}" ,
                "}");

        helper.parseDocStr(documentStr);
        MethodDeclaration methodDeclaration = (MethodDeclaration) helper.getBodyDeclarations().get(0);
        Javadoc javadoc = methodDeclaration.getJavadoc();
        String summary = getMethodSummary(javadoc);
        assertEquals(methodSummary + " " + methodSummary, summary);
    }

    @Test
    void parseIntoMethodDeclaration_GivenJavadocAndMethod_CreatesCorrectMethodDeclaration() {
        String methodBody = String.join(System.lineSeparator(), "public static void main(String[] args) {" ,
                "for(int i = 0; i<5;i++) {" +
                        "a++;" +
                        "a = 0;" ,
                "int b = 0;" ,
                "}" ,
                "}");
        String documentStr = String.join(System.lineSeparator(), "public class Clazz {",
                methodBody,
                "}");
        helper.parseDocStr(documentStr);
        MethodDeclaration methodDeclarationNode = (MethodDeclaration) helper.getBodyDeclarations().get(0);
        ExplainableMutationCommand command = new ExplainableMutationCommand(methodDeclarationNode, null);
        MethodDeclaration methodDeclaration = command.parseIntoMethodDeclaration("this is a javadoc", methodBody);
        String expectedResult = String.join(System.lineSeparator(), "/** ",
                " * this is a javadoc",
                " */",
                "public static void main(String[] args){",
                "  for (int i=0; i < 5; i++) {",
                "    a++;",
                "    a=0;",
                "    int b=0;",
                "  }",
                "}",
                ""
                );
        assertEquals(expectedResult, methodDeclaration.toString());
    }
}