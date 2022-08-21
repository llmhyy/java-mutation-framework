package jmutation.utils;

import jmutation.model.ExecutionResult;
import jmutation.model.Project;
import jmutation.model.TestCase;
import jmutation.model.TestIO;
import jmutation.model.ast.ASTNodeParentRetriever;
import jmutation.model.ast.ASTNodeRetriever;
import jmutation.mutation.commands.MutationCommand;
import jmutation.parser.ProjectParser;
import microbat.model.BreakPoint;
import microbat.model.trace.Trace;
import microbat.model.trace.TraceNode;
import microbat.model.value.ReferenceValue;
import microbat.model.value.VarValue;
import microbat.model.variable.ArrayElementVar;
import microbat.model.variable.FieldVar;
import microbat.model.variable.LocalVar;
import microbat.model.variable.Variable;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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

    public static List<TestIO> getTestInputOutputs(ExecutionResult mutatedInstrumentationResult, ExecutionResult instrumentationResultWithAssertions, TestCase testCase, Project project) {
        Trace trace = mutatedInstrumentationResult.getTrace();
        Trace traceWithAssertion = instrumentationResultWithAssertions.getTrace();
        List<TraceNode> executionList = trace.getExecutionList();
        List<TraceNode> executionListWithAssertions = traceWithAssertion.getExecutionList();
        List<TestIO> result = new ArrayList<>();
        if (executionList.isEmpty()) {
            return result;
        }
        String testCaseName = testCase.qualifiedName();
        Map<String, VarValue> primitiveVarToValMap = new HashMap<>(); // Store the inputs
        Map<String, List<VarValue>> referenceVarToValMap = new HashMap<>(); // Store the inputs
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
                // Output sometimes wrongly added as input if the output assignment line different from assertion call
                String inputKey = formKeyForInputMap(output.getVariable());
                if (primitiveVarToValMap.containsKey(inputKey)) {
                    primitiveVarToValMap.remove(inputKey);
                }
                List<VarValue> inputs = getInputs(executionList, traceNode, referenceVarToValMap, primitiveVarToValMap, project);
                TestIO testIO = new TestIO(inputs, output);
                result.add(testIO);
                continue;
            }
            String fullMethodName = breakPoint.getDeclaringCompilationUnitName() + "#" + currentMethodName;
            // Only take written vars in the test case (top layer) as inputs, not inner method calls
            // e.g. int f = 1; int x = funcCall(2,3); Store 1, 2, 3 and written val to x
            // Store the input values if not yet declared, otherwise, update the value
            if (fullMethodName.equals(testCaseName)) {
                setVarVals(primitiveVarToValMap, referenceVarToValMap, traceNode);
            }
        }
        // If crashed, obtain the last read/written var
        if (mutatedInstrumentationResult.hasThrownException()) {
            int idx = executionList.size() - 1;
            while (idx >= 0) {
                TraceNode current = executionList.get(idx);
                List<VarValue> varValues = new ArrayList<>(current.getWrittenVariables());
                varValues.addAll(current.getReadVariables());
                String stringValOfOutput = mutatedInstrumentationResult.getInstrumentationResult().getProgramMsg();
                stringValOfOutput = stringValOfOutput.substring(stringValOfOutput.indexOf(';') + 1);
                for (VarValue varValue : varValues) {
                    // Array element, index out of bounds, can use varID to check idx.
                    // e.g. varID = 1365008457[-1], check if [-1] inside varID
                    boolean shouldSkip = true;
                    if (!stringValOfOutput.equals(varValue.getStringValue())) {
                        shouldSkip = false;
                    } else if (varValue instanceof ReferenceValue) {
                        Variable var = varValue.getVariable();
                        if (var instanceof ArrayElementVar) {
                            if (var.getVarID().contains("[" + stringValOfOutput + "]")) {
                                shouldSkip = false;
                            }
                        }
                    }

                    if (shouldSkip) {
                        continue;
                    }

                    List<VarValue> inputs = new ArrayList<>();
                    inputs.addAll(primitiveVarToValMap.values());
                    for (Entry<String, List<VarValue>> entry : referenceVarToValMap.entrySet()) {
                        inputs.addAll(entry.getValue());
                    }
                    TestIO testIO = new TestIO(inputs, varValue);
                    result.add(testIO);
                    return result;
                }
                idx--;
            }
        }
        return result;
    }

    public static List<TestIO> getTestInputOutputs1(ExecutionResult mutatedInstrumentationResult, ExecutionResult instrumentationResultWithAssertions, TestCase testCase, Project project) {
        Trace trace = mutatedInstrumentationResult.getTrace();
        Trace traceWithAssertion = instrumentationResultWithAssertions.getTrace();
        List<TraceNode> executionList = trace.getExecutionList();
        List<TraceNode> executionListWithAssertions = traceWithAssertion.getExecutionList();
        List<TestIO> result = new ArrayList<>();
        if (executionList.isEmpty()) {
            return result;
        }
        String testCaseName = testCase.qualifiedName();
        Map<String, VarValue> primitiveVarToValMap = new HashMap<>(); // Store the inputs
        Map<String, List<VarValue>> referenceVarToValMap = new HashMap<>(); // Store the inputs
        for (int i = 0; i < executionList.size(); i++) {
            TraceNode traceNode = executionList.get(i);
            TraceNode traceNodeWithAssertion = executionListWithAssertions.get(i);
            BreakPoint breakPoint = traceNode.getBreakPoint();
            String currentMethodName = breakPoint.getMethodName();
            if (currentMethodName.equals("<init>") || currentMethodName.equals("<clinit>")) {
                continue;
            }
            boolean shouldCallGetOutput = isOutputNode(traceNodeWithAssertion);
            String fullMethodName = breakPoint.getDeclaringCompilationUnitName() + "#" + currentMethodName;
            if (shouldCallGetOutput) {
                VarValue output = getOutput(traceNode, traceNodeWithAssertion);
                // Output sometimes wrongly added as input if the output assignment line different from assertion call
                String inputKey = formKeyForInputMap(output.getVariable());
                if (primitiveVarToValMap.containsKey(inputKey)) {
                    primitiveVarToValMap.remove(inputKey);
                }
                Set<VarValue> inputSet = getInputs1(output, traceNode, trace, new HashSet<>(), testCaseName);
                List<VarValue> inputs = new ArrayList<>(inputSet);
                TestIO testIO = new TestIO(inputs, output);
                result.add(testIO);
                continue;
            }
            // Only take written vars in the test case (top layer) as inputs, not inner method calls
            // e.g. int f = 1; int x = funcCall(2,3); Store 1, 2, 3 and written val to x
            // Store the input values if not yet declared, otherwise, update the value
            if (fullMethodName.equals(testCaseName)) {
                setVarVals(primitiveVarToValMap, referenceVarToValMap, traceNode);
            }
        }
        // If crashed, obtain the last read/written var
        if (mutatedInstrumentationResult.hasThrownException()) {
            int idx = executionList.size() - 1;
            while (idx >= 0) {
                TraceNode current = executionList.get(idx);
                List<VarValue> varValues = new ArrayList<>(current.getWrittenVariables());
                varValues.addAll(current.getReadVariables());
                String stringValOfOutput = mutatedInstrumentationResult.getInstrumentationResult().getProgramMsg();
                stringValOfOutput = stringValOfOutput.substring(stringValOfOutput.indexOf(';') + 1);
                for (VarValue varValue : varValues) {
                    // Array element, index out of bounds, can use varID to check idx.
                    // e.g. varID = 1365008457[-1], check if [-1] inside varID
                    boolean shouldSkip = true;
                    if (!stringValOfOutput.equals(varValue.getStringValue())) {
                        shouldSkip = false;
                    } else if (varValue instanceof ReferenceValue) {
                        Variable var = varValue.getVariable();
                        if (var instanceof ArrayElementVar) {
                            if (var.getVarID().contains("[" + stringValOfOutput + "]")) {
                                shouldSkip = false;
                            }
                        }
                    }

                    if (shouldSkip) {
                        continue;
                    }

                    List<VarValue> inputs = new ArrayList<>();
                    inputs.addAll(primitiveVarToValMap.values());
                    for (Entry<String, List<VarValue>> entry : referenceVarToValMap.entrySet()) {
                        inputs.addAll(entry.getValue());
                    }
                    TestIO testIO = new TestIO(inputs, varValue);
                    result.add(testIO);
                    return result;
                }
                idx--;
            }
        }
        return result;
    }

    private static Set<VarValue> getInputs1(VarValue output, TraceNode outputNode, Trace trace, Set<String> encounteredVars, String testClassName) {
        Set<VarValue> result = new HashSet<>();
        String varID = output.getVarID();
        if (encounteredVars.contains(varID)) {
            return result;
        }
        encounteredVars.add(varID);
        TraceNode dataDependency = trace.findDataDependency(outputNode, output);
        if (dataDependency == null || dataDependency.getReadVariables().isEmpty()) {
            if (!(outputNode.getDeclaringCompilationUnitName() + "#" + outputNode.getMethodName()).equals(testClassName)) {
                return result;
            }
            result.add(output);
            return result;
        }
        if (dataDependency.getWrittenVariables().contains(output) &&
                (dataDependency.getDeclaringCompilationUnitName() + "#" + dataDependency.getMethodName()).equals(testClassName)) {
            result.add(output);
        }
        for (VarValue readVarValue : dataDependency.getReadVariables()) {
            result.addAll(getInputs1(readVarValue, dataDependency, trace, encounteredVars, testClassName));
        }
        return result;
    }

    private static List<VarValue> getInputs(List<TraceNode> executionList, TraceNode node, Map<String, List<VarValue>> referenceVarToValMap, Map<String, VarValue> primitiveVarToValMap, Project project) {
        int[] startAndEndLineNums = getLineNumsForAssertion(node, project);
        List<TraceNode> assertionTraceNodes = getTraceNodesBetweenLineNums(executionList, node.getDeclaringCompilationUnitName(), startAndEndLineNums);
        List<VarValue> inputs = new ArrayList<>();
        Set<String> alreadyAddedInputs = new HashSet<>();
        for (TraceNode assertionTraceNode : assertionTraceNodes) {
           List<VarValue> varVals = new ArrayList<>();
           varVals.addAll(assertionTraceNode.getReadVariables());
           varVals.addAll(assertionTraceNode.getWrittenVariables());
           for (VarValue readVarVal : varVals) {
               Variable var = readVarVal.getVariable();
               if (readVarVal instanceof ReferenceValue) {
                   List<VarValue> newInputs = referenceVarToValMap.get(formKeyForInputMap(var));
                   if (newInputs == null) {
                       continue;
                   }
                   for (VarValue newInput : newInputs) {
                       String varID = newInput.getVarID();
                       if (alreadyAddedInputs.contains(varID)) {
                           continue;
                       }
                       alreadyAddedInputs.add(varID);
                       inputs.add(newInput);
                   }
               } else {
                   VarValue newInput = primitiveVarToValMap.get(formKeyForInputMap(var));
                   if (newInput == null) {
                       continue;
                   }
                   String varID = newInput.getVarID();
                   if (alreadyAddedInputs.contains(varID)) {
                       continue;
                   }
                   alreadyAddedInputs.add(varID);
                   inputs.add(newInput);
               }
           }
       }
       return inputs;
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
                // Sometimes the assertion spans multiple lines, the output var may be in a diff line from the assertion call.
                // Get step over previous node, until the output var is found.
                // e.g.
                // assertEquals(1,
                // 2);
                // 2nd line is called first before the assertion call at 1st line. Use stepOverPrevious from assertion call node to line 2.
                TraceNode current = node;
                while (current != null) {
                    List<VarValue> readVarVals = current.getReadVariables();
                    for (VarValue readVarVal : readVarVals) {
                        if (readVarVal.getStringValue().equals(varValue.getStringValue())) {
                            return readVarVal;
                        }
                    }
                    current = current.getStepOverPrevious();
                }
                // TODO:
                // Should not reach here, take some var val close to assertion's "actual" value so that output is not null.
                // If reference values are correctly stored in writtenVariables, should not reach here.
                // Remove once instrumentator fixed.
                current = node;
                while (current != null) {
                    List<VarValue> readVarVals = current.getReadVariables();
                    for (VarValue readVarVal : readVarVals) {
                        return readVarVal;
                    }
                    current = current.getStepOverPrevious();
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

    private static void setVarVals(Map<String, VarValue> primitiveVarToVal, Map<String, List<VarValue>> referenceVarToVal, TraceNode traceNode) {
        List<VarValue> varVals = new ArrayList<>();
        varVals.addAll(traceNode.getReadVariables());
        varVals.addAll(traceNode.getWrittenVariables());
        for (VarValue writtenVarVal : varVals) {
            Variable writtenVariable = writtenVarVal.getVariable();
            String key = formKeyForInputMap(writtenVariable);
            // Ignore objects e.g. Object x = constructor(); x is not user defined input
            if (writtenVarVal instanceof ReferenceValue) {
                if (shouldGetInputsForRefVal(traceNode)) {
                    List<VarValue> initializersForRefVal = new ArrayList<>();
                    if (referenceVarToVal.containsKey(key)) {
                        initializersForRefVal = referenceVarToVal.get(key);
                    }
                    initializersForRefVal.addAll(getInitializersForReferenceVal(traceNode, (ReferenceValue) writtenVarVal));
                    referenceVarToVal.put(key, initializersForRefVal);
                    continue;
                }
            }

            primitiveVarToVal.put(key, writtenVarVal);
        }
    }

    private static String formKeyForInputMap(Variable var) {
        // Location of var has to be appended as the location is not used in equals method for vars.
        // i.e. int x = 1; and func(int x);
        // Both x will be treated the same without location when they should be different inputs
        String varLocation;
        if (var instanceof LocalVar) {
            varLocation = ((LocalVar) var).getLocationClass();
        } else if (var instanceof FieldVar) {
            varLocation = ((FieldVar) var).getDeclaringType();
        } else {
            varLocation = "";
        }
        return var.toString() + varLocation;
    }

    private static List<VarValue> getInitializersForReferenceVal(TraceNode referenceValNode, ReferenceValue refValue) {
        TraceNode stepOverPrevious = referenceValNode.getStepOverPrevious();
        List<VarValue> result = new ArrayList<>();
        List<VarValue> fieldsInReferenceVal = refValue.getChildren();
        // The written vals could be arguments passed into the ref, however, not all are stored in the reference obj.
        // Use getChildren to obtain the fields in the obj, and check if the written val was written to some field in the obj.
        // If the obj stores the val, then add to the map.
        for (VarValue fieldVal : fieldsInReferenceVal) {
            for (VarValue writtenVal : stepOverPrevious.getWrittenVariables()) {
                if (fieldVal.getStringValue().equals(writtenVal.getStringValue())) {
                    result.add(writtenVal);
                }
            }
        }
        return result;
    }

    private static boolean shouldGetInputsForRefVal(TraceNode referenceValNode) {
        TraceNode stepOverPrevious = referenceValNode.getStepOverPrevious();
        if (stepOverPrevious == null) {
            return false;
        }
        if (!referenceValNode.getClassCanonicalName().equals(stepOverPrevious.getClassCanonicalName())) {
            return false;
        }
        if (referenceValNode.getLineNumber() != stepOverPrevious.getLineNumber()) {
            return false;
        }
        return true;
    }

    private static int[] getLineNumsForAssertion(TraceNode assertionTraceNode, Project project) {
        File file = ProjectParser.getFileOfClass(assertionTraceNode.getDeclaringCompilationUnitName(), project.getRoot());
        String fileContent;
        try {
            fileContent = Files.readString(file.toPath());
        } catch (IOException e) {
            throw new RuntimeException("Could not read file at " + file.toPath());
        }

        CompilationUnit unit = ProjectParser.parseCompilationUnit(fileContent);
        ASTNodeRetriever<MethodInvocation> methodInvocationASTNodeRetriever = new ASTNodeRetriever<>(MethodInvocation.class);
        unit.accept(methodInvocationASTNodeRetriever);
        int lineNumOfTraceNode = assertionTraceNode.getLineNumber();
        for (MethodInvocation mi : methodInvocationASTNodeRetriever.getNodes()) {
            String methodName = mi.getName().toString();
            if (!methodName.contains("assert")) {
                continue;
            }
            int startLine = unit.getLineNumber(mi.getStartPosition());
            int endLine = unit.getLineNumber(mi.getStartPosition() + mi.getLength() - 1);
            if (lineNumOfTraceNode < startLine || lineNumOfTraceNode > endLine) {
                continue;
            }
            return new int[] {startLine, endLine};
        }
        return null;
    }

    private static List<TraceNode> getTraceNodesBetweenLineNums(List<TraceNode> executionList, String assertionClassName, int[] startAndEndLineNums) {
        List<TraceNode> result = new ArrayList<>();
        for (TraceNode traceNode : executionList) {
            int lineNum = traceNode.getLineNumber();
            if (lineNum >= startAndEndLineNums[0] && lineNum <= startAndEndLineNums[1] && traceNode.getDeclaringCompilationUnitName().equals(assertionClassName)) {
                result.add(traceNode);
            }
        }
        return result;
    }
}
