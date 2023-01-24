package jmutation.mutation.heuristic.parser;

import jmutation.mutation.heuristic.commands.MutationReturnMathCommand;
import jmutation.mutation.heuristic.commands.MutationReturnStmtLiteralCommand;
import jmutation.mutation.heuristic.commands.strong.MutationBlockRemovalCommand;
import jmutation.mutation.heuristic.commands.strong.MutationChangeVarNameCommand;
import jmutation.mutation.heuristic.commands.strong.MutationIfCondToTrueCommand;
import jmutation.mutation.heuristic.commands.strong.MutationReturnReplaceArgCommand;
import jmutation.mutation.heuristic.commands.strong.MutationReturnStmtCommand;
import jmutation.mutation.heuristic.commands.strong.MutationVariableDeclarationDefaultCommand;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

/**
 * Similar to MutationParser, but for mutations that are stronger or break data dependencies, and we do not want to use them unless test case cannot fail
 */
public class StrongMutationParser extends MutationParser {

    @Override
    public boolean visit(ReturnStatement node) {
        commands.add(new MutationReturnReplaceArgCommand(node));
        commands.add(new MutationReturnStmtLiteralCommand(node));
        commands.add(new MutationReturnMathCommand(node));
        commands.add(new MutationReturnStmtCommand(node));
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
}
