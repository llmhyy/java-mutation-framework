package jmutation.mutation.heuristic.parser;

import jmutation.mutation.MutationCommand;
import jmutation.mutation.heuristic.commands.MutationConditionalBoundaryCommand;
import jmutation.mutation.heuristic.commands.MutationConditionalNegationCommand;
import jmutation.mutation.heuristic.commands.MutationMathOperatorCommand;
import jmutation.mutation.heuristic.utils.ConditionalOperator;
import jmutation.mutation.heuristic.utils.MathOperator;
import jmutation.utils.RandomSingleton;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.InfixExpression.Operator;

import java.util.ArrayList;
import java.util.List;
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

    public static List<MutationCommand> parseAllCommands(InfixExpression node) {
        List<MutationCommand> result = new ArrayList<>();
        if (isMathOperator(node)) {
            result.add(new MutationMathOperatorCommand(node));
            return result;
        }
        if (isComparisonOperator(node)) {
            if (cannotCreateConditionalBoundaryCommand(node)) {
                result.add(new MutationConditionalNegationCommand(node));
                return result;
            }
            result.add(new MutationConditionalBoundaryCommand(node));
            result.add(new MutationConditionalNegationCommand(node));
        }
        return result;
    }

    private static boolean cannotCreateConditionalBoundaryCommand(InfixExpression node) {
        Operator op = node.getOperator();
        return op.equals(Operator.EQUALS) || op.equals(Operator.NOT_EQUALS);
    }
}
