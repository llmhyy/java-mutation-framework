package jmutation.utils;

import jmutation.model.TestCase;
import jmutation.model.TestIO;
import jmutation.model.ast.ASTNodeParentRetriever;
import jmutation.mutation.commands.MutationCommand;
import microbat.model.BreakPoint;
import microbat.model.trace.Trace;
import microbat.model.trace.TraceNode;
import microbat.model.value.ArrayValue;
import microbat.model.value.ReferenceValue;
import microbat.model.value.VarValue;
import microbat.model.variable.FieldVar;
import microbat.model.variable.LocalVar;
import microbat.model.variable.Variable;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
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
        Set<TraceNode> result = new HashSet<>();
        List<TraceNode> executionList = buggyTrace.getExecutionList();
        for (MutationCommand mutationCommand: mutationHistory) {
            ASTNode node = mutationCommand.getNode();
            ASTNode root = node.getRoot();
            if (!(root instanceof CompilationUnit)) {
                // If the ASTNode in mutation history is no longer part of a class, it must have been overwritten by another mutation.
                // No need to look for corresponding TraceNode.
                continue;
            }
            ASTNodeParentRetriever<TypeDeclaration> typeDeclarationASTNodeParentRetriever = new ASTNodeParentRetriever<>(TypeDeclaration.class);
            TypeDeclaration typeDeclaration = typeDeclarationASTNodeParentRetriever.getParentOfType(node);
            CompilationUnit unit = (CompilationUnit) root;
            PackageDeclaration packageDeclaration = unit.getPackage();
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

        return new ArrayList<>(result);
    }

    public static List<TestIO> getTestInputOutputs(Trace trace, Trace traceWithAssertion, TestCase testCase) {
        List<TraceNode> executionList = trace.getExecutionList();
        List<TraceNode> executionListWithAssertions = traceWithAssertion.getExecutionList();
        List<TestIO> result = new ArrayList<>();
        String testCaseName = testCase.qualifiedName();
        Map<String, VarValue> varToValMap = new HashMap<>(); // Store the inputs
        for (int i = 0; i < executionList.size(); i++) {
            TraceNode traceNode = executionList.get(i);
            TraceNode traceNodeWithAssertion = executionListWithAssertions.get(i);
            BreakPoint breakPoint = traceNode.getBreakPoint();
            String currentMethodName = breakPoint.getMethodName();
            if (currentMethodName.equals("<init>") || currentMethodName.equals("<clinit>")) {
                continue;
            }
            boolean shouldCallGetOutput = isOutputNode(traceNodeWithAssertion);
            if (shouldCallGetOutput) {
                VarValue output = getOutput(traceNode, traceNodeWithAssertion);
                List<VarValue> inputs = new ArrayList<>();
                inputs.addAll(varToValMap.values());
                TestIO testIO = new TestIO(inputs, output);
                result.add(testIO);
                continue;
            }
            String fullMethodName = breakPoint.getDeclaringCompilationUnitName() + "#" + currentMethodName;
            // Only take written vars in the test case (top layer) as inputs, not inner method calls
            // e.g. int f = 1; int x = funcCall(2,3); Store 1, 2, 3 and written val to x
            // Store the input values if not yet declared, otherwise, update the value
            if (fullMethodName.equals(testCaseName)) {
                setInputs(varToValMap, traceNode);
            }
        }
        return result;
    }

    /**
     * Returns the output of a test case (assertion)
     * Caller must implement logic to know when to call this method. i.e. isOutputNode method
     * @param node
     * @return
     */
    private static VarValue getOutput(TraceNode node, TraceNode traceNodeWithAssertion) {
        List<VarValue> writtenVarValues = traceNodeWithAssertion.getWrittenVariables();
        for (VarValue varValue : writtenVarValues) {
            Variable var = varValue.getVariable();
            if (getVarLocation(var).equals("org.junit.Assert") && varIsOutput(var)) {
                List<VarValue> readVarVals = node.getReadVariables();
                for (VarValue readVarVal : readVarVals) {
                    if (readVarVal.getStringValue().equals(varValue.getStringValue())) {
                        return readVarVal;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Checks if trace node should be used to obtain test output i.e. whether to call getOutput method
     * @param node
     * @return
     */
    private static boolean isOutputNode(TraceNode node) {
        List<VarValue> writtenVarValues = node.getWrittenVariables();
        for (VarValue varValue : writtenVarValues) {
            Variable var = varValue.getVariable();
            if (getVarLocation(var).equals("org.junit.Assert") && varIsOutput(var)) {
                return true;
            }
        }
        return false;
    }

    private static String getVarLocation(Variable var) {
        String varLocation;
        if (var instanceof LocalVar) {
            varLocation = ((LocalVar) var).getLocationClass();
        } else if (var instanceof FieldVar) {
            varLocation = ((FieldVar) var).getDeclaringType();
        } else {
            varLocation = "";
        }
        return varLocation;
    }

    private static boolean varIsOutput(Variable var) {
        String varName = var.getName();
        return varName.equals("actual") || varName.equals("actuals") || varName.equals("condition") || varName.equals("object");

    }

    private static void setInputs(Map<String, VarValue> varToVarValMap, TraceNode traceNode) {
        List<VarValue> writtenVarVals = traceNode.getWrittenVariables();
        writtenVarVals.addAll(traceNode.getReadVariables());
        for (VarValue writtenVarVal : writtenVarVals) {
            Variable writtenVariable = writtenVarVal.getVariable();
            // Ignore objects e.g. Object x = constructor(); x is not user defined input
            if (writtenVarVal instanceof ReferenceValue && !(writtenVarVal instanceof ArrayValue)) {
                continue;
            }
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
            varToVarValMap.put(writtenVariable.toString() + varLocation, writtenVarVal);
        }
    }

}
