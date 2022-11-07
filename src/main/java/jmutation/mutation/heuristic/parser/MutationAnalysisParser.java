package jmutation.mutation.heuristic.parser;

import jmutation.mutation.MutationCommand;
import jmutation.mutation.heuristic.commands.*;
import jmutation.mutation.heuristic.commands.strong.*;
import org.eclipse.jdt.core.dom.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MutationAnalysisParser extends ASTVisitor {
    private List<MutationCommand> commands;

    public List<MutationCommand> parse(ASTNode node) {
        node.accept(this);
        return commands.stream().filter(command -> command.canExecute()).collect(Collectors.toList());
    }

    @Override
    public void preVisit(ASTNode node) {
        commands = new ArrayList<>();
    }

    @Override
    public boolean visit(ReturnStatement node) {
        List<MutationCommand> possibleCommands = new ArrayList<>();
        possibleCommands.add(new MutationReturnReplaceArgCommand(node));
        possibleCommands.add(new MutationReturnStmtLiteralCommand(node));
        possibleCommands.add(new MutationReturnMathCommand(node));
        possibleCommands.add(new MutationReturnStmtCommand(node));
        possibleCommands = possibleCommands.stream().filter(command -> command.canExecute()).collect(Collectors.toList());
        if (possibleCommands.isEmpty()) {
            return true;
        }
        commands.addAll(possibleCommands);
        return false;
    }

    @Override
    public boolean visit(Block node) {
        commands.add(new MutationBlockRemovalCommand(node));
        return false;
    }

    @Override
    public boolean visit(VariableDeclarationStatement node) {
        commands.add(new MutationVariableDeclarationDefaultCommand(node));
        return false;
    }

    @Override
    public boolean visit(IfStatement node) {
        commands.add(new MutationIfCondToTrueCommand(node));
        return false;
    }

    @Override
    public boolean visit(SimpleName node) {
        commands.add(new MutationChangeVarNameCommand(node));
        return false;
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
    public boolean visit(MethodInvocation node) {
        if (node.getExpression() != null && node.getExpression().toString().equals("Math")) {
            commands.add(new MutationMathLibCommand(node));
        }
        return false;
    }
}
