package jmutation.mutation.parser;

import jmutation.mutation.MathOperator;
import jmutation.mutation.commands.MutationBlockRemovalCommand;
import jmutation.mutation.commands.MutationCommand;
import jmutation.mutation.commands.MutationForLoopToIfCommand;
import jmutation.mutation.commands.MutationMathOperatorCommand;
import jmutation.mutation.commands.MutationWhileLoopToIfCommand;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.WhileStatement;

import java.util.Set;

public class MutationParser {
    static public MutationCommand createMutationCommand(ASTNode node) {
        // TODO: check and return correct mutation command based on contents of ASTNode
        if (node instanceof InfixExpression) {
            InfixExpression currNode = (InfixExpression) node;
            if (isMathOperator(currNode)) {
                return new MutationMathOperatorCommand(node);
            }
        } else if (node instanceof WhileStatement) {
            return new MutationWhileLoopToIfCommand(node);
        } else if (node instanceof Block) {
            return new MutationBlockRemovalCommand(node);
        } else if (node instanceof ForStatement) {
            return new MutationForLoopToIfCommand(node);
        }
        return null;
    }

    private static boolean isMathOperator(InfixExpression e) {
        Set<InfixExpression.Operator> mathOperators = MathOperator.getOperatorSet();
        InfixExpression.Operator currentOp = e.getOperator();
        return mathOperators.contains(currentOp);
    }
}
