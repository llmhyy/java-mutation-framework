package jmutation.mutation.utils;

import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.InfixExpression.Operator;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class ConditionalOperator {
    public static Set<Operator> getOperatorSet() {
        return operatorSet;
    }

    static Set<InfixExpression.Operator> operatorSet = new HashSet<>(Arrays.asList(Operator.LESS, Operator.LESS_EQUALS, Operator.GREATER, Operator.GREATER_EQUALS, Operator.EQUALS, Operator.NOT_EQUALS));
}
