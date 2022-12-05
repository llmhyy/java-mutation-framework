package jmutation.mutation.heuristic.utils;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.CharacterLiteral;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.Type;

public class DefaultValueReplacements {
    private DefaultValueReplacements() {
    }

    public static Expression getDefaultReplacementExpression(Type type) {
        AST ast = type.getAST();
        if (type instanceof PrimitiveType) {
            PrimitiveType primitiveType = (PrimitiveType) type;
            PrimitiveType.Code primitiveTypeCode = primitiveType.getPrimitiveTypeCode();
            if (primitiveTypeCode.equals(PrimitiveType.BYTE) ||
                    primitiveTypeCode.equals(PrimitiveType.INT) ||
                    primitiveTypeCode.equals(PrimitiveType.SHORT) ||
                    primitiveTypeCode.equals(PrimitiveType.LONG)) {
                return ast.newNumberLiteral("1");
            } else if (primitiveTypeCode.equals(PrimitiveType.BOOLEAN)) {
                return ast.newBooleanLiteral(true);
            } else if (primitiveTypeCode.equals(PrimitiveType.FLOAT) ||
                    primitiveTypeCode.equals(PrimitiveType.DOUBLE)) {
                return ast.newNumberLiteral("1.0");
            } else if (primitiveTypeCode.equals(PrimitiveType.CHAR)) {
                CharacterLiteral characterLiteral = ast.newCharacterLiteral();
                characterLiteral.setCharValue('\u0001');
                return characterLiteral;
            }
        }
        return null;
    }
}
