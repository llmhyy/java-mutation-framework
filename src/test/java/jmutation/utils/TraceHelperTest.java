package jmutation.utils;

import jmutation.mutation.MutationCommand;
import jmutation.mutation.MutationTestHelper;
import jmutation.mutation.heuristic.commands.strong.MutationBlockRemovalCommand;
import microbat.model.BreakPoint;
import microbat.model.trace.Trace;
import microbat.model.trace.TraceNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

class TraceHelperTest {
    MutationTestHelper helper = new MutationTestHelper();

    @Test
    void getMutatedTraceNodes_validMutationCommand_findsCorrectTraceNode() {
        List<TraceNode> executionList = new ArrayList<>();
        String className = "main.Main";
        String methodSignature = "methodSignature";
        int lineNum = 1;
        BreakPoint breakPoint = new BreakPoint(className, methodSignature, lineNum);
        TraceNode traceNode = new TraceNode(breakPoint, null, 1, null, "");
        executionList.add(traceNode);
        lineNum = 3;
        breakPoint = new BreakPoint(className, methodSignature, lineNum);
        traceNode = new TraceNode(breakPoint, null, 1, null, "");
        executionList.add(traceNode);
        Trace trace = new Trace(null);
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
        Assertions.assertEquals(1, rootCauses.size());
        Assertions.assertEquals(traceNode, rootCauses.get(0));
    }
}
