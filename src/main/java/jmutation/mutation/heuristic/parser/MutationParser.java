package jmutation.mutation.heuristic.parser;

import jmutation.mutation.MutationCommand;
import jmutation.mutation.heuristic.commands.MutationForLoopToIfCommand;
import jmutation.mutation.heuristic.commands.MutationMathLibCommand;
import jmutation.mutation.heuristic.commands.MutationReturnMathCommand;
import jmutation.mutation.heuristic.commands.MutationReturnStmtLiteralCommand;
import jmutation.mutation.heuristic.commands.MutationWhileLoopToIfCommand;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.WhileStatement;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Returns a mutation command to execute for a given ASTNode
 */
public class MutationParser extends ASTVisitor {
    List<MutationCommand> commands = new ArrayList<>();

    public List<MutationCommand> parse(ASTNode node) {
        node.accept(this);
        return commands.stream().filter(MutationCommand::canExecute).collect(Collectors.toList());
    }

    @Override
    public void preVisit(ASTNode node) {
        commands = new ArrayList<>();
    }

    @Override
    public boolean visit(InfixExpression node) {
        commands.addAll(MutationInfixExpressionParser.parseAllCommands(node));
        return false;
    }

    @Override
    public boolean visit(WhileStatement node) {
        commands.add(new MutationWhileLoopToIfCommand(node));
        return false;
    }

    @Override
    public boolean visit(ForStatement node) {
        commands.add(new MutationForLoopToIfCommand(node));
        return false;
    }

    @Override
    public boolean visit(ReturnStatement node) {
        commands.add(new MutationReturnMathCommand(node));
        commands.add(new MutationReturnStmtLiteralCommand(node));
        return false;
    }

    @Override
    public boolean visit(MethodInvocation node) {
        if (node.getExpression() != null && node.getExpression().toString().equals("Math")) {
            commands.add(new MutationMathLibCommand(node));
        }
        return false;
    }
}
