package jmutation.mutation.parser;

import jmutation.mutation.commands.MutationCommand;
import jmutation.mutation.commands.MutationConditionalBoundaryCommand;
import jmutation.mutation.commands.MutationConditionalNegationCommand;
import jmutation.mutation.commands.MutationMathOperatorCommand;
import jmutation.mutation.utils.ConditionalOperator;
import jmutation.mutation.utils.MathOperator;
import jmutation.utils.RandomSingleton;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.InfixExpression.Operator;

import java.util.Set;

/**
 * Helper for MutationParser for the various cases for InfixExpression mutations
 */
public class MutationInfixExpressionParser {
    private static boolean isMathOperator(InfixExpression e) {
        Set<Operator> mathOperators = MathOperator.getOperatorSet();
        InfixExpression.Operator currentOp = e.getOperator();
        return mathOperators.contains(currentOp);
    }

    private static boolean isComparisonOperator(InfixExpression e) {
        Set<Operator> comparisonOperators = ConditionalOperator.getOperatorSet();
        InfixExpression.Operator currentOp = e.getOperator();
        return comparisonOperators.contains(currentOp);
    }

    public static MutationCommand parse(InfixExpression node) {
        if (isMathOperator(node)) {
            return new MutationMathOperatorCommand(node);
        }
        if (isComparisonOperator(node)) {
            if (cannotCreateConditionalBoundaryCommand(node)) {
                return new MutationConditionalNegationCommand(node);
            }
            if (RandomSingleton.getSingleton().random() > 0.5) {
                return new MutationConditionalBoundaryCommand(node);
            } else {
                return new MutationConditionalNegationCommand(node);
            }
        }
        return null;
    }

    private static boolean cannotCreateConditionalBoundaryCommand(InfixExpression node) {
        Operator op = node.getOperator();
        if (op.equals(Operator.EQUALS) || op.equals(Operator.NOT_EQUALS)) {
            return true;
        }
        return false;
    }
}
