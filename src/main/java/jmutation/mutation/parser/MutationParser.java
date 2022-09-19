package jmutation.mutation.parser;

import jmutation.mutation.commands.*;
import jmutation.utils.RandomSingleton;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
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
    MutationCommand command;

    public MutationCommand parse(ASTNode node) {
        node.accept(this);
        if (command == null) {
            return null;
        }
        if (command.canExecute()) {
            return command;
        }
        return null;
    }

    @Override
    public void preVisit(ASTNode node) {
        command = null;
    }

    @Override
    public boolean visit(InfixExpression node) {
        command = MutationInfixExpressionParser.parse(node);
        return false;
    }

    @Override
    public boolean visit(WhileStatement node) {
        command = new MutationWhileLoopToIfCommand(node);
        return false;
    }

    @Override
    public boolean visit(ForStatement node) {
        command = new MutationForLoopToIfCommand(node);
        return false;
    }

    @Override
    public boolean visit(IfStatement node) {
        command = new MutationIfCondToTrueCommand(node);
        return false;
    }

    @Override
    public boolean visit(ReturnStatement node) {
        List<MutationCommand> possibleCommands = new ArrayList<>();
        possibleCommands.add(new MutationReturnMathCommand(node));
        possibleCommands.add(new MutationReturnStmtLiteralCommand(node));
        possibleCommands = possibleCommands.stream().filter(command -> command.canExecute()).collect(Collectors.toList());
        if (possibleCommands.isEmpty()) {
            return true;
        }
        possibleCommands = RandomSingleton.getSingleton().shuffle(possibleCommands);
        command = possibleCommands.get(0);
        return false;
    }

    @Override
    public boolean visit(MethodInvocation node) {
        if (node.getExpression() != null && node.getExpression().toString().equals("Math")) {
            command = new MutationMathLibCommand(node);
        }
        return false;
    }
}
