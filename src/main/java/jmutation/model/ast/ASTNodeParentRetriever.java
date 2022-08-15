package jmutation.model.ast;

import org.eclipse.jdt.core.dom.ASTNode;

public class ASTNodeParentRetriever<T extends ASTNode> {

    Class<T> nodeType;
    public ASTNodeParentRetriever(Class<T> nodeType) {
        this.nodeType = nodeType;
    }

    public T getParentOfType(ASTNode node) {
        ASTNode current = node;
        while (current != null) {
            if (nodeType.isAssignableFrom(current.getClass())) {
                return (T) current;
            }
            current = current.getParent();
        }
        return null;
    }
}
