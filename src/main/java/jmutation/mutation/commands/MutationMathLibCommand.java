package jmutation.mutation.commands;

import jmutation.utils.RandomSingleton;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



public class MutationMathLibCommand extends MutationCommand {
    private List<String> mathDoubleArgMethodNames = new ArrayList<>(Arrays.asList(""));
    private Map<String, List<String>> replacementMap = new HashMap<>(){{
        put("pow", Arrays.asList("addExact"));}
    };
    public MutationMathLibCommand(ASTNode node) {
        super(node);
    }

    @Override
    public ASTNode executeMutation() {
        MethodInvocation methodInvocation = (MethodInvocation) node;
        String methodName = methodInvocation.getName().toString();
        if (!replacementMap.containsKey(methodName)) {
            return null;
        }
        List<String> possibleReplacements = replacementMap.get(methodName);
        int numOfReplacements = possibleReplacements.size();
        int randomIdx = (int) Math.floor(RandomSingleton.getSingleton().random() * numOfReplacements);
        if (randomIdx == numOfReplacements) {
            randomIdx--;
        }
        String replacementMethodName = possibleReplacements.get(randomIdx);
        SimpleName replacement = ast.newSimpleName(replacementMethodName);
        methodInvocation.setName(replacement);
        return methodInvocation;
    }
}