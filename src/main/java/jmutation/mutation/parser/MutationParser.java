package jmutation.mutation.parser;

import jmutation.mutation.commands.*;
import org.eclipse.jdt.core.dom.ASTNode;

public class MutationParser {
    static public MutationCommand createMutationCommand(ASTNode node) {
        // TODO: check and return correct mutation command based on contents of ASTNode
        return new MutationOperatorCommand(node);
    }
}
