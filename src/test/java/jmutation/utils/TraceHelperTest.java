package jmutation.utils;

import jmutation.MutationFramework;
import jmutation.model.ExecutionResult;
import jmutation.model.TestCase;
import jmutation.mutation.MutationTestHelper;
import jmutation.mutation.commands.MutationBlockRemovalCommand;
import jmutation.mutation.commands.MutationCommand;
import microbat.model.BreakPoint;
import microbat.model.trace.Trace;
import microbat.model.trace.TraceNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

public class TraceHelperTest {
    MutationTestHelper helper = new MutationTestHelper();
    @Test
    public void getMutatedTraceNodes_validMutationCommand_findsCorrectTraceNode() {
        String className = "main.Main";
        String methodSignature = "methodSignature";
        int lineNum = 1;
        BreakPoint breakPoint = new BreakPoint(className, methodSignature, lineNum);
        TraceNode traceNode = new TraceNode(breakPoint, null, 1, null, "");
        List<TraceNode> executionList = new ArrayList<>();
        executionList.add(traceNode);
        Trace trace = new Trace("1");
        trace.setExecutionList(executionList);

        String documentStr = String.join("\n", "package main;",
                "public class Main {",
                "public static void main(String[] args) {",
                "int a = 0;",
                "while(a != 20) {",
                "a++;",
                "int b = 0;",
                "}",
                "}",
                "}");
        helper.parseDocStr(documentStr);
        MethodDeclaration methodDeclaration = (MethodDeclaration) helper.getBodyDeclarations().get(0);
        Block methodBody = (Block) methodDeclaration.getStructuralProperty(MethodDeclaration.BODY_PROPERTY);
        MutationBlockRemovalCommand command = new MutationBlockRemovalCommand(methodBody);
        List<MutationCommand> mutationHistory = new ArrayList<>();
        mutationHistory.add(command);
        List<TraceNode> rootCauses = TraceHelper.getMutatedTraceNodes(trace, mutationHistory);
    }
}