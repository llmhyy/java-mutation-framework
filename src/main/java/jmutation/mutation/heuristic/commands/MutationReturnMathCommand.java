package jmutation.mutation.heuristic.commands;

import jmutation.model.ast.ASTNodeParentRetriever;
import jmutation.mutation.heuristic.utils.MathOperator;
import jmutation.utils.RandomSingleton;
import org.eclipse.jdt.core.dom.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MutationReturnMathCommand extends HeuristicMutationCommand {
    private Set<PrimitiveType.Code> mathCodes = new HashSet<>(){{
        add(PrimitiveType.INT);
        add(PrimitiveType.FLOAT);
        add(PrimitiveType.DOUBLE);
        add(PrimitiveType.SHORT);
        add(PrimitiveType.LONG);
    }
    };

    public MutationReturnMathCommand(ASTNode node) {
        super(node);
    }

    @Override
    public ASTNode executeMutation() {
        ReturnStatement returnStatement = (ReturnStatement) node;
        Expression returnExpression = returnStatement.getExpression();
        InfixExpression replacementExpression = ast.newInfixExpression();
        returnStatement.setExpression(replacementExpression);
        replacementExpression.setLeftOperand(returnExpression);
        replacementExpression.setRightOperand(ast.newNumberLiteral("2"));
        List<InfixExpression.Operator> operatorList = MathOperator.getOperatorList();
        operatorList = RandomSingleton.getSingleton().shuffle(operatorList);
        replacementExpression.setOperator(operatorList.get(0));
        return returnStatement;
    }

    @Override
    public boolean canExecute() {
        ASTNodeParentRetriever<MethodDeclaration> methodDeclarationASTNodeParentRetriever = new ASTNodeParentRetriever<>(MethodDeclaration.class);
        MethodDeclaration methodDeclaration = methodDeclarationASTNodeParentRetriever.getParentOfType(node);
        Type returnType = methodDeclaration.getReturnType2();
        if (returnType instanceof PrimitiveType) {
            PrimitiveType primitiveType = (PrimitiveType) returnType;
            if (mathCodes.contains(primitiveType.getPrimitiveTypeCode())) {
                return true;
            }
        }
        return false;
    }
}
