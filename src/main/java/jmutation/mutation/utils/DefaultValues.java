package jmutation.mutation.utils;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ArrayType;
import org.eclipse.jdt.core.dom.CharacterLiteral;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.Type;

public class DefaultValues {
    /**
     *  Returns expression representing the default value for given type
     *  i.e. 0 for int byte short long types, false for boolean, etc.
     *  null for reference types is not implemented, as it can often lead to crashes
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
        }
        return null;
    }
}
