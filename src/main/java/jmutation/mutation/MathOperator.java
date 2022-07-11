package jmutation.mutation;

import org.eclipse.jdt.core.dom.InfixExpression;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class MathOperator {
    public static Set<InfixExpression.Operator> getOperatorSet() {
        return operatorSet;
    }

    static Set<InfixExpression.Operator> operatorSet = new HashSet<>(Arrays.asList(InfixExpression.Operator.DIVIDE, InfixExpression.Operator.MINUS, InfixExpression.Operator.PLUS, InfixExpression.Operator.TIMES, InfixExpression.Operator.REMAINDER));

}
