package jmutation.utils;

import jmutation.model.TestCase;
import jmutation.model.TestIO;
import jmutation.mutation.commands.MutationCommand;
import microbat.model.BreakPoint;
import microbat.model.trace.Trace;
import microbat.model.trace.TraceNode;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import java.util.ArrayList;
import java.util.List;

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
       List<TraceNode> executionList = trace.getExecutionList();
       List<TestIO> result = new ArrayList<>();
       String testCaseName = testCase.qualifiedName();
       for (TraceNode traceNode : executionList) {
            BreakPoint breakPoint = traceNode.getBreakPoint();
            String currentMethodName = breakPoint.getMethodName();
            if (currentMethodName.equals("<init>") || currentMethodName.equals("<clinit>")) {
                continue;
            }
            String fullMethodName = breakPoint.getDeclaringCompilationUnitName() + "#" + currentMethodName;
           if (fullMethodName.equals(testCaseName)){
               continue;
           }
           TestIO testIO = new TestIO(traceNode.getReadVariables(), traceNode.getWrittenVariables());
           result.add(testIO);
       }
       return result;
    }
}
