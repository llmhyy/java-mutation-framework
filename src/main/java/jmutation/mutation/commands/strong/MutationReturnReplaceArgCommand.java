package jmutation.mutation.commands;

import jmutation.model.ast.ASTNodeParentRetriever;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ArrayType;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.Type;

import java.util.List;

public class MutationReturnReplaceArgCommand extends MutationCommand {
    public MutationReturnReplaceArgCommand(ASTNode node) {
        super(node);
    }

    @Override
    public ASTNode executeMutation() {
        ReturnStatement returnStatement = (ReturnStatement) node;
        ASTNodeParentRetriever<MethodDeclaration> methodDeclarationASTNodeParentRetriever = new ASTNodeParentRetriever<>(MethodDeclaration.class);
        MethodDeclaration methodDeclaration = methodDeclarationASTNodeParentRetriever.getParentOfType(node);
        Type returnType = methodDeclaration.getReturnType2();
        List<SingleVariableDeclaration> parameters = methodDeclaration.parameters();
        TypeIdentifier typeIdentifier = new TypeIdentifier();
        String returnTypeIdentifier = typeIdentifier.getTypeIdentifier(returnType);
        for (SingleVariableDeclaration parameter : parameters) {
            Type parameterType = parameter.getType();
            String parameterTypeIdentifier = typeIdentifier.getTypeIdentifier(parameterType);
            if (parameterTypeIdentifier != null && parameterTypeIdentifier.equals(returnTypeIdentifier) &&
                    !parameter.getName().toString().equals(returnStatement.getExpression().toString())) {
                Name replacement = ast.newName(parameter.getName().toString());
                returnStatement.setExpression(replacement);
                return returnStatement;
            }
        }
        return null;
    }

    @Override
    public boolean canExecute() {
        ReturnStatement returnStatement = (ReturnStatement) node;
        Expression expression = returnStatement.getExpression();
        if (expression == null) {
            return false;
        }
        if (expression instanceof MethodInvocation) {
            ASTNodeParentRetriever<TryStatement> tryStatementASTNodeParentRetriever = new ASTNodeParentRetriever<>(TryStatement.class);
            TryStatement tryStatement = tryStatementASTNodeParentRetriever.getParentOfType(node);
            if (tryStatement != null) {
                return false;
            }
        }
        ASTNodeParentRetriever<MethodDeclaration> methodDeclarationASTNodeParentRetriever = new ASTNodeParentRetriever<>(MethodDeclaration.class);
        MethodDeclaration methodDeclaration = methodDeclarationASTNodeParentRetriever.getParentOfType(node);
        Type returnType = methodDeclaration.getReturnType2();
        List<SingleVariableDeclaration> parameters = methodDeclaration.parameters();
        TypeIdentifier typeIdentifier = new TypeIdentifier();
        String returnTypeIdentifier = typeIdentifier.getTypeIdentifier(returnType);
        for (SingleVariableDeclaration parameter : parameters) {
            Type parameterType = parameter.getType();
            String parameterTypeIdentifier = typeIdentifier.getTypeIdentifier(parameterType);
            if (parameterTypeIdentifier != null && parameterTypeIdentifier.equals(returnTypeIdentifier) &&
                    !parameter.getName().toString().equals(returnStatement.getExpression().toString())) {
                return true;
            }
        }

        return false;
    }

    private class TypeIdentifier extends ASTVisitor {
        String typeIdentifier = null;

        @Override
        public void preVisit(ASTNode node) {
            typeIdentifier = null;
        }

        @Override
        public boolean visit(PrimitiveType primitiveType) {
            typeIdentifier = primitiveType.getClass().toString();
            typeIdentifier += ":" + primitiveType.getPrimitiveTypeCode();
            return false;
        }

        @Override
        public boolean visit(ArrayType arrayType) {
            typeIdentifier = arrayType.getClass().toString();
            typeIdentifier += ":" + arrayType.getElementType();
            return false;
        }

        public String getTypeIdentifier(ASTNode node) {
            node.accept(this);
            return typeIdentifier;
        }
    }
}
