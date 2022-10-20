package jmutation.mutation.semantic;

import jmutation.execution.Coverage;
import jmutation.model.project.Project;
import jmutation.mutation.Mutator;
import jmutation.mutation.semantic.semseed.io.PatternIO;
import jmutation.mutation.semantic.semseed.io.handler.FileHandler;
import jmutation.mutation.semantic.semseed.model.Pattern;

import java.util.List;

public class SemanticMutator extends Mutator {
    private String patternFilePath;

    public Project mutate(Coverage coverage, Project project) {
        // Get all patterns from file
        // Do static analysis (top k id/lit in project + id/lit in each file), future work: Cache it in temp directory
        // Get ASTNodes that can be mutated using syntax matcher
        // Randomize list of nodes
        // Create mutation command based on first node (Must take in possible replacements in constructor?)
        // execute it
        // Recompile
        // If fail, redo with second ASTNode
        List<Pattern> patterns = readPatternsFromFile(patternFilePath);

        return null;
    }

    private List<Pattern> readPatternsFromFile(String filePath) {
        FileHandler fileHandler = new FileHandler();
        fileHandler.setSource(filePath);
        PatternIO patternIO = new PatternIO(fileHandler);
        return patternIO.readAll();
    }
}
