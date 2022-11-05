package jmutation.mutation.heuristic.commands;

import jmutation.mutation.MutationCommand;
import jmutation.mutation.heuristic.utils.DefaultValues;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

/**
 * Not complete. Only works for var declaration method invocations e.g. int x = bar(); -&gt; int x = 0;
 * Difficulty obtaining return types of methods for method invocations.
 * (Need turn on bindings on ASTParser, but incurs large overhead)
 */
public class MutationMethodInvocationReturnCommand extends MutationCommand {
    public MutationMethodInvocationReturnCommand(ASTNode node) {
        super(node);
    }

    @Override
    public ASTNode executeMutation() {
        MethodInvocation methodInvocation = (MethodInvocation) node;
        MethodReturnTypeObtainer returnTypeObtainer = new MethodReturnTypeObtainer();
        returnTypeObtainer.setMethodInvocation((MethodInvocation) originalNode);
        Type methodReturnType = returnTypeObtainer.getReturnType();
        node = getDefaultValue(methodReturnType);
        return methodInvocation;
    }

    private Expression getDefaultValue(Type methodReturnType) {
        return DefaultValues.getDefaultExpression(methodReturnType);
    }

    private static class MethodReturnTypeObtainer extends ASTVisitor {
        MethodInvocation methodInvocation;
        Type type;

        void setMethodInvocation(MethodInvocation methodInvocation) {
            this.methodInvocation = methodInvocation;
        }

        Type getReturnType() {
            type = null;
            ASTNode parent = methodInvocation.getParent();
            parent.accept(this);
            return type;
        }

        @Override
        public boolean visit(VariableDeclarationFragment variableDeclarationFragment) {
            VariableDeclarationStatement stmt = (VariableDeclarationStatement) variableDeclarationFragment.getParent();
            type = stmt.getType();
            return false;
        }
    }
}
