package jmutation.utils;

import jmutation.model.TestCase;
import jmutation.model.TestIO;
import jmutation.model.ast.ASTNodeRetriever;
import jmutation.mutation.commands.MutationCommand;
import microbat.model.BreakPoint;
import microbat.model.trace.Trace;
import microbat.model.trace.TraceNode;
import microbat.model.value.VarValue;
import microbat.model.variable.FieldVar;
import microbat.model.variable.LocalVar;
import microbat.model.variable.Variable;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class TraceHelper {
    /**
     * Gets TraceNodes that matches the mutation history i.e. root cause of bugs
     * @param buggyTrace
     * @param mutationHistory
     * @return list of TraceNodes
     */
    public static List<TraceNode> getMutatedTraceNodes(Trace buggyTrace, List<MutationCommand> mutationHistory) {
        List<TraceNode> result = new ArrayList<>();
        List<TraceNode> executionList = buggyTrace.getExecutionList();
        for (MutationCommand mutationCommand: mutationHistory) {
            ASTNode node = mutationCommand.getNode();
            CompilationUnit unit = (CompilationUnit) node.getRoot();
            PackageDeclaration packageDeclaration = unit.getPackage();
            TypeDeclaration typeDeclaration = (TypeDeclaration) unit.types().get(0);
            String mutatedClassName = typeDeclaration.getName().getFullyQualifiedName();
            String fullMutatedClassName = packageDeclaration == null ? mutatedClassName : packageDeclaration.getName() + "." + mutatedClassName;
            int startPos = unit.getLineNumber(node.getStartPosition());
            int endPos = unit.getLineNumber(node.getStartPosition() + node.getLength() - 1);
            for (TraceNode traceNode: executionList) {
                BreakPoint breakPoint = traceNode.getBreakPoint();
                int lineNum = breakPoint.getLineNumber();
                String classCanonicalName = breakPoint.getDeclaringCompilationUnitName();
                if (classCanonicalName.equals(fullMutatedClassName) && lineNum <= endPos && lineNum >= startPos) {
                   result.add(traceNode);
                }
            }
        }
        return result;
    }

    public static List<TestIO> getTestInputOutputs(Trace trace, TestCase testCase) {
        Set<Integer> assertionLineNums = getAssertionLineNums(testCase);
        List<TraceNode> executionList = trace.getExecutionList();
        List<TestIO> result = new ArrayList<>();
        String testCaseName = testCase.qualifiedName();
        Map<String, VarValue> varToValMap = new HashMap<>(); // Store the inputs
        for (TraceNode traceNode : executionList) {
            BreakPoint breakPoint = traceNode.getBreakPoint();
            String currentMethodName = breakPoint.getMethodName();
            if (currentMethodName.equals("<init>") || currentMethodName.equals("<clinit>")) {
                continue;
            }
            boolean shouldCallGetOutput = isOutputNode(traceNode, testCase, assertionLineNums);
            if (shouldCallGetOutput) {
                List<VarValue> output = getOutput(traceNode, testCase);
                List<VarValue> inputs = new ArrayList<>();
                inputs.addAll(varToValMap.values());
                TestIO testIO = new TestIO(inputs, output);
                result.add(testIO);
                continue;
            }
            String fullMethodName = breakPoint.getDeclaringCompilationUnitName() + "#" + currentMethodName;
            List<VarValue> writtenVarVals = traceNode.getWrittenVariables();
            // Only take written vars in the test case (top layer) as inputs, not inner method calls
            // e.g. int f = 1; int x = funcCall(2,3); Store 1, 2, 3 and written val to x
            // Store the input values if not yet declared, otherwise, update the value
            if (fullMethodName.equals(testCaseName)) {
               for (VarValue writtenVarVal : writtenVarVals) {
                   Variable writtenVariable = writtenVarVal.getVariable();
                   // Location of var has to be appended as the location is not used in equals method for vars.
                   // i.e. int x = 1; and func(int x);
                   // Both x will be treated the same without location when they should be different inputs
                   String varLocation;
                   if (writtenVariable instanceof LocalVar) {
                       varLocation = ((LocalVar) writtenVariable).getLocationClass();
                   } else if (writtenVariable instanceof FieldVar) {
                       varLocation = ((FieldVar) writtenVariable).getDeclaringType();
                   } else {
                       varLocation = "";
                   }
                   varToValMap.put(writtenVariable.toString() + varLocation, writtenVarVal);
               }
            }
        }
        return result;
    }

    private static Set<Integer> getAssertionLineNums(TestCase testCase) {
        MethodDeclaration methodDeclaration = testCase.mtd;
        CompilationUnit compilationUnit = (CompilationUnit) methodDeclaration.getRoot();
        ASTNodeRetriever<MethodInvocation> methodInvocationASTNodeRetriever = new ASTNodeRetriever<>(MethodInvocation.class);
        methodDeclaration.accept(methodInvocationASTNodeRetriever);
        List<MethodInvocation> methodInvocations = methodInvocationASTNodeRetriever.getNodes();
        Set<Integer> result = new HashSet<>();
        for (MethodInvocation methodInvocation : methodInvocations) {
            String methodName = methodInvocation.getName().toString();
            if (methodName.toLowerCase(Locale.ROOT).contains("assert")) {
                int lineNum = compilationUnit.getLineNumber(methodInvocation.getStartPosition());
                result.add(lineNum);
            }
        }
        return result;
    }

    /**
     * Returns the output of a test case (assertion)
     * Caller must implement logic to know when to call this method. i.e. isOutputNode method
     * @param node
     * @param testCase
     * @return
     */
    private static List<VarValue> getOutput(TraceNode node, TestCase testCase) {
        String testCaseName = testCase.qualifiedName();
        TraceNode stepInPrev = node.getStepInPrevious();
        BreakPoint breakPoint = stepInPrev.getBreakPoint();
        String fullMethodName = breakPoint.getDeclaringCompilationUnitName() + "#" + breakPoint.getMethodName();
        // No method calls inside the assertion, just take the read variables from the assertion
        // e.g. assertEquals(2, x); Get read var x
        if (testCaseName.equals(fullMethodName)) {
            return node.getReadVariables();
        }
        // Method call inside assertion, take written variable just before inner func call returns to the assertion
        // e.g. assertEquals(2, funcCall(5)); Take the written var inside funcCall(5)
        return stepInPrev.getWrittenVariables();
    }

    /**
     * Checks if trace node should be used to obtain test output i.e. whether to call getOutput method
     * Current node is top layer, stepInNext is top layer, stepOverPrevious is top layer, line num is assertion
     * @param node
     * @param testCase
     * @param assertionLineNums
     * @return
     */
    private static boolean isOutputNode(TraceNode node, TestCase testCase, Set<Integer> assertionLineNums) {
        int lineNum = node.getLineNumber();
        if (!assertionLineNums.contains(lineNum)) {
            return false;
        }
        String testCaseName = testCase.qualifiedName();
        BreakPoint breakPoint = node.getBreakPoint();
        String fullMethodName = breakPoint.getDeclaringCompilationUnitName() + "#" + breakPoint.getMethodName();
        if (!testCaseName.equals(fullMethodName)) {
            return false;
        }
        TraceNode stepInNext = node.getStepInNext();
        if (stepInNext != null) {
            breakPoint = stepInNext.getBreakPoint();
            fullMethodName = breakPoint.getDeclaringCompilationUnitName() + "#" + breakPoint.getMethodName();
            if (!testCaseName.equals(fullMethodName)) {
                return false;
            }
        }
        TraceNode stepOverPrev = node.getStepOverPrevious();
        if (stepOverPrev != null) {
            breakPoint = stepOverPrev.getBreakPoint();
            fullMethodName = breakPoint.getDeclaringCompilationUnitName() + "#" + breakPoint.getMethodName();
            if (!testCaseName.equals(fullMethodName)) {
                return false;
            }
        }
        return true;
    }
}
