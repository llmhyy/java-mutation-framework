package jmutation.mutation.utils;

import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.InfixExpression.Operator;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class MathOperator {
    static Map<Operator, Operator> replacementMap = new HashMap<>(){{
        put(Operator.DIVIDE, Operator.TIMES);
        put(Operator.MINUS, Operator.PLUS);
        put(Operator.REMAINDER, Operator.TIMES);
        put(Operator.TIMES, Operator.DIVIDE);
        put(Operator.PLUS, Operator.MINUS);
    }};

    public static Set<InfixExpression.Operator> getOperatorSet() {
        return operatorSet;
    }

    static Set<Operator> operatorSet = new HashSet<>(Arrays.asList(Operator.DIVIDE, Operator.MINUS, Operator.PLUS, Operator.TIMES, Operator.REMAINDER));

    public static Operator getReplacementOperator(Operator operator) {
        return replacementMap.get(operator);
    }
}
