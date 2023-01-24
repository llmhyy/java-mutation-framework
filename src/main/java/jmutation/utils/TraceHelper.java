package jmutation.utils;

import jmutation.model.ast.ASTNodeParentRetriever;
import jmutation.mutation.MutationCommand;
import jmutation.parser.ProjectParser;
import microbat.model.BreakPoint;
import microbat.model.ClassLocation;
import microbat.model.trace.Trace;
import microbat.model.trace.TraceNode;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TraceHelper {
    private TraceHelper() {
    }

    /**
     * Gets TraceNodes that matches the mutation history i.e. root cause of bugs
     *
     * @param buggyTrace
     * @param mutationHistory
     * @return list of TraceNodes
     */
    public static List<TraceNode> getMutatedTraceNodes(Trace buggyTrace, List<MutationCommand> mutationHistory) {
        Set<TraceNode> result = new HashSet<>();
        List<TraceNode> executionList = buggyTrace.getExecutionList();
        for (MutationCommand mutationCommand : mutationHistory) {
            ASTNode node = mutationCommand.getOriginalNode();
            ASTNode root = node.getRoot();
            ASTNodeParentRetriever<TypeDeclaration> typeDeclarationASTNodeParentRetriever = new ASTNodeParentRetriever<>(TypeDeclaration.class);
            TypeDeclaration typeDeclaration = typeDeclarationASTNodeParentRetriever.getParentOfType(node);
            CompilationUnit unit = (CompilationUnit) root;
            PackageDeclaration packageDeclaration = unit.getPackage();
            String mutatedClassName = typeDeclaration.getName().getFullyQualifiedName();
            String fullMutatedClassName = packageDeclaration == null ? mutatedClassName : packageDeclaration.getName() + "." + mutatedClassName;
            int startPos = unit.getLineNumber(node.getStartPosition());
            int endPos = unit.getLineNumber(node.getStartPosition() + node.getLength() - 1);
            for (TraceNode traceNode : executionList) {
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

    public static void setClassPathsToBreakpoints(Trace trace, File root) {
        Map<String, String> classNameToFilePath = new HashMap<>();
        List<TraceNode> executionList = trace.getExecutionList();
        for (TraceNode traceNode : executionList) {
            BreakPoint breakPoint = traceNode.getBreakPoint();
            String classCanonicalName = breakPoint.getClassCanonicalName();
            if (classNameToFilePath.containsKey(classCanonicalName)) {
                breakPoint.setFullJavaFilePath(classNameToFilePath.get(classCanonicalName));
                continue;
            }
            File breakPointFile = ProjectParser.getFileOfClass(classCanonicalName, root);
            String breakPointFilePath = breakPointFile.getAbsolutePath();
            breakPoint.setFullJavaFilePath(breakPointFilePath);
            classNameToFilePath.put(classCanonicalName, breakPointFilePath);
        }
    }

    public static void setClassPathsToClassLocations(Set<ClassLocation> classLocationSet, File root) {
        Map<String, String> classNameToFilePath = new HashMap<>();
        for (ClassLocation classLocation : classLocationSet) {
            String classCanonicalName = classLocation.getClassCanonicalName();
            if (classNameToFilePath.containsKey(classCanonicalName)) {
                classLocation.setFullJavaFilePath(classNameToFilePath.get(classCanonicalName));
                continue;
            }
            File breakPointFile = ProjectParser.getFileOfClass(classCanonicalName, root);
            String breakPointFilePath = breakPointFile.getAbsolutePath();
            classLocation.setFullJavaFilePath(breakPointFilePath);
            classNameToFilePath.put(classCanonicalName, breakPointFilePath);
        }
    }
}
