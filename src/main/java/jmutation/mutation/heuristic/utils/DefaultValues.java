package jmutation.mutation.heuristic.utils;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ArrayType;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.CharacterLiteral;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.Type;

public class DefaultValues {
    private DefaultValues() {
    }

    /**
     * Returns expression representing the default value for given type
     * i.e. 0 for int byte short long types, false for boolean, etc.
     */
    public static Expression getDefaultExpression(Type type) {
        AST ast = type.getAST();
        if (type instanceof PrimitiveType) {
            PrimitiveType primitiveType = (PrimitiveType) type;
            PrimitiveType.Code primitiveTypeCode = primitiveType.getPrimitiveTypeCode();
            if (primitiveTypeCode.equals(PrimitiveType.BYTE) ||
                    primitiveTypeCode.equals(PrimitiveType.INT) ||
                    primitiveTypeCode.equals(PrimitiveType.SHORT) ||
                    primitiveTypeCode.equals(PrimitiveType.LONG)) {
                return ast.newNumberLiteral("0");
            } else if (primitiveTypeCode.equals(PrimitiveType.BOOLEAN)) {
                return ast.newBooleanLiteral(false);
            } else if (primitiveTypeCode.equals(PrimitiveType.FLOAT) ||
                    primitiveTypeCode.equals(PrimitiveType.DOUBLE)) {
                return ast.newNumberLiteral("0.0");
            } else if (primitiveTypeCode.equals(PrimitiveType.CHAR)) {
                CharacterLiteral characterLiteral = ast.newCharacterLiteral();
                characterLiteral.setCharValue('\u0000');
                return characterLiteral;
            }
        } else if (type instanceof SimpleType || type instanceof ArrayType) {
            return ast.newNullLiteral();
        }
        return null;
    }

    public static boolean isDefaultExpression(Expression expression) {
        DefaultExpressionChecker defaultExpressionChecker = new DefaultExpressionChecker();
        return defaultExpressionChecker.check(expression);
    }

    private static class DefaultExpressionChecker extends ASTVisitor {
        private boolean isDefault = false;
        private boolean visited = false;

        @Override
        public void preVisit(ASTNode node) {
            isDefault = false;
        }

        @Override
        public boolean preVisit2(ASTNode node) {
            if (visited) {
                return false;
            }
            return super.preVisit2(node);
        }

        @Override
        public boolean visit(NumberLiteral numberLiteral) {
            String token = numberLiteral.getToken();
            int idxOfDot = token.indexOf('.');
            isDefault = true;
            for (int i = 0; i < token.length(); i++) {
                if (i == idxOfDot) {
                    continue;
                }
                char current = token.charAt(i);
                if (current != '0') {
                    isDefault = false;
                    break;
                }
            }
            return false;
        }

        @Override
        public boolean visit(CharacterLiteral characterLiteral) {
            isDefault = characterLiteral.getEscapedValue().equals('\u0000');
            return false;
        }

        @Override
        public boolean visit(BooleanLiteral booleanLiteral) {
            isDefault = !booleanLiteral.booleanValue();
            return false;
        }

        @Override
        public void postVisit(ASTNode node) {
            visited = true;
        }

        public boolean check(ASTNode node) {
            if (node == null) {
                return false;
            }
            node.accept(this);
            return isDefault;
        }
    }
}
