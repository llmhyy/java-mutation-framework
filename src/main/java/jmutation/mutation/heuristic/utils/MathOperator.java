package jmutation.mutation.heuristic.utils;

import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.InfixExpression.Operator;

import java.util.*;

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
    public static List<InfixExpression.Operator> getOperatorList() {
        return operatorList;
    }

    static Set<Operator> operatorSet = new HashSet<>(Arrays.asList(Operator.DIVIDE, Operator.MINUS, Operator.PLUS, Operator.TIMES, Operator.REMAINDER));
    static List<Operator> operatorList = new ArrayList<>(Arrays.asList(Operator.DIVIDE, Operator.MINUS, Operator.PLUS, Operator.TIMES, Operator.REMAINDER));

    public static Operator getReplacementOperator(Operator operator) {
        return replacementMap.get(operator);
    }
}
