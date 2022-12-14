package jmutation.mutation.explainable;

import jmutation.execution.Coverage;
import jmutation.model.mutation.MutationRange;
import jmutation.model.project.Project;
import jmutation.mutation.MutationCommand;
import jmutation.mutation.Mutator;
import jmutation.mutation.explainable.doc.DocumentBasedMutationCommand;
import jmutation.mutation.explainable.doc.code.CodeChunkGetter;
import jmutation.mutation.explainable.doc.code.CornerCaseCommentFocus;
import jmutation.mutation.explainable.doc.code.matcher.SemanticMatcher;
import jmutation.mutation.explainable.doc.model.CodeChunk;
import jmutation.mutation.explainable.doc.model.JavaComment;
import jmutation.mutation.explainable.doc.model.JavaFileComment;
import jmutation.mutation.explainable.doc.parser.ProjectCommentParser;
import jmutation.mutation.explainable.doc.parser.ProjectCommentParser.ProjectParserBuilder;
import jmutation.mutation.explainable.doc.parser.handler.CoverageFilter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ExplainableMutator extends Mutator {
    @Override
    public Project mutate(Coverage coverage, Project project) {
        // Only mutate coverage
        // go through the coverage
        // Only parse from files in coverage
        try {
            jmutation.mutation.explainable.doc.model.Project projectForParser =
                    new jmutation.mutation.explainable.doc.model.Project(project.getRoot().getCanonicalPath());
            CoverageFilter coverageFilter = new CoverageFilter(coverage);
            ProjectCommentParser projectCommentParser = new ProjectParserBuilder(projectForParser).
                    addFilter(coverageFilter).
                    build();
            Iterator<JavaFileComment> commentsInFiles = projectCommentParser.parse();
            CodeChunkGetter codeChunkGetter = new CodeChunkGetter(new CornerCaseCommentFocus(), new SemanticMatcher());
            while (commentsInFiles.hasNext()) {
                JavaFileComment fileComments = commentsInFiles.next();
                List<JavaComment> comments = fileComments.getComments();
                for (JavaComment comment : comments) {
                    List<CodeChunk> codeChunks = codeChunkGetter.get(comment, comment.getFilePath());
                    for (CodeChunk codeChunk : codeChunks) {
                        MutationCommand mutationCommand = parseCodeAndCommentIntoMutation(codeChunk, comment);
                        if (!mutationCommand.canExecute()) {
                            continue;
                        }
                        mutationCommand.executeMutation();
                        mutationHistory.add(mutationCommand);
                        File file = new File(comment.getFilePath());
                        String originalSource = Files.readString(file.toPath());
                        writeToFile(mutationCommand.getRewriter(), file);
                        if (!compileProject(project)) {
                            try (FileWriter writer = new FileWriter(file)) {
                                writer.write(originalSource);
                            }
                            continue;
                        }
                        return project;
                    }
                }
            }
            return project;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return project;
    }

    private MutationCommand parseCodeAndCommentIntoMutation(CodeChunk codeChunk, JavaComment comment) {
        // Perhaps can use comment type to determine mutation type
        return new DocumentBasedMutationCommand(codeChunk.getNode());
    }

    @Override
    public List<MutationCommand> analyse(List<MutationRange> ranges, Project project) {
        // TODO: Implement this
        return new ArrayList<>();
    }
}
