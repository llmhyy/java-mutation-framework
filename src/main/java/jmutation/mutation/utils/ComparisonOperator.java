package jmutation.mutation.utils;

import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.InfixExpression.Operator;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ComparisonOperator {
    static Map<Operator, Operator> replacementMap = new HashMap<>(){{
        put(Operator.LESS, Operator.LESS_EQUALS);
        put(Operator.LESS_EQUALS, Operator.LESS);
        put(Operator.GREATER, Operator.GREATER_EQUALS);
        put(Operator.GREATER_EQUALS, Operator.GREATER);
        put(Operator.EQUALS, Operator.NOT_EQUALS);
        put(Operator.NOT_EQUALS, Operator.EQUALS);
    }};
    public static Set<Operator> getOperatorSet() {
        return operatorSet;
    }

    static Set<InfixExpression.Operator> operatorSet = new HashSet<>(Arrays.asList(Operator.LESS, Operator.LESS_EQUALS, Operator.GREATER, Operator.GREATER_EQUALS, Operator.EQUALS, Operator.NOT_EQUALS));

    public static Operator getReplacementOperator(Operator operator) {
        return replacementMap.get(operator);
    }
}
