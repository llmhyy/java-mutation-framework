package jmutation.mutation.heuristic.parser;

import jmutation.mutation.heuristic.commands.HeuristicMutationCommand;
import jmutation.mutation.heuristic.commands.MutationReturnMathCommand;
import jmutation.mutation.heuristic.commands.MutationReturnStmtLiteralCommand;
import jmutation.mutation.heuristic.commands.strong.MutationBlockRemovalCommand;
import jmutation.mutation.heuristic.commands.strong.MutationChangeVarNameCommand;
import jmutation.mutation.heuristic.commands.strong.MutationIfCondToTrueCommand;
import jmutation.mutation.heuristic.commands.strong.MutationReturnReplaceArgCommand;
import jmutation.mutation.heuristic.commands.strong.MutationReturnStmtCommand;
import jmutation.mutation.heuristic.commands.strong.MutationVariableDeclarationDefaultCommand;
import jmutation.utils.RandomSingleton;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Similar to MutationParser, but for mutations that are stronger or break data dependencies, and we do not want to use them unless test case cannot fail
 */
public class StrongMutationParser extends MutationParser {

    @Override
    public boolean visit(ReturnStatement node) {
        List<HeuristicMutationCommand> possibleCommands = new ArrayList<>();
        possibleCommands.add(new MutationReturnReplaceArgCommand(node));
        possibleCommands.add(new MutationReturnStmtLiteralCommand(node));
        possibleCommands.add(new MutationReturnMathCommand(node));
        possibleCommands.add(new MutationReturnStmtCommand(node));
        possibleCommands = possibleCommands.stream().filter(command -> command.canExecute()).collect(Collectors.toList());
        if (possibleCommands.isEmpty()) {
            return true;
        }
        possibleCommands = RandomSingleton.getSingleton().shuffle(possibleCommands);
        command = possibleCommands.get(0);
        return false;
    }

    @Override
    public boolean visit(Block node) {
        command = new MutationBlockRemovalCommand(node);
        return false;
    }

    @Override
    public boolean visit(VariableDeclarationStatement node) {
        command = new MutationVariableDeclarationDefaultCommand(node);
        return false;
    }

    @Override
    public boolean visit(IfStatement node) {
        command = new MutationIfCondToTrueCommand(node);
        return false;
    }

    @Override
    public boolean visit(SimpleName node) {
        command = new MutationChangeVarNameCommand(node);
        return false;
    }
}
