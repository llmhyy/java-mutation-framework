package jmutation.mutation.commands.strong;

import jmutation.model.ast.ASTNodeRetriever;
import jmutation.mutation.commands.MutationCommand;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.TryStatement;
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
        stmts.clear();
        return block;
    }

    @Override
    public boolean canExecute() {
        Block block = (Block) node;
        List<Statement> stmts = block.statements();
        if (stmts.isEmpty() || hasInnerBlock(stmts) || hasTopLayerReturnStmt(stmts) ||
                hasIrremovableAssignmentsInBlock(stmts) || isTryBlock(block)) {
            return false;
        }
        return true;
    }

    private boolean hasInnerBlock(List<Statement> stmts) {
        ASTNodeRetriever<Block> blockASTNodeRetriever = new ASTNodeRetriever<>(Block.class);
        blockASTNodeRetriever.setShouldStopAtFirstEncounter(true);
        for (Statement stmt : stmts) {
            stmt.accept(blockASTNodeRetriever);
        }
        List<Block> innerBlocks = blockASTNodeRetriever.getNodes();
        if (innerBlocks.isEmpty()) {
            return false;
        }
        return true;
    }
    private boolean hasIrremovableAssignmentsInBlock(List<Statement> stmts) {
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
                    String varName;
                    if (leftHandSide instanceof SimpleName) {
                        varName = ((SimpleName) leftHandSide).getFullyQualifiedName();
                    } else if (leftHandSide instanceof FieldAccess) {
                        varName = ((FieldAccess) leftHandSide).getName().toString();
                    } else {
                        continue;
                    }
                    if (!declaredVarNames.contains(varName)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean hasTopLayerReturnStmt(List<Statement> stmts) {
        ASTNode blockParent = node.getParent();
        if (blockParent instanceof MethodDeclaration) {
            // If block is not outermost block, safe to remove
            for (Statement stmt : stmts) {
                // If return statement removed, will not compile.
                if (stmt instanceof ReturnStatement) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isTryBlock(Block block) {
        ASTNode parent = block.getParent();
        if (parent instanceof TryStatement) {
            return true;
        }
        return false;
    }
}
