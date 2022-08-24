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
        Set<TraceNode> pastOutputNodes = new HashSet<>();
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
//                Set<TraceNode> nodesToCheckForInput = getNodesToObtainInputs(trace, traceNode, pastOutputNodes);

                Set<VarValue> inputSet = getInputsFromDataDep(output, traceNode, trace, new HashSet<>(), testCaseName, pastOutputNodes, new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>());

                int[] startAndEndLineNums = getLineNumsForAssertion(traceNode, project);
                List<TraceNode> assertionTraceNodes = getTraceNodesBetweenLineNums(executionList, traceNode.getDeclaringCompilationUnitName(), startAndEndLineNums);
                pastOutputNodes.addAll(assertionTraceNodes);

                List<VarValue> inputs = new ArrayList<>(inputSet);
                // Data dep was broken, take inputs the greedy way. Go through trace and obtain all declared variables.
                if (inputs.contains(output)) {
                    inputs = getInputsFromTrace(executionList, traceNode, referenceVarToValMap, primitiveVarToValMap, project);
                }
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
                String programMsg = mutatedInstrumentationResult.getInstrumentationResult().getProgramMsg();
                String stringValOfOutput = programMsg.substring(programMsg.indexOf(';') + 1);
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


    /**
     * Gets inputs using data dependency.
     * Has issues obtaining user-defined inputs when data dependency is broken midway. e.g. when "return 0;" encountered.
     * @param output
     * @param outputNode
     * @param trace
     * @param encounteredVars
     * @param testClassName
     * @return
     */
    private static Set<VarValue> getInputsFromDataDep(VarValue output, TraceNode outputNode, Trace trace, Set<Integer> encounteredVars, String testClassName, Set<TraceNode> nodesToIgnore, List<List<VarValue>> tracker, List<VarValue> currentTrack, List<List<TraceNode>> nodesTracker, List<TraceNode> currentNodesTrack) {
        Set<VarValue> result = new HashSet<>();
        if (nodesToIgnore.contains(outputNode)) {
            return result;
        }
        int varID = System.identityHashCode(output);
        if (encounteredVars.contains(varID)) {
            return result;
        }
        encounteredVars.add(varID);
        TraceNode dataDependency = trace.findDataDependency(outputNode, output);
        if (dataDependency == null || dataDependency.getReadVariables().isEmpty()) {
            if (!(outputNode.getDeclaringCompilationUnitName() + "#" + outputNode.getMethodName()).equals(testClassName)) {
                return result;
            }
            List<VarValue> currentTrackCopy = new ArrayList<>(currentTrack);
            currentTrackCopy.add(output);
            tracker.add(currentTrackCopy);

            List<TraceNode> currentNodesTrackCopy = new ArrayList<>(currentNodesTrack);
            currentNodesTrackCopy.add(outputNode);
            nodesTracker.add(currentNodesTrackCopy);

            result.add(output);
            return result;
        }
        if (dataDependency.getWrittenVariables().contains(output) &&
                (dataDependency.getDeclaringCompilationUnitName() + "#" + dataDependency.getMethodName()).equals(testClassName) && !nodesToIgnore.contains(dataDependency)) {
            List<VarValue> currentTrackCopy = new ArrayList<>(currentTrack);
            currentTrackCopy.add(output);
            tracker.add(currentTrackCopy);
            List<TraceNode> currentNodesTrackCopy = new ArrayList<>(currentNodesTrack);
            currentNodesTrackCopy.add(outputNode);
            nodesTracker.add(currentNodesTrackCopy);

            result.add(output);
        }
        for (VarValue readVarValue : dataDependency.getReadVariables()) {
            List<VarValue> currentTrackCopy = new ArrayList<>(currentTrack);
            currentTrackCopy.add(readVarValue);

            List<TraceNode> currentNodesTrackCopy = new ArrayList<>(currentNodesTrack);
            currentNodesTrackCopy.add(outputNode);

            result.addAll(getInputsFromDataDep(readVarValue, dataDependency, trace, encounteredVars, testClassName, nodesToIgnore, tracker, currentTrackCopy, nodesTracker, currentNodesTrackCopy));
        }
        return result;
    }

    private static List<VarValue> getInputsFromTrace(List<TraceNode> executionList, TraceNode assertionNode, Map<String, List<VarValue>> referenceVarToValMap, Map<String, VarValue> primitiveVarToValMap, Project project) {
        int[] startAndEndLineNums = getLineNumsForAssertion(assertionNode, project);
        List<TraceNode> assertionTraceNodes = getTraceNodesBetweenLineNums(executionList, assertionNode.getDeclaringCompilationUnitName(), startAndEndLineNums);
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

    /**
     * This method is used to fix the error that the instrumentator failed
     * to recognized the write variable in method call statement
     * @param trace Trace of target program
     */
    private static void fillMethodCallVariables(Trace trace) {
        List<TraceNode> executionList = trace.getExecutionList();
        for (TraceNode node : executionList) {

            // We only focus on method call node
            if (node.getInvocationChildren().isEmpty()) {
                continue;
            }

            // We only focus on the case that instrumentator failed to recognized the writen variable
            if (!node.getReadVariables().isEmpty() && node.getWrittenVariables().isEmpty()) {

                // Deep copy the required parameters of the method call
                List<VarValue> parameters = new ArrayList<>();
                parameters.addAll(node.getReadVariables());

                // Try to find the read variable whose data dominator is out of scope of the method
                Set<Integer> childrenOrder = new HashSet<>();
                for (TraceNode childNode : node.getInvocationChildren()) {
                    childrenOrder.add(childNode.getOrder());

                    for (VarValue readVar : childNode.getReadVariables()) {
                        TraceNode dataDominator = trace.findDataDependency(childNode, readVar);
                        if (dataDominator != null) {
                            /*
                             * If the data dominator is out of scope, that mean
                             * it may be the parameter of the method.
                             */
                            if (!childrenOrder.contains(dataDominator.getOrder())) {

                                // We need to make sure that the variables are correspondence.
                                for (VarValue writeVar : dataDominator.getWrittenVariables()) {

                                    // Double check the data dependency relation
                                    if (!writeVar.getType().equals(readVar.getType())) {
                                        continue;
                                    }

                                    if (trace.findDataDependentee(dataDominator, writeVar).contains(childNode)) {
                                        for (int idx=0; idx<parameters.size(); idx++) {
                                            /*
                                             * If the data dominator is one of the parameter of method,
                                             * then add the variable into the method call.
                                             */
                                            VarValue parameter = parameters.get(idx);
                                            if (parameter.equals(writeVar)) {
                                                node.addWrittenVariable(readVar);
                                                parameters.remove(idx);
                                                break;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private static Set<TraceNode> getNodesToObtainInputs(Trace trace, TraceNode outputNode, Set<TraceNode> pastOutputNodes) {
        Set<TraceNode> result = new HashSet(trace.getExecutionList());
//        TraceNode currentNode;
//        boolean pastOutputNodeEncountered = false;
//        int currentOrder = outputNode.getOrder();
//        while (currentOrder >= 1) {
//            currentNode = trace.getTraceNode(currentOrder);
//            if (pastOutputNodes.contains(currentNode)) {
//                pastOutputNodeEncountered = true;
//            }
//            if (!pastOutputNodeEncountered) {
//                result.add(currentNode);
//                currentOrder--;
//                continue;
//            }
//            if (currentNode.getInvocationParent() == null) {
//                result.add(currentNode);
//            }
//            currentOrder--;

//            TraceNode parentNode = currentNode.getInvocationParent();
//            List<TraceNode> sameLayerNodes;
//            if (parentNode == null) {
//                sameLayerNodes = new ArrayList<>();
//                for (TraceNode node : trace.getExecutionList()) {
//                    if (node.getInvocationParent() == null) {
//                        sameLayerNodes.add(node);
//                    }
//                }
//            } else {
//                sameLayerNodes = parentNode.getInvocationChildren();
//            }
//            for (TraceNode sameLayerNode : sameLayerNodes) {
//                result.add(sameLayerNode);
//                if (sameLayerNode.equals(currentNode)) {
//                    break;
//                }
//            }
//            currentNode = currentNode.getInvocationParent();
//        }
        return result;
    }
}
