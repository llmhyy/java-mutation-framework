package jmutation.model.ast;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;

import java.util.ArrayList;
import java.util.List;

public class ASTNodeRetriever<T extends ASTNode> extends ASTVisitor {
    List<T> nodeLs = new ArrayList<>();
    Class<T> nodeType;

    ASTNode stopNode = null;

    boolean shouldStopVisits = false;

    boolean shouldStopAtFirstEncounter = false;

    public ASTNodeRetriever(Class<T> nodeType) {
        this.nodeType = nodeType;
    }

    public void setStopNode(ASTNode node) {
        this.stopNode = node;
    }

    public void setShouldStopAtFirstEncounter(boolean shouldStopAtFirstEncounter) {
        this.shouldStopAtFirstEncounter = shouldStopAtFirstEncounter;
    }

    @Override
    public void preVisit(ASTNode node) {
        // Check if node is instance of nodeType, then add to nodeLs
        if (!(nodeType.isAssignableFrom(node.getClass()))) {
            return;
        }
        nodeLs.add((T) node);
        if (shouldStopAtFirstEncounter) {
           shouldStopVisits = true;
        }
    }

    @Override
    public boolean preVisit2(ASTNode node) {
        if (shouldStopVisits) {
            return false;
        }
        if (stopNode != null && node == stopNode) {
            shouldStopVisits = true;
            return false;
        }
        preVisit(node);
        return true;
    }

    public List<T> getNodes() {
        return nodeLs;
    }
}
