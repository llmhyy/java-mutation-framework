package jmutation.mutation.utils;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.MethodDeclaration;

public class MutationHelper {
    public static MethodDeclaration getMethodDeclarationParent(ASTNode node) {
        ASTNode current = node;
        while (current != null) {
            if (current instanceof MethodDeclaration) {
                return (MethodDeclaration) current;
            }
            current = current.getParent();
        }
        return null;
    }
}
