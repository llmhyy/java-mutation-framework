package jmutation.mutation.commands;

import jmutation.mutation.utils.DefaultValues;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import java.util.List;

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
        returnTypeObtainer.setMethodInvocation(methodInvocation);
        Type methodReturnType = returnTypeObtainer.getReturnType();
        StructuralPropertyDescriptor location = methodInvocation.getLocationInParent();
        Expression replacement = getDefaultValue(methodReturnType);
        ASTNode parent = methodInvocation.getParent();
        if (!location.isChildListProperty()) {
            parent.setStructuralProperty(location, replacement);
        } else {
            List<ASTNode> children = ((List) parent.getStructuralProperty(location));
            int childIdx = children.indexOf(methodInvocation);
            children.set(childIdx, replacement);
        }
        node = replacement;
        return methodInvocation;
    }

    private Expression getDefaultValue(Type methodReturnType) {
        return DefaultValues.getDefaultExpression(methodReturnType);
    }

    private class MethodReturnTypeObtainer extends ASTVisitor {
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
