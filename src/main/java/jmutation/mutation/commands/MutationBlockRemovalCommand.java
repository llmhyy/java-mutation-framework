package jmutation.mutation.commands;

import jmutation.model.ast.ASTNodeRetriever;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.VariableDeclaration;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class MutationBlockRemovalCommand extends MutationCommand {
    public MutationBlockRemovalCommand(ASTNode node) {
        super(node);
    }

    @Override
    public ASTNode executeMutation() {
        Block block = (Block) node;
        List<Statement> stmts = block.statements();
        if (!canClearBlock()) {
            return null;
        }
        stmts.clear();
        return block;
    }

    private boolean canClearBlock() {
        Block block = (Block) node;
        ASTNodeRetriever<Block> blockASTNodeRetriever = new ASTNodeRetriever<>(Block.class);
        blockASTNodeRetriever.setShouldStopAtFirstEncounter(true);
        List<Statement> stmts = block.statements();
        if (stmts.size() == 0) {
            return false;
        }
        for (Statement stmt : stmts) {
            stmt.accept(blockASTNodeRetriever);
        }
        List<Block> innerBlocks = blockASTNodeRetriever.getNodes();
        if (!innerBlocks.isEmpty()) {
            return false;
        }
        ASTNode blockParent = node.getParent();
        if (blockParent instanceof MethodDeclaration) {
            // If block is not outermost block, safe to remove
            for (Statement stmt : stmts) {
                // If return statement removed, will not compile.
                if (stmt instanceof ReturnStatement) {
                    return false;
                }
            }
        }
        ASTNodeRetriever<VariableDeclaration> variableDeclarationRetriever = new ASTNodeRetriever<>(VariableDeclaration.class);
        int lastStmtIdx = stmts.size() - 1;
        variableDeclarationRetriever.setStopNode(stmts.get(lastStmtIdx));
        cu.accept(variableDeclarationRetriever);
        List<VariableDeclaration> variableDeclarations = variableDeclarationRetriever.getNodes();
        List<VariableDeclaration> variableDeclarationsWithInit = variableDeclarations.stream().filter(variableDeclaration -> variableDeclaration.getInitializer() != null).collect(Collectors.toList());
        List<String> declaredVarNamesLs = variableDeclarationsWithInit.stream().map((variableDeclaration -> variableDeclaration.getName().toString())).collect(Collectors.toList());
        Set<String> declaredVarNames = new HashSet<>(declaredVarNamesLs);
        // If block contains pure assignments (not new var declarations) e.g. x = 1; But was not assigned before, do not clear.
        // Can lead to compilation errors.
        for (Statement stmt : stmts) {
            // For each stmt, check if it is pure assignment.
            // For those that are, check if they were declared before. If not declared, cannot remove it.
            if (stmt instanceof ExpressionStatement) {
                ExpressionStatement expressionStatement = (ExpressionStatement) stmt;
                Expression expression = expressionStatement.getExpression();
                if (expression instanceof Assignment) {
                    Assignment assignment = (Assignment) expression;
                    Expression leftHandSide = assignment.getLeftHandSide();
                    if (leftHandSide instanceof SimpleName) {
                        String varName = ((SimpleName) leftHandSide).getFullyQualifiedName();
                        if (!declaredVarNames.contains(varName)) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }
}
